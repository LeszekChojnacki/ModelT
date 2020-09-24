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
package de.hybris.platform.warehousing.sourcing.context.populator.impl;

import com.google.common.base.Preconditions;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.warehousing.data.sourcing.SourcingLocation;
import de.hybris.platform.warehousing.sourcing.context.populator.SourcingLocationPopulator;
import de.hybris.platform.warehousing.stock.services.impl.DefaultWarehouseStockService;
import org.springframework.beans.factory.annotation.Required;

import java.util.HashMap;
import java.util.Map;


/**
 * Populate a sourcing location's availability for the items ordered.
 */
public class AvailabilitySourcingLocationPopulator implements SourcingLocationPopulator
{
	private DefaultWarehouseStockService warehouseStockService;

	@Override
	public void populate(final WarehouseModel source, final SourcingLocation target)
	{
		Preconditions.checkArgument(source != null, "Point of service model (source) cannot be null.");
		Preconditions.checkArgument(target != null, "Sourcing location (target) cannot be null.");

		final Map<ProductModel, Long> availability = new HashMap<>();
		target.getContext().getOrderEntries().forEach(entry -> setAvailability(source, entry, availability));
		target.setAvailability(availability);
	}

	/**
	 * Sets the product availability in the map by using the commerce stock service.
	 *
	 * @param source
	 *           - the warehouse model
	 * @param entry
	 *           - the order entry model
	 * @param availability
	 *           - the map containing the availability; the value is never <tt>null</tt>
	 */
	protected void setAvailability(final WarehouseModel source, final AbstractOrderEntryModel entry,
			final Map<ProductModel, Long> availability)
	{
		ProductModel product = entry.getProduct();
		Long stock = getWarehouseStockService().getStockLevelForProductCodeAndWarehouse(product.getCode(), source);

		//When stock is ForcedInStock
		if(stock == null)
		{
			stock = entry.getQuantity();
		}

		availability.put(product,stock);
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
