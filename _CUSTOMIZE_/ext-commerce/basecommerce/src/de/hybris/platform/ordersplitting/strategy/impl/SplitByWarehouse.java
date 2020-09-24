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
package de.hybris.platform.ordersplitting.strategy.impl;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.ordersplitting.WarehouseService;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.ordersplitting.strategy.SplittingStrategy;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


public class SplitByWarehouse implements SplittingStrategy
{

	private static final Logger LOG = Logger.getLogger(SplitByWarehouse.class.getName());

	private WarehouseService warehouseService;

	private static final String WAREHOUSE_LIST_NAME = "WAREHOUSE_LIST";
	private static final String RANDOM_ALGORITHM = "SHA1PRNG";

	protected WarehouseModel getWarehouse(final OrderEntryGroup orderEntries)
	{
		return chooseBestWarehouse(orderEntries);
	}

	protected List<OrderEntryGroup> splitForWarehouses(final OrderEntryGroup orderEntryList)
	{
		final List<OrderEntryGroup> result = new ArrayList<>();

		//list of orderEntry - todoList
		final OrderEntryGroup todoEntryList = orderEntryList.getEmpty();

		//List of working elements
		OrderEntryGroup workingOrderEntryList = sortOrderEntryBeforeWarehouseSplitting(orderEntryList);

		// list of entries that can't be performed by any warehouse
		final OrderEntryGroup emptyOrderEntryList = orderEntryList.getEmpty();

		do
		{
			//clear need here before normal proceeding
			todoEntryList.clear();

			//list of warehouse after retailAll of prev. orderEntries
			List<WarehouseModel> tmpWarehouseResult = null;
			//list of orderEntries that can be realized by tmpWarehouseResult
			final OrderEntryGroup tmpOrderEntryResult = orderEntryList.getEmpty();


			for (final AbstractOrderEntryModel orderEntry : workingOrderEntryList)
			{
				final List<WarehouseModel> currentPossibleWarehouses = getPossibleWarehouses(orderEntry);

				// no warehouse can solve order entry
				if (currentPossibleWarehouses.isEmpty())
				{
					emptyOrderEntryList.add(orderEntry);
					continue;
				}

				//first time we wish to store all warehouses
				if (tmpWarehouseResult != null)
				{
					//if not first time we take retainAll
					currentPossibleWarehouses.retainAll(tmpWarehouseResult);
				}

				// if this orderEntry can't be realized whit previous set
				if (currentPossibleWarehouses.isEmpty())
				{
					// add entry to todoList
					todoEntryList.add(orderEntry);
				}
				else
				{
					//we store list after retainAll and add orderEntry to tmpResult
					tmpWarehouseResult = currentPossibleWarehouses;
					tmpOrderEntryResult.add(orderEntry);
				}
			}

			if (!tmpOrderEntryResult.isEmpty())
			{
				//add chosen one to result
				tmpOrderEntryResult.setParameter(WAREHOUSE_LIST_NAME, tmpWarehouseResult);
				result.add(tmpOrderEntryResult);
			}
			//starting process with new (not split yet) orderEntry List
			//remember to make clean at begin of new loop - if will not done unfinished loop will aper
			workingOrderEntryList = todoEntryList.getEmpty();
			workingOrderEntryList.addAll(todoEntryList);
		}
		//still something to do
		while (!todoEntryList.isEmpty());

		//entries for which warehouse can't be chosen
		if (!emptyOrderEntryList.isEmpty())
		{
			result.add(emptyOrderEntryList);
		}

		return result;
	}

	protected List<WarehouseModel> getPossibleWarehouses(final AbstractOrderEntryModel orderEntry)
	{
		return new ArrayList<>(warehouseService.getWarehousesWithProductsInStock(orderEntry));
	}

	/**
	 * Choose best warehouse this function is called by getWarehouseList after we have set of possible warehouses.
	 *
	 * @param orderEntries
	 *           the order entries
	 *
	 * @return the warehouse model
	 */
	protected WarehouseModel chooseBestWarehouse(final OrderEntryGroup orderEntries)
	{
		try
		{
			final List<WarehouseModel> warehouses = (List<WarehouseModel>) orderEntries.getParameter(WAREHOUSE_LIST_NAME);
			if ((warehouses == null) || (warehouses.isEmpty()))
			{
				return null;
			}

			final SecureRandom sRnd = SecureRandom.getInstance(RANDOM_ALGORITHM);

			//basic solution is to random
			return warehouses.get(sRnd.nextInt(warehouses.size()));
		}
		catch (final NoSuchAlgorithmException ex)
		{
			LOG.error("Choose best warehouse failed!!", ex);
			return null;
		}

	}

	/**
	 * Sort order entry before warehouse splitting.
	 *
	 * @param listOrderEntry
	 *           the list order entry
	 *
	 * @return the list< order entry model>
	 */
	protected OrderEntryGroup sortOrderEntryBeforeWarehouseSplitting(final OrderEntryGroup listOrderEntry)
	{
		// basic - not sort
		return listOrderEntry;
	}

	@Override
	public List<OrderEntryGroup> perform(final List<OrderEntryGroup> orderEntryGroup)
	{
		final List<OrderEntryGroup> result = new ArrayList<>();

		for (final OrderEntryGroup orderEntry : orderEntryGroup)
		{
			final List<OrderEntryGroup> tmpList = splitForWarehouses(orderEntry);
			for (final OrderEntryGroup tmpOrderEntryGroup : tmpList)
			{
				result.add(tmpOrderEntryGroup);
			}

		}

		return result;
	}

	@Override
	public void afterSplitting(final OrderEntryGroup group, final ConsignmentModel createdOne)
	{
		createdOne.setWarehouse(chooseBestWarehouse(group));
	}

	@Required
	public void setWarehouseService(final WarehouseService warehouseService)
	{
		this.warehouseService = warehouseService;
	}

	protected WarehouseService getWarehouseService()
	{
		return warehouseService;
	}
}
