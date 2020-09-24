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
package de.hybris.platform.returns.impl;

import de.hybris.platform.basecommerce.enums.RefundReason;
import de.hybris.platform.basecommerce.enums.ReplacementReason;
import de.hybris.platform.basecommerce.enums.ReturnAction;
import de.hybris.platform.basecommerce.enums.ReturnStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.order.AbstractOrder;
import de.hybris.platform.jalo.order.AbstractOrderEntry;
import de.hybris.platform.jalo.order.OrderEntry;
import de.hybris.platform.refund.RefundService;
import de.hybris.platform.returns.OrderReturnException;
import de.hybris.platform.returns.OrderReturnRecordHandler;
import de.hybris.platform.returns.RMAGenerator;
import de.hybris.platform.returns.ReturnActionRequest;
import de.hybris.platform.returns.ReturnActionRequestExecutor;
import de.hybris.platform.returns.ReturnActionResponse;
import de.hybris.platform.returns.ReturnCallbackService;
import de.hybris.platform.returns.ReturnService;
import de.hybris.platform.returns.dao.ReplacementOrderDao;
import de.hybris.platform.returns.dao.ReturnRequestDao;
import de.hybris.platform.returns.jalo.ReturnEntry;
import de.hybris.platform.returns.model.OrderReturnRecordModel;
import de.hybris.platform.returns.model.RefundEntryModel;
import de.hybris.platform.returns.model.ReplacementEntryModel;
import de.hybris.platform.returns.model.ReplacementOrderEntryModel;
import de.hybris.platform.returns.model.ReplacementOrderModel;
import de.hybris.platform.returns.model.ReturnEntryModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.returns.processor.RefundOrderProcessor;
import de.hybris.platform.returns.processor.ReplacementOrderProcessor;
import de.hybris.platform.returns.processor.ReturnEntryProcessor;
import de.hybris.platform.returns.strategy.ReturnableCheck;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;


/**
 * Default implementation of "Returns handling". It offers ...
 * <ul>
 * <li>Configurable "Return Merchandise Authorization" aka "Return Material Authorization" (RMA) handling
 * <li>Replacement Order handling
 * <li>Refund handling incl. configurable calculation
 * <li>Configurable 'is returnable' check
 * </ul>
 */
public class DefaultReturnService implements ReturnService, ReturnCallbackService
{
	private ReplacementOrderDao replacementOrderDao;
	private ReturnRequestDao returnRequestDao;
	private OrderReturnRecordHandler modificationHandler; //NOPMD

	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(DefaultReturnService.class.getName());

	private List<ReturnableCheck> returnableChecks = new LinkedList<>();
	private RefundService refundService = null;
	private RMAGenerator generator = null;
	private ReturnEntryProcessor returnEntryProcessor = null;
	private ReplacementOrderProcessor replacementOrderProcessor = null;
	private RefundOrderProcessor refundOrderProcessor = null;
	private FlexibleSearchService flexibleSearchService;
	private ReturnActionRequestExecutor returnActionRequestExecutor;
	private ModelService modelService;

	/**
	 * @return the configured refundService (calculation)
	 */
	protected RefundService getRefundService()
	{
		return refundService;
	}

	/**
	 * @param refundService
	 *           the service to set
	 */
	@Required
	public void setRefundService(final RefundService refundService)
	{
		this.refundService = refundService;
	}

	public void setReturnsEntryProcessor(final ReturnEntryProcessor returnEntryProcessor)
	{
		this.returnEntryProcessor = returnEntryProcessor;
	}

	protected ReturnEntryProcessor getReturnsEntryProcessor()
	{
		return this.returnEntryProcessor;
	}

	public void setReplacementOrderProcessor(final ReplacementOrderProcessor replacementOrderProcessor)
	{
		this.replacementOrderProcessor = replacementOrderProcessor;
	}

	protected ReplacementOrderProcessor getReplacementOrderProcessor()
	{
		return this.replacementOrderProcessor;
	}

	public void setRefundOrderProcessor(final RefundOrderProcessor refundOrderProcessor)
	{
		this.refundOrderProcessor = refundOrderProcessor;
	}

	protected RefundOrderProcessor getRefundOrderProcessor()
	{
		return this.refundOrderProcessor;
	}

	/**
	 * @return the returnableChecks
	 */
	protected List<ReturnableCheck> getReturnableChecks()
	{
		return returnableChecks;
	}

	/**
	 * @param returnableChecks
	 *           the strategiesList to set
	 */
	public void setReturnableChecks(final List<ReturnableCheck> returnableChecks)
	{
		this.returnableChecks = returnableChecks;
	}

	@Required
	public void setGenerator(final RMAGenerator generator)
	{
		this.generator = generator;
	}

	protected RMAGenerator getGenerator()
	{
		return this.generator;
	}

	/**
	 * returns a "Return Merchandise Authorization" aka "Return Material Authorization" (RMA).
	 *
	 * @param request
	 *           reference of the related ReturnsRequest
	 * @return RMA
	 */
	@Override
	public String getRMA(final ReturnRequestModel request)
	{
		return request.getRMA();
	}

	/**
	 * creates "Return Merchandise Authorization" aka "Return Material Authorization" (RMA) and assigns it to the
	 * assigned {@link ReturnRequestModel}
	 *
	 * @param request
	 *           reference of the related "ReturnRequest" for which this RMA will be created
	 * @return RMA
	 */
	@Override
	public String createRMA(final ReturnRequestModel request)
	{
		request.setRMA(getGenerator().generateRMA(request));
		getModelService().save(request);
		return request.getRMA();
	}

	/**
	 * Creates an "authorization" object (@link ReturnRequest} for the order to be returned, if there doesn't exists one
	 * for that order so far
	 *
	 * @param order
	 *           the order which should be returned
	 * @return ReturnRequest instance, which will deliver among others the RMA code of every processed "return order".
	 */
	@Override
	public ReturnRequestModel createReturnRequest(final OrderModel order)
	{
		return getReturnRequestDao().createReturnRequest(order);
	}

	@Override
	public List<ReturnRequestModel> getReturnRequests(final String code)
	{
		return getReturnRequestDao().getReturnRequests(code);
	}

	/**
	 * Returns an order by its code
	 *
	 * @param code
	 *           the code of the order
	 * @return the order
	 */
	public OrderModel getOrderByCode(final String code)
	{
		final Map<String, Object> values = new HashMap<String, Object>();
		values.put("value", code);
		final String query = "SELECT {" + Item.PK + "} FROM {" + OrderModel._TYPECODE + "} WHERE {" + AbstractOrder.CODE
				+ "} = ?value ORDER BY {" + Item.PK + "} ASC";
		final List<OrderModel> result = (List) flexibleSearchService.search(query, values).getResult();
		return result == null ? null : result.iterator().next();
	}

	@Override
	public ReplacementOrderModel getReplacementOrder(final String rma)
	{
		return getReplacementOrderDao().getReplacementOrder(rma);
	}

	/**
	 * Creates a {@link ReplacementOrderModel}
	 *
	 * @param request
	 *           the returns request to which the order will be assigned
	 * @return the Replacement Order'
	 */
	@Override
	public ReplacementOrderModel createReplacementOrder(final ReturnRequestModel request)
	{
		final ReplacementOrderModel replacement = getReplacementOrderDao().createReplacementOrder(request);
		getModelService().save(request);
		return replacement;
	}

	/**
	 * Creates a Replacement based on the assigned OrderEntry instance
	 *
	 * @param request
	 *           we use this for the verification process (order)
	 * @param entry
	 *           the original OrderEntry
	 * @param notes
	 *           some notes
	 * @param expectedQuantity
	 *           the amount of products which the customer wants to return.
	 * @param action
	 *           action which indicates if the 'returns process' will be executed immediately or is still on hold
	 *           (waiting for the article to be send back)
	 * @param reason
	 *           reason for the replacement
	 */
	@Override
	public ReplacementEntryModel createReplacement(final ReturnRequestModel request, final AbstractOrderEntryModel entry,
			final String notes, final Long expectedQuantity, final ReturnAction action, final ReplacementReason reason)

	{
		final ReplacementEntryModel returnsEntry = getModelService().create(ReplacementEntryModel.class);
		returnsEntry.setOrderEntry(entry);
		returnsEntry.setAction(action);
		returnsEntry.setNotes(notes);
		returnsEntry.setReason(reason);
		returnsEntry.setReturnRequest(request);
		returnsEntry.setStatus(ReturnStatus.WAIT);
		returnsEntry.setExpectedQuantity(expectedQuantity);
		getModelService().save(returnsEntry);
		return returnsEntry;
	}

	/**
	 * Creates a ReplRefundacement based on the assigned OrderEntry instance
	 *
	 * @param request
	 *           we use this for the verification process (order)
	 * @param entry
	 *           the original OrderEntry
	 * @param notes
	 *           some notes
	 * @param expectedQuantity
	 *           the amount of products which the customer wants to got a refund.
	 * @param action
	 *           action which indicates if the 'returns process' will be executed immediately or is still on hold
	 *           (waiting for the article to be send back)
	 * @param reason
	 *           reason for the refund
	 */
	@Override
	public RefundEntryModel createRefund(final ReturnRequestModel request, final AbstractOrderEntryModel entry, final String notes,
			final Long expectedQuantity, final ReturnAction action, final RefundReason reason)
	{
		final RefundEntryModel returnsEntry = getModelService().create(RefundEntryModel.class);
		returnsEntry.setOrderEntry(entry);
		returnsEntry.setAction(action);
		returnsEntry.setNotes(notes);
		returnsEntry.setReason(reason);
		returnsEntry.setReturnRequest(request);
		returnsEntry.setExpectedQuantity(expectedQuantity);
		returnsEntry.setStatus(ReturnStatus.WAIT);
		getModelService().save(returnsEntry);
		request.setSubtotal(
				request.getSubtotal().add(BigDecimal.valueOf(entry.getBasePrice().doubleValue() * expectedQuantity.longValue())));
		getModelService().save(request);
		return returnsEntry;
	}

	/**
	 * Adds {@link ReplacementOrderEntryModel} entries from the specified {@link ReplacementEntryModel}. Caution: This
	 * impl. only accepts {@link ReplacementEntryModel} which are not on HOLD {@link ReturnAction}.
	 *
	 * @param order
	 *           the replacement order
	 * @param replacements
	 *           the 'replacement' entries (instances which are on HOLD will be ignored)
	 */
	@Override
	public void addReplacementOrderEntries(final ReplacementOrderModel order, final List<ReplacementEntryModel> replacements)
	{
		for (final ReplacementEntryModel replacement : replacements)
		{
			if (replacement.getAction().equals(ReturnAction.HOLD))
			{
				LOG.warn("Skipping 'replacement order entry' creation, because assigned 'replacement' instance is still on 'HOLD'");
				break;
			}
			/*
			 * KKW: YTODO could use generic OrderService.addNewEntry method instead.
			 */
			final ReplacementOrderEntryModel entry = getModelService().create(ReplacementOrderEntryModel.class);
			entry.setProduct(replacement.getOrderEntry().getProduct());
			entry.setOrder(order);
			entry.setEntryNumber(replacement.getOrderEntry().getEntryNumber());
			entry.setQuantity(replacement.getExpectedQuantity());
			entry.setUnit(replacement.getOrderEntry().getUnit());
			getModelService().save(entry);

		}

		getModelService().refresh(order);
	}

	/**
	 * Returns the ReturnsEntries for the specified product
	 *
	 * @param product
	 *           the product
	 * @return the ReturnsEntry
	 */
	@Override
	public List<ReturnEntryModel> getReturnEntries(final ProductModel product)
	{
		final Map<String, Object> params = new HashMap();
		params.put("product", product);
		final String query = "SELECT {ret." + Item.PK + "} FROM {" + ReturnEntryModel._TYPECODE + " AS ret JOIN "
				+ OrderEntryModel._TYPECODE + " AS ord ON { " + ReturnEntry.ORDERENTRY + "} = { ord." + Item.PK + "}} WHERE {ord."
				+ AbstractOrderEntry.PRODUCT + "} = ?product ORDER BY {ret." + Item.PK + "} ASC";
		return (List) flexibleSearchService.search(query, params).getResult();
	}

	/**
	 * Returns the ReturnsEntries for the specified order entry
	 *
	 * @param entry
	 *           the OrderEntry
	 * @return the ReturnsEntry
	 */
	@Override
	public List<ReturnEntryModel> getReturnEntry(final AbstractOrderEntryModel entry)
	{
		final Map<String, Object> params = new HashMap();
		params.put("entry", entry);
		final String query = "SELECT {ret." + Item.PK + "} FROM { " + ReturnEntryModel._TYPECODE + " AS ret} WHERE {"
				+ ReturnEntry.ORDERENTRY + "} = ?entry ORDER BY {ret." + Item.PK + "} ASC";
		return (List) flexibleSearchService.search(query, params).getResult();
	}

	/**
	 * Returns all Replacements for the specified returns request
	 *
	 * @param request
	 *           the ReturnRequestModel
	 * @return the replacements
	 */
	@Override
	public List<ReplacementEntryModel> getReplacements(final ReturnRequestModel request)
	{
		final Map<String, Object> params = new HashMap();
		params.put("request", request);

		final String query = "SELECT {" + Item.PK + "} FROM {" + ReplacementEntryModel._TYPECODE + "} WHERE {"
				+ ReturnRequestModel._TYPECODE + "} = ?request ORDER BY {" + Item.PK + "} ASC";
		final List<ReplacementEntryModel> result = (List) flexibleSearchService.search(query, params).getResult();
		return result == null ? Collections.emptyList() : result;
	}

	/**
	 * Determines if the product is 'returnable' by using the injected {@link ReturnableCheck} implementations. To be
	 * successful every strategy has to return 'true'. If no strategies were injected at all, 'true' will be returned as
	 * default.
	 *
	 * @param order
	 *           the related original order
	 * @param entry
	 *           the ordered product which will be returned
	 * @param returnQuantity
	 *           the quantity of entries to be returned
	 * @return true if product is 'returnable'
	 */
	@Override
	public boolean isReturnable(final OrderModel order, final AbstractOrderEntryModel entry, final long returnQuantity)
	{
		// process assigned strategies
		boolean isReturnable = (getReturnableChecks() == null || getReturnableChecks().isEmpty()) ? true : false;
		for (final ReturnableCheck strategy : getReturnableChecks())
		{
			isReturnable = strategy.perform(order, entry, returnQuantity);

			if (!isReturnable)
			{
				return false;
			}
		}
		return isReturnable;
	}

	/**
	 * Returns all returnable {@link OrderEntry}'s. The algorithm returns the max. returnable quantity for every
	 * single(!) order entry.
	 *
	 * @param order
	 *           the related order
	 * @return all returnable {@link OrderEntry} and their returnable quantity
	 */
	@Override
	public Map<AbstractOrderEntryModel, Long> getAllReturnableEntries(final OrderModel order)
	{
		final Map<AbstractOrderEntryModel, Long> returnable = new HashMap<AbstractOrderEntryModel, Long>();

		for (final AbstractOrderEntryModel entry : order.getEntries())
		{
			long returnableQuantity = 0;
			for (long i = entry.getQuantity().longValue(); i > 0; i--)
			{
				if (isReturnable(order, entry, i))
				{
					returnableQuantity = i;
					break;
				}
			}
			if (returnableQuantity > 0)
			{
				returnable.put(entry, Long.valueOf(returnableQuantity));
			}
		}
		return returnable;
	}

	@Override
	public OrderReturnRecordModel getOrderReturnRecordForOrder(final OrderModel order) throws OrderReturnException
	{
		return getModificationHandler().getReturnRecord(order);
	}

	@Override
	public void requestManualPaymentReversalForReturnRequest(final ReturnRequestModel returnRequest) throws OrderReturnException
	{
		Assert.notNull(returnRequest, "ReturnRequest cannot be null");

		final ReturnActionRequest returnActionRequest = new ReturnActionRequest(returnRequest);
		getReturnActionRequestExecutor().processManualPaymentReversalForReturnRequest(returnActionRequest);
	}

	@Override
	public void requestManualTaxReversalForReturnRequest(final ReturnRequestModel returnRequest) throws OrderReturnException
	{
		Assert.notNull(returnRequest, "ReturnRequest cannot be null");

		final ReturnActionRequest returnActionRequest = new ReturnActionRequest(returnRequest);
		getReturnActionRequestExecutor().processManualTaxReversalForReturnRequest(returnActionRequest);
	}

	/**
	 * Here you have the chance to 'inject' your final Returns Entry processing. For example for handling consignment
	 * creation
	 *
	 * @param entries
	 *           the entries to be process
	 */
	@Override
	public void processReturnEntries(final List<ReturnEntryModel> entries)
	{
		if (getReturnsEntryProcessor() != null)
		{
			getReturnsEntryProcessor().process(entries);
		}
	}

	/**
	 * Here you have the chance to 'inject' your final Replacement order processing. For example for handling consignment
	 * creation
	 *
	 * @param order
	 *           the order to be process
	 */
	@Override
	public void processReplacementOrder(final ReplacementOrderModel order)
	{
		if (getReplacementOrderProcessor() != null)
		{
			getReplacementOrderProcessor().process(order);
		}
	}

	/**
	 * Here you have the chance to 'inject' your final Refund order processing. For example for handling consignment
	 * creation or initiating the final payment transaction.
	 *
	 * @param order
	 *           the order to be process
	 */
	@Override
	public void processRefundOrder(final OrderModel order)
	{
		if (getRefundOrderProcessor() != null)
		{
			getRefundOrderProcessor().process(order);
		}
	}

	@Override
	public void onReturnApprovalResponse(final ReturnActionResponse approvalResponse) throws OrderReturnException
	{
		getReturnActionRequestExecutor().processApprovingRequest(approvalResponse.getReturnRequest());
	}

	@Override
	public void onReturnCancelResponse(final ReturnActionResponse cancelResponse) throws OrderReturnException
	{
		getReturnActionRequestExecutor().processCancellingRequest(cancelResponse.getReturnRequest());
	}

	@Override
	public void onReturnReceptionResponse(final ReturnActionResponse receptionResponse) throws OrderReturnException
	{
		getReturnActionRequestExecutor().processReceivingRequest(receptionResponse.getReturnRequest());
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

	protected ReturnActionRequestExecutor getReturnActionRequestExecutor()
	{
		return returnActionRequestExecutor;
	}

	@Required
	public void setReturnActionRequestExecutor(final ReturnActionRequestExecutor returnActionRequestExecutor)
	{
		this.returnActionRequestExecutor = returnActionRequestExecutor;
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

	protected ReplacementOrderDao getReplacementOrderDao()
	{
		return replacementOrderDao;
	}

	@Required
	public void setReplacementOrderDao(final ReplacementOrderDao replacementOrderDao)
	{
		this.replacementOrderDao = replacementOrderDao;
	}

	protected ReturnRequestDao getReturnRequestDao()
	{
		return returnRequestDao;
	}

	@Required
	public void setReturnRequestDao(final ReturnRequestDao returnRequestDao)
	{
		this.returnRequestDao = returnRequestDao;
	}

	protected OrderReturnRecordHandler getModificationHandler()
	{
		return modificationHandler;
	}

	@Required
	public void setModificationHandler(final OrderReturnRecordHandler modificationHandler)
	{
		this.modificationHandler = modificationHandler;
	}

}
