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
package de.hybris.platform.stock.strategy.impl;

import de.hybris.platform.basecommerce.enums.InStockStatus;
import de.hybris.platform.basecommerce.enums.StockLevelStatus;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.stock.strategy.StockLevelStatusStrategy;

import java.util.Collection;


/**
 * Default implementation of {@link StockLevelStatusStrategy}.
 */
public class DefaultStockLevelStatusStrategy implements StockLevelStatusStrategy
{

	@Override
	public StockLevelStatus checkStatus(final StockLevelModel stockLevel)
	{
		if (stockLevel == null)
		{
			return StockLevelStatus.OUTOFSTOCK;
		}
		else
		{
			if (InStockStatus.FORCEINSTOCK.equals(stockLevel.getInStockStatus()))
			{
				return StockLevelStatus.INSTOCK;
			}
			else
			{
				final int result = stockLevel.getAvailable() - stockLevel.getReserved() + stockLevel.getOverSelling();
				if (result > 0)
				{
					return StockLevelStatus.INSTOCK;
				}
				else
				{
					return StockLevelStatus.OUTOFSTOCK;
				}
			}
		}
	}

	@Override
	public StockLevelStatus checkStatus(final Collection<StockLevelModel> stockLevels)
	{
		StockLevelStatus resultStatus = StockLevelStatus.OUTOFSTOCK;
		for (final StockLevelModel level : stockLevels)
		{
			resultStatus = this.checkStatus(level);
			if (StockLevelStatus.INSTOCK.equals(resultStatus))
			{
				break;
			}
		}
		return resultStatus;
	}

}
