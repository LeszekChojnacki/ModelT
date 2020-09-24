/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.refund.impl;

import de.hybris.platform.basecommerce.enums.OrderEntryStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.jalo.JaloItemNotFoundException;
import de.hybris.platform.order.CalculationService;
import de.hybris.platform.order.OrderService;
import de.hybris.platform.order.exceptions.CalculationException;
import de.hybris.platform.orderhistory.OrderHistoryService;
import de.hybris.platform.orderhistory.model.OrderHistoryEntryModel;
import de.hybris.platform.refund.RefundService;
import de.hybris.platform.refund.dao.RefundDao;
import de.hybris.platform.returns.OrderReturnRecordHandler;
import de.hybris.platform.returns.OrderReturnRecordsHandlerException;
import de.hybris.platform.returns.jalo.ReturnRequest;
import de.hybris.platform.returns.model.RefundEntryModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.servicelayer.exceptions.SystemException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link RefundService}<br/>
 * <b>Sample usage:</b>
 *
 * <pre>
 * // order creation
 * final OrderModel order = orderService.placeOrder(cart, deliveryAddress, null, paymentInfo);
 * // create 'returns request'
 * final ReturnRequestModel request = returnService.createReturnRequest(order);
 * // assign RMA to 'return request'
 * returnService.createRMA(request);
 * // based on the original order a 'preview' will be created, so that we can apply the refunds without modifying the original order.
 * final OrderModel refundOrderPreview = refundService.createRefundOrderPreview(request, order);
 * // ... after that we will apply the refunds on the preview of our order AND DON'T create a {@link OrderHistoryEntryModel} entry
 * refundService.apply(Arrays.asList(refundEntry), refundOrderPreview), false);
 * // afterwards, if the customer agrees with the offered refund, we would apply the refund on the original order AND create a {@link OrderHistoryEntryModel} entry
 * refundService.apply(Arrays.asList(refundEntry), order), true);
 * </pre>
 *
 */
public class DefaultRefundService implements RefundService
{
	private static final Logger LOG = Logger.getLogger(DefaultRefundService.class.getName());

	private OrderReturnRecordHandler modificationHandler;
	private RefundDao refundDao;
	private OrderService orderService;
	private CalculationService calculationService;
	private FlexibleSearchService flexibleSearchService;
	private OrderHistoryService orderHistoryService;
	private ModelService modelService;

	/**
	 * @param refundDao
	 *           the refundDao to set
	 */
	public void setRefundDao(final RefundDao refundDao)
	{
		this.refundDao = refundDao;
	}

	/**
	 * @param modificationHandler
	 *           the modificationHandler to set
	 */
	public void setModificationHandler(final OrderReturnRecordHandler modificationHandler)
	{
		this.modificationHandler = modificationHandler;
	}

	public OrderReturnRecordHandler getModificationHandler()
	{
		return this.modificationHandler;
	}

	protected AbstractOrderEntryModel getOrderEntry(final RefundEntryModel refund, final AbstractOrderModel order)
	{
		final AbstractOrderEntryModel refundOrderEntry = refund.getOrderEntry();
		AbstractOrderEntryModel ret = null;
		for (final AbstractOrderEntryModel original : order.getEntries())
		{
			if (original.equals(refundOrderEntry))
			{
				ret = original;
				break;
			}
		}
		return ret;
	}

	/**
	 * Create a "refund order" based on the specified order instance. The returned refund order, will be a clone of the
	 * original one (see {@link OrderHistoryService#createHistorySnapshot(OrderModel)}. This new instance should be used
	 * for applying the refund. (see: RefundService#calculate)<br>
	 * <b>Note:</b>the {@link ReturnRequest} instance of the specified order will not be 'cloned' or 'referenced'.
	 *
	 * @param original
	 *           the original order
	 * @return the refund order
	 */
	@Override
	public OrderModel createRefundOrderPreview(final OrderModel original)
	{
		final OrderModel refundOrder = getOrderHistoryService().createHistorySnapshot(original);
		// no need for it, because we use this "preview" only for "price-pre-calculation" based on the assigned "refund entries"
		refundOrder.setReturnRequests(null);
		getModelService().saveAll(Arrays.asList(refundOrder));
		return refundOrder;
	}

	/**
	 * Based on the assigned refund entries the order will be recalculated
	 *
	 * @param refunds
	 *           the refunds for which the amount will be calculated
	 * @param order
	 *           the refund order for which the refund will be calculated
	 */
	@Override
	public void apply(final List<RefundEntryModel> refunds, final OrderModel order)
	{
		for (final Iterator it = refunds.iterator(); it.hasNext();)
		{
			final RefundEntryModel refund = (RefundEntryModel) it.next();
			final AbstractOrderEntryModel orderEntry = getOrderEntry(refund, order);
			if (orderEntry != null)
			{
				final long newQuantity = orderEntry.getQuantity().longValue() - refund.getExpectedQuantity().longValue();
				orderEntry.setQuantity(Long.valueOf(newQuantity));
				orderEntry.setCalculated(Boolean.FALSE);


				if (newQuantity <= 0)
				{
					orderEntry.setQuantityStatus(OrderEntryStatus.DEAD);
				}

				getModelService().save(orderEntry);
			}
		}

		order.setCalculated(Boolean.FALSE);
		getModelService().save(order);

		try
		{
			getCalculationService().calculate(order);
		}
		catch (final CalculationException e)
		{
			throw new SystemException("Could not calculate order [" + order.getCode() + "] due to : " + e.getMessage(), e);
		}
	}

	/**
	 * Based on the assigned refund entries of the 'previewOrder'
	 * {@link RefundService#createRefundOrderPreview(OrderModel)} , the 'finalOrder' will be recalculated and
	 * modification entries will be written.
	 *
	 * @param previewOrder
	 *           the order which holds the preview of refund
	 * @param request
	 *           the request which holds the order for which the refunds will be calculated AND the "refund entries"
	 *           which will be logged by {@link OrderReturnRecordHandler}
	 * @throws OrderReturnRecordsHandlerException in case of DAO error
	 */
	@Override
	public void apply(final OrderModel previewOrder, final ReturnRequestModel request) throws OrderReturnRecordsHandlerException
	{
		final OrderModel finalOrder = request.getOrder();
		getModificationHandler().createRefundEntry(finalOrder, getRefunds(request),
				"Refund request for order: " + finalOrder.getCode());

		for (final AbstractOrderEntryModel previewEntry : previewOrder.getEntries())
		{
			final AbstractOrderEntryModel originalEntry = getEntry(finalOrder, previewEntry.getEntryNumber());
			final long newQuantity = previewEntry.getQuantity().longValue();

			originalEntry.setQuantity(Long.valueOf(newQuantity));
			originalEntry.setCalculated(Boolean.FALSE);

			if (newQuantity <= 0)
			{
				originalEntry.setQuantityStatus(OrderEntryStatus.DEAD);
			}

			getModelService().save(originalEntry);
		}

		finalOrder.setCalculated(Boolean.FALSE);
		getModelService().save(finalOrder);

		try
		{
			getCalculationService().calculate(finalOrder);
		}
		catch (final CalculationException e)
		{
			throw new SystemException("Could not calculate order [" + finalOrder.getCode() + "] due to : " + e.getMessage(), e);
		}
	}

	/**
	 * Generates a order history comment from the specified refunds by collecting the stored 'refund notes'
	 *
	 * @param refunds
	 *           the refunds from which the nodes will be extracted
	 * @return the generated order history comment
	 */
	protected String createOrderHistoryEntryDescription(final List<RefundEntryModel> refunds)
	{
		final StringBuilder description = new StringBuilder("Refunds:");
		for (final Iterator it = refunds.iterator(); it.hasNext();)
		{
			final RefundEntryModel refund = (RefundEntryModel) it.next();
			description.append("- ").append(refund.getNotes()).append('\n');
		}
		return description.toString();
	}

	/**
	 * Creates a history entry for the 'processedOrder'.
	 *
	 * @param processedOrder
	 *           the 'modified' order
	 * @param snapshot
	 *           the original of the order
	 * @return the created order history entry
	 */
	protected OrderHistoryEntryModel createOrderHistoryEntry(final OrderModel processedOrder, final OrderModel snapshot)
	{
		final OrderHistoryEntryModel historyModel = getModelService().create(OrderHistoryEntryModel.class);
		historyModel.setTimestamp(new Date());
		historyModel.setOrder(processedOrder);
		historyModel.setPreviousOrderVersion(snapshot);
		getModelService().save(historyModel);
		return historyModel;
	}

	/**
	 * Returns the order entry at a specified position.
	 *
	 * @param postion
	 * @return the related order entry (can be 'null')
	 * @throws JaloItemNotFoundException
	 */
	protected AbstractOrderEntryModel getEntry(final AbstractOrderModel order, final Integer postion)
	{
		final Map values = new HashMap();
		values.put("o", order);
		values.put("nr", postion);

		final SearchResult<AbstractOrderEntryModel> res = getFlexibleSearchService()
				.search(
						"SELECT {" + AbstractOrderEntryModel.PK + "} FROM {" + AbstractOrderEntryModel._TYPECODE + "} WHERE {"
								+ AbstractOrderEntryModel.ORDER + "} = ?o AND {" + AbstractOrderEntryModel.ENTRYNUMBER + "} = ?nr",
						values);

		if (res.getResult().isEmpty())
		{
			LOG.warn("can't find entry number " + postion + " within order " + order);
			return null;
		}
		else if (res.getResult().size() > 1)
		{
			LOG.warn("there are more than one entries [" + res.getResult() + "] with the same number " + postion
					+ " within one order");
			return null;
		}
		else
		{
			return res.getResult().iterator().next();
		}
	}

	/**
	 * Returns the refunds for the specified order
	 *
	 * @param request
	 *           the request
	 * @return the refunds
	 */
	@Override
	public List<RefundEntryModel> getRefunds(final ReturnRequestModel request)
	{
		return refundDao.getRefunds(request);
	}

	protected RefundDao getRefundDao()
	{
		return refundDao;
	}

	protected OrderService getOrderService()
	{
		return orderService;
	}

	@Required
	public void setOrderService(final OrderService orderService)
	{
		this.orderService = orderService;
	}

	protected CalculationService getCalculationService()
	{
		return calculationService;
	}

	@Required
	public void setCalculationService(final CalculationService calculationService)
	{
		this.calculationService = calculationService;
	}

	protected FlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService;
	}

	@Required
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}

	protected OrderHistoryService getOrderHistoryService()
	{
		return orderHistoryService;
	}

	@Required
	public void setOrderHistoryService(final OrderHistoryService orderHistoryService)
	{
		this.orderHistoryService = orderHistoryService;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

}
