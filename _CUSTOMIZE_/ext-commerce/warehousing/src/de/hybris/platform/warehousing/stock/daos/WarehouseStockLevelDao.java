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
 */
package de.hybris.platform.warehousing.stock.daos;


import de.hybris.platform.ordersplitting.model.StockLevelModel;

import java.util.Date;
import java.util.List;


/**
 * Data Access for looking up {@link StockLevelModel}.
 */
public interface WarehouseStockLevelDao
{
	/**
	 * Retrieves a specific {@link StockLevelModel}
	 *
	 * @param productCode
	 *           the product code of the stock level to retrieve (mandatory)
	 * @param warehouseCode
	 *           the warehouse code of the stock level to retrieve (mandatory)
	 * @param binCode
	 *           the bin code of the stock level to retrieve (optional)
	 * @param releaseDate
	 *           the release date of the stock level to retrieve (optional)
	 *
	 * @return the List {@link StockLevelModel} matching the request
	 */
	List<StockLevelModel> getStockLevels(String productCode, String warehouseCode, String binCode, Date releaseDate);

	/**
	 * Retrieves a specific future {@link StockLevelModel}
	 *
	 * @param productCode
	 *           the product code of the future stock level to retrieve (mandatory)
	 * @param warehouseCode
	 *           the warehouse code of the future stock level to retrieve (mandatory)
	 * @param binCode
	 *           the bin code of the future stock level to retrieve (optional)
	 *
	 * @return the List of future {@link StockLevelModel} matching the request
	 */
	List<StockLevelModel> getFutureStockLevels(String productCode, String warehouseCode, String binCode);
}

