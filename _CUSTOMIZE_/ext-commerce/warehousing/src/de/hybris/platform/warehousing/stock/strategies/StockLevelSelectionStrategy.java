/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousing.stock.strategies;

import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.warehousing.model.AllocationEventModel;

import java.util.Collection;
import java.util.Map;


/**
 * Strategy used to select a stock level to be associated with an inventory event
 */
public interface StockLevelSelectionStrategy
{

	/**
	 * Sorts and splits the {@link StockLevelModel} and returns a Map of {@link StockLevelModel} to fulfill the quantity passed.
	 *
	 * @param stockLevels
	 * 		collection of stock level model
	 * @param quantityToAllocate
	 * 		total quantity to be allocated
	 * @return a map of {@link StockLevelModel} and quantity to be allocated per stock level
	 */
	Map<StockLevelModel, Long> getStockLevelsForAllocation(Collection<StockLevelModel> stockLevels, Long quantityToAllocate);

	/**
	 * Extracts the stock levels out of the list {@link AllocationEventModel}. Sorts, splits and returns a Map of {@link StockLevelModel} to
	 * fulfill the quantity passed.
	 *
	 * @param allocationEvents
	 * 		collection of allocation events
	 * @param quantityToCancel
	 * 		total quantity to be cancelled
	 * @return a map of {@link StockLevelModel} a the quantity to cancel for this stock level.
	 */
	Map<StockLevelModel, Long> getStockLevelsForCancellation(Collection<AllocationEventModel> allocationEvents,
			Long quantityToCancel);

}
