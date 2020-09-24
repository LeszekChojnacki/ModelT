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
package de.hybris.platform.warehousing.stock.services;

import de.hybris.platform.basecommerce.enums.InStockStatus;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;

import java.util.Date;


/**
 * Service to get stock availability for a single warehouse.
 */
public interface WarehouseStockService
{
	/**
	 * Returns stock level value for given productCode and warehouse.
	 *
	 * @param productCode
	 * 		The product code for which we want to retrieve a specific {@link StockLevelModel} value
	 * @param warehouse
	 * 		The warehouse {@link WarehouseModel} for which we want to retrieve a specific {@link StockLevelModel} value
	 * @return actual stock level
	 */
	Long getStockLevelForProductCodeAndWarehouse(final String productCode, final WarehouseModel warehouse);

	/**
	 * Creates and saves a new {@link StockLevelModel}.
	 *
	 * @param productCode
	 * 		The product code for which we want to retrieve a specific {@link StockLevelModel} (mandatory).
	 * @param warehouse
	 * 		The warehouse {@link WarehouseModel} for which we want to retrieve a specific {@link StockLevelModel}
	 * @param initialQuantityOnHand
	 * 		The initial Quantity on hand
	 * @param status
	 * 		The {@link InStockStatus}
	 * @param releaseDate
	 * 		The release date of the specific {@link StockLevelModel} to retrieve
	 * @param bin
	 * 		The bin code of the specific {@link StockLevelModel} to retrieve
	 * @return newly created {@link StockLevelModel}
	 */
	StockLevelModel createStockLevel(String productCode, WarehouseModel warehouse, int initialQuantityOnHand, InStockStatus status,
			Date releaseDate, String bin);

	/**
	 * Retrieves a unique {@link StockLevelModel}.
	 *
	 * @param productCode
	 * 		The product code for which we want to retrieve a specific {@link StockLevelModel} (mandatory).
	 * @param warehouseCode
	 * 		The warehouse code for which we want to retrieve a specific {@link StockLevelModel} (mandatory).
	 * @param binCode
	 * 		The bin code of the specific {@link StockLevelModel} to retrieve (optional).
	 * @param releaseDate
	 * 		The release date of the specific {@link StockLevelModel} to retrieve (optional).
	 * @return a targeted stock level.
	 */
	StockLevelModel getUniqueStockLevel(final String productCode, final String warehouseCode, final String binCode,
			final Date releaseDate);
}
