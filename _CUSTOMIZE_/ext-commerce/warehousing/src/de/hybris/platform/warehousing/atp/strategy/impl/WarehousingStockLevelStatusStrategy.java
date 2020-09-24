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
package de.hybris.platform.warehousing.atp.strategy.impl;

import de.hybris.platform.basecommerce.enums.InStockStatus;
import de.hybris.platform.basecommerce.enums.StockLevelStatus;
import de.hybris.platform.commerceservices.stock.strategies.impl.CommerceStockLevelStatusStrategy;
import de.hybris.platform.ordersplitting.model.StockLevelModel;

import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Warehousing implementation of {@link CommerceStockLevelStatusStrategy}
 */
public class WarehousingStockLevelStatusStrategy extends CommerceStockLevelStatusStrategy
{

	private static final Logger LOGGER = LoggerFactory.getLogger(WarehousingStockLevelStatusStrategy.class);

	@Override
	public StockLevelStatus checkStatus(final Collection<StockLevelModel> stockLevels)
	{
		StockLevelStatus resultStatus = StockLevelStatus.OUTOFSTOCK;

		if (CollectionUtils.isEmpty(stockLevels))
		{
			LOGGER.debug("No stocklevel passed to check for their status. Returning OUTOFSTOCK as default.");
			return resultStatus;
		}

		final boolean isInStockStatusLevel = stockLevels.stream()
				.anyMatch(stockLevel -> InStockStatus.FORCEINSTOCK.equals(stockLevel.getInStockStatus()));

		if (isInStockStatusLevel)
		{
			resultStatus = StockLevelStatus.INSTOCK;
		}
		else
		{
			final Collection<StockLevelModel> filteredStockLevels = stockLevels.stream()
					.filter(stockLevel ->  !InStockStatus.FORCEOUTOFSTOCK.equals(stockLevel.getInStockStatus()))
					.collect(Collectors.toSet());

			final Long availability = getCommerceStockLevelCalculationStrategy().calculateAvailability(filteredStockLevels);
			if (availability <= 0)
			{
				resultStatus = StockLevelStatus.OUTOFSTOCK;
			}
			else if (availability > getDefaultLowStockThreshold())
			{
				resultStatus = StockLevelStatus.INSTOCK;
			}
			else
			{
				resultStatus = StockLevelStatus.LOWSTOCK;
			}
		}

		return resultStatus;
	}

}
