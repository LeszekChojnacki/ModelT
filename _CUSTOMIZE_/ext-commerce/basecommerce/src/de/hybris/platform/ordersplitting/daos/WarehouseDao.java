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
package de.hybris.platform.ordersplitting.daos;

import de.hybris.platform.ordersplitting.model.VendorModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.internal.dao.GenericDao;

import java.util.List;


/**
 *
 */
public interface WarehouseDao extends GenericDao<WarehouseModel>
{
	/**
	 * Return list of warehouses with a specified code
	 * 
	 * @param code
	 *           code for search
	 * @return warehouses
	 */
	List<WarehouseModel> getWarehouseForCode(String code);


	/**
	 * Return list of warehouses with are marked as default
	 * 
	 * @return warehouses
	 */
	List<WarehouseModel> getDefWarehouse();

	/**
	 * Return list of warehouses that have stock levels for given product code.
	 * 
	 * @param productCode
	 *           queried entry
	 * @return list of warehouses
	 */
	List<WarehouseModel> getWarehouses(final String productCode);


	/**
	 * Return list of warehouses that have stock level with quantity greater or equal to quantity. If vandor parameter is
	 * set it also filter the results
	 * 
	 * @param productCode
	 *           code of product
	 * @param quantity
	 *           quantity
	 * @param vendor
	 *           vendor
	 * @return list of warehouses
	 */
	List<WarehouseModel> getWarehousesWithProductsInStock(final String productCode, long quantity, VendorModel vendor);
}
