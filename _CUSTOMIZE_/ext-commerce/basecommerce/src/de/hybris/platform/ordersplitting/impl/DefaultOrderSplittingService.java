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
package de.hybris.platform.ordersplitting.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.jalo.JaloInvalidParameterException;
import de.hybris.platform.jalo.numberseries.NumberGenerator;
import de.hybris.platform.jalo.numberseries.NumberSeriesManager;
import de.hybris.platform.ordersplitting.ConsignmentCreationException;
import de.hybris.platform.ordersplitting.ConsignmentService;
import de.hybris.platform.ordersplitting.OrderSplittingService;
import de.hybris.platform.ordersplitting.constants.OrdersplittingConstants;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.strategy.SplittingStrategy;
import de.hybris.platform.ordersplitting.strategy.impl.OrderEntryGroup;
import de.hybris.platform.servicelayer.model.ModelService;


/**
 * Default Implementation of {@link OrderSplittingService}
 */
public class DefaultOrderSplittingService implements OrderSplittingService
{
	private static final Logger LOG = Logger.getLogger(DefaultOrderSplittingService.class);
	private List<SplittingStrategy> strategiesList = new LinkedList<SplittingStrategy>();
	private ModelService modelService;
	private ConsignmentService consignmentService;


	/**
	 * @return the strategiesList
	 */
	public List<SplittingStrategy> getStrategiesList()
	{
		return strategiesList;
	}

	/**
	 * @param strategiesList
	 *           the strategiesList to set
	 */
	public void setStrategiesList(final List<SplittingStrategy> strategiesList)
	{
		this.strategiesList = strategiesList;
	}


	/**
	 * @param modelService
	 *           the modelService to set
	 */
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	/**
	 * @return the modelService
	 */
	public ModelService getModelService()
	{
		return modelService;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.ordersplitting.OrderSplittingService#splitOrderForConsignment(java.util.List)
	 */
	@Override
	public List<ConsignmentModel> splitOrderForConsignment(final AbstractOrderModel order,
			final List<AbstractOrderEntryModel> orderEntryList) throws ConsignmentCreationException
	{
		final List<ConsignmentModel> listConsignmentModel = splitOrderForConsignmentNotPersist(order, orderEntryList);

		for (final ConsignmentModel consignment : listConsignmentModel)
		{
			modelService.save(consignment);
		}
		modelService.save(order);
		return listConsignmentModel;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see de.hybris.platform.ordersplitting.OrderSplittingService#splitOrderForConsignmentNotPersist(java.util.List)
	 */
	@Override
	public List<ConsignmentModel> splitOrderForConsignmentNotPersist(final AbstractOrderModel order,
			final List<AbstractOrderEntryModel> orderEntryList) throws ConsignmentCreationException
	{
		List<OrderEntryGroup> splitedList = new ArrayList<OrderEntryGroup>();

		final OrderEntryGroup tmpOrderEntryList = new OrderEntryGroup();
		tmpOrderEntryList.addAll(orderEntryList);
		splitedList.add(tmpOrderEntryList);
		// fire strategies
		if (strategiesList == null || strategiesList.isEmpty())
		{
			LOG.warn("No splitting strategies were configured!");
		}
		for (final SplittingStrategy strategy : strategiesList)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Applying order splitting strategy : [" + strategy.getClass().getName() + "]");
			}
			splitedList = strategy.perform(splitedList);
		}


		String orderCode;

		if (order == null)
		{

			orderCode = getUniqueNumber(OrdersplittingConstants.ORDER_KEY, OrdersplittingConstants.ORDER_KEY_DIGITS,
					OrdersplittingConstants.ORDER_KEY_STARTVAL);
		}
		else
		{
			orderCode = order.getCode();
		}


		final List<ConsignmentModel> consignmentList = new ArrayList<ConsignmentModel>();
		char prefixCode = 'a';
		for (final OrderEntryGroup orderEntryResultList : splitedList)
		{
			final ConsignmentModel cons = consignmentService.createConsignment(order, prefixCode + orderCode, orderEntryResultList);
			prefixCode += 1;

			for (final SplittingStrategy strategy : strategiesList)
			{
				strategy.afterSplitting(orderEntryResultList, cons);
			}

			consignmentList.add(cons);
		}

		return consignmentList;
	}

	public void setConsignmentService(final ConsignmentService consignmentService)
	{
		this.consignmentService = consignmentService;
	}


	protected String getUniqueNumber(final String code, final int digits, final String startValue)
	{
		try
		{
			NumberSeriesManager.getInstance().getNumberSeries(code);
		}
		catch (final JaloInvalidParameterException e)
		{// do nothing about the exception
			if (LOG.isDebugEnabled())
			{
				LOG.debug("Invalid Parameter Exception" + e);
			}
			NumberSeriesManager.getInstance().createNumberSeries(code, startValue,
					NumberGenerator.NumberSeriesConstants.TYPES.NUMERIC, digits, null);

		}
		return NumberSeriesManager.getInstance().getUniqueNumber(code, digits);
	}


}
