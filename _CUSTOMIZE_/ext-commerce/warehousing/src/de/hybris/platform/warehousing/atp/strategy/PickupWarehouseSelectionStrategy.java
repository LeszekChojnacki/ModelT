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
package de.hybris.platform.warehousing.atp.strategy;

import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.storelocator.model.PointOfServiceModel;

import java.util.Collection;


/**
 * This strategy returns warehouses where pickup is supported.
 */
public interface PickupWarehouseSelectionStrategy
{
	/**
	 * Get all warehouses associated to the point of service that allow pickup.
	 *
	 * @param pos
	 *           - the point of service associated to the warehouses which allow pickup
	 * @return a list of warehouses for pickup
	 */
	Collection<WarehouseModel> getWarehouses(PointOfServiceModel pos);
}
