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
package de.hybris.platform.stock.impl;

import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.internal.dao.Dao;

import java.util.Collection;


/**
 * The {@link StockLevelModel} DAO.
 * 
 */
public interface StockLevelDao extends Dao
{

	/**
	 * Finds the stock level of the specified product at the specified warehouse.
	 * 
	 * @param productCode
	 *           the product code
	 * @param warehouse
	 *           warehouse of the product
	 * @return found stock level, and null if no such stock level can be found.
	 * @throws IllegalArgumentException
	 *            if either productCode or warehouse is null
	 */
	StockLevelModel findStockLevel(String productCode, WarehouseModel warehouse);

	/**
	 * Finds the stock levels of the specified product from all warehouses.
	 * 
	 * @param productCode
	 *           the product code
	 * @return all found stock levels of product
	 * @throws IllegalArgumentException
	 *            if productCode is null
	 */
	Collection<StockLevelModel> findAllStockLevels(final String productCode);

	/**
	 * Finds all stock levels of the specified product in the specified warehouses.
	 * 
	 * @param productCode
	 *           the product code
	 * @param warehouses
	 *           the warehouses
	 * @return found stock levels of the product
	 * @throws IllegalArgumentException
	 *            if either productCode or warehouses is null
	 */
	Collection<StockLevelModel> findStockLevels(final String productCode, final Collection<WarehouseModel> warehouses);

	Collection<StockLevelModel> findStockLevels(final String productCode, final Collection<WarehouseModel> warehouses,
			final int preOrderQuantity);

	Integer getAvailableQuantity(final WarehouseModel warehouse, final String productCode);

	/**
	 * Reserves the stock level with the amount. NOTE: direct database reservation with jdbc query must be used.
	 * 
	 * @param stockLevel
	 *           the stock level to be reserved
	 * @param amount
	 *           the amount of the reservation
	 * @return the actual stock level reserved amount after successful reservation, or NULL if reservation fails
	 */
	Integer reserve(StockLevelModel stockLevel, int amount);

	/**
	 * Releases the stock level with the amount. NOTE: direct database reservation with jdbc query must be used.
	 * 
	 * @param stockLevel
	 *           the stock level to be released
	 * @param amount
	 *           the amount of the release
	 * @return the actual stock level reserved amount after successful release, or NULL if release fails
	 */
	Integer release(StockLevelModel stockLevel, int amount);

	/**
	 * Updates the actual stock level with the actual amount. NOTE: direct database reservation with jdbc query must be
	 * used.
	 * 
	 * @param stockLevel
	 *           the stock level to be updated
	 * 
	 * @param actualAmount
	 *           the actual amount of the stock level
	 */
	void updateActualAmount(StockLevelModel stockLevel, int actualAmount);

}
