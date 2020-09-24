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
package de.hybris.platform.stock.strategy;

import de.hybris.platform.basecommerce.enums.StockLevelStatus;
import de.hybris.platform.ordersplitting.jalo.StockLevel;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.stock.StockService;

import java.util.Collection;


/**
 * The StockLevelStatusStrategy is used by the {@link StockService} to check the status of the specified
 * {@link StockLevel}.
 * 
 * @spring.bean stockLevelStatusStrategy
 */
public interface StockLevelStatusStrategy
{

	/**
	 * Checks the status of the specified {@link StockLevel}.
	 * 
	 * @param stockLevel
	 *           the stock level to be checked
	 * @return stock level status
	 */
	StockLevelStatus checkStatus(StockLevelModel stockLevel);

	/**
	 * Checks the status of the specified {@link StockLevel}s.
	 * 
	 * @param stockLevels
	 *           the stock levels to be checked
	 * @return stock level status
	 */
	StockLevelStatus checkStatus(Collection<StockLevelModel> stockLevels);

}
