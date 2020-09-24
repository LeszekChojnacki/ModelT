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
package de.hybris.platform.warehousing.sourcing.filter.impl;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.warehousing.stock.services.impl.DefaultWarehouseStockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * This filter returns a set of sourcing locations where at least one of the item in the order has available stock.<br/>
 * It is NOT recommended to use this filter as the initial filter in the chain of filters due to performance reason.
 */
public class AvailabilitySourcingLocationFilter extends AbstractBaseSourcingLocationFilter
{
	private static Logger LOGGER = LoggerFactory.getLogger(AvailabilitySourcingLocationFilter.class);

	private DefaultWarehouseStockService warehouseStockService;

	@Override
	public Collection<WarehouseModel> applyFilter(final AbstractOrderModel order, final Set<WarehouseModel> warehouses)
	{
		final Collection<WarehouseModel> result = warehouses.stream()
				.filter(warehouse -> isWarehouseHasAvailabilityForAnyProductInOrder(order, warehouse))
				.collect(Collectors.toList());
		LOGGER.debug("Filter '{}' found '{}' warehouses.", getClass().getSimpleName(), result.size());
		return result;
	}

	/**
	 * Given an order and a warehouse, checks if the warehouse has some availability for at least one of the products in the order.
	 *
	 * @param order
	 *           - order for which to check the product stock level
	 * @param warehouse
	 *           - the warehouse to filter on stock availability
	 * @return true, if the warehouse has atleast one availability, for any product in the order
	 */
	protected boolean isWarehouseHasAvailabilityForAnyProductInOrder(final AbstractOrderModel order, final WarehouseModel warehouse)
	{
		return order.getEntries().stream().anyMatch(entry ->
		{
			final Long available =  getWarehouseStockService().getStockLevelForProductCodeAndWarehouse(entry.getProduct().getCode(), warehouse);
			return available == null || available.longValue() > 0;
		});
	}


	protected DefaultWarehouseStockService getWarehouseStockService()
	{
		return warehouseStockService;
	}

	@Required
	public void setWarehouseStockService(final DefaultWarehouseStockService warehouseStockService)
	{
		this.warehouseStockService = warehouseStockService;
	}
}
