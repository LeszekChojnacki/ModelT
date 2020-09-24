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

import de.hybris.platform.ordersplitting.model.WarehouseModel;

import java.util.Date;
import java.util.Map;


/**
 * Strategy for 'calculating' the "best" product "availability" or "quantity" of a certain product .
 */
public interface BestMatchStrategy
{
	/**
	 * Returns the warehouse which offers the "best" product "quantity".
	 * 
	 * @param map
	 *           the mapped quantities of a certain product
	 * @return WarehouseModel best match
	 */
	WarehouseModel getBestMatchOfQuantity(final Map<WarehouseModel, Integer> map);

	/**
	 * Returns the warehouse which offers the "best" product "availability" .
	 * 
	 * @param map
	 *           the mapped available dates of a certain product
	 * @return WarehouseModel best match
	 */
	WarehouseModel getBestMatchOfAvailability(final Map<WarehouseModel, Date> map);

}
