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
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.strategy.AbstractSplittingStrategy;
import de.hybris.platform.stock.StockService;

import org.springframework.beans.factory.annotation.Required;


public class SplitByAvailableCount extends AbstractSplittingStrategy
{
	private StockService stockService;

	/**
	 * @param stockService
	 *           the stockService to set
	 */
	@Required
	public void setStockService(final StockService stockService)
	{
		this.stockService = stockService;
	}

	@Override
	public Object getGroupingObject(final AbstractOrderEntryModel orderEntry)
	{
		int res = 0;
		for (final StockLevelModel stockLevel : stockService.getAllStockLevels(orderEntry.getProduct()))
		{
			res += stockLevel.getAvailable();
		}

		return Boolean.valueOf(res >= orderEntry.getQuantity().intValue());
	}

	@Override
	public void afterSplitting(final Object groupingObject, final ConsignmentModel createdOne)
	{
		//nothing to do		
	}

}
