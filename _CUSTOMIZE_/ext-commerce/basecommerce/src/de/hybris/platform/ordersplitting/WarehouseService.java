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
package de.hybris.platform.ordersplitting;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;

import java.util.Collection;
import java.util.List;


/**
 * The service is used to fetch warehouse for order entries.
 */
public interface WarehouseService
{

	/**
	 * Return list of warehouses that can be used to create order from order entry.
	 *
	 * @param orderEntry
	 * 		queried order entry
	 * @return list of warehouses
	 */
	List<WarehouseModel> getWarehousesWithProductsInStock(AbstractOrderEntryModel orderEntry);

	/**
	 * Return list of warehouses that can be used to create order from order entries.
	 *
	 * @param orderEntries
	 * 		queried list
	 * @return list of warehouses
	 */
	List<WarehouseModel> getWarehouses(Collection<? extends AbstractOrderEntryModel> orderEntries);

	/**
	 * Return the warehouse with for the code
	 *
	 * @param code
	 * 		code of warehouse to search.
	 * @return instance of {@link WarehouseModel}, {@link RuntimeException} is thrown otherwise
	 */
	WarehouseModel getWarehouseForCode(String code);

	/**
	 * Gets warehouse that is marked as default.
	 *
	 * @return default warehouse
	 */
	List<WarehouseModel> getDefWarehouse();
}
