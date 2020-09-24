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
package de.hybris.platform.warehousing.atp.dao;

import java.util.Map;


/**
 * The available to promise Dao
 */
public interface AvailableToPromiseDao
{
	/**
	 * Get the quantity in stock of a certain product for a collection of stock levels
	 * (excluding availability in the external warehouses)
	 *
	 * @param params
	 *           the parameters required to perform the search
	 * @return the quantity of product in stock
	 */
	Long getAvailabilityForStockLevels(Map<String, Object> params);

	/**
	 * Get the allocated quantity of a certain product for a collection of stock levels
	 *
	 * @param params
	 *           the parameters required to perform the search
	 * @return the quantity of product allocated
	 */
	Long getAllocationQuantityForStockLevels(Map<String, Object> params);

	/**
	 * Get the cancelled quantity of a certain product for a collection of stock levels
	 *
	 * @param params
	 *           the parameters required to perform the search
	 * @return the quantity of product cancelled
	 */
	Long getCancellationQuantityForStockLevels(Map<String, Object> params);

	/**
	 * Get the reserved quantity of a certain product for a collection of stock levels
	 *
	 * @param params
	 *           the parameters required to perform the search
	 * @return the quantity of product in reserve
	 */
	Long getReservedQuantityForStockLevels(Map<String, Object> params);


	/**
	 * Get the Shrinkage quantity of a certain product for a collection of stock levels
	 *
	 * @param params
	 *           the parameters required to perform the search
	 * @return the quantity of shrinkage product that have shrinkage event
	 */
	Long getShrinkageQuantityForStockLevels(Map<String, Object> params);


	/**
	 * Get the Wastage quantity of a certain product for a collection of stock levels
	 *
	 * @param params
	 *           the parameters required to perform the search
	 * @return the quantity of Wastage product that have Wastage event
	 */
	Long getWastageQuantityForStockLevels(Map<String, Object> params);

	/**
	 *  Get the Increased quantity of a certain product for a collection of stock levels
	 * @param params
	 *           the parameters required to perform the search
	 * @return the quantity of added product that have increase event
	 */
	Long getIncreaseQuantityForStockLevels(Map<String, Object> params);
	
}
