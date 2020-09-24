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
package de.hybris.platform.warehousing.atp.services.impl;

import de.hybris.platform.basecommerce.enums.StockLevelStatus;
import de.hybris.platform.commerceservices.stock.impl.DefaultCommerceStockService;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import de.hybris.platform.warehousing.atp.strategy.PickupWarehouseSelectionStrategy;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;


/**
 * OMS implementation of {@link de.hybris.platform.commerceservices.stock.CommerceStockService}
 */
public class WarehousingCommerceStockService extends DefaultCommerceStockService
{
	private PickupWarehouseSelectionStrategy pickupWarehouseSelectionStrategy;

	@Override
	public Long getStockLevelForProductAndPointOfService(final ProductModel product, final PointOfServiceModel pos)
	{
		validateParameterNotNull(product, "Parameter product cannot be null");
		validateParameterNotNull(pos, "Parameter point of service cannot be null");

		final Collection<WarehouseModel> pickupWarehouses = getPickupWarehouseSelectionStrategy().getWarehouses(pos);

		final Collection<StockLevelModel> stockLevels = getStockService().getStockLevels(product, pickupWarehouses);

		if (stockLevels == null)
		{
			return Long.valueOf(0);
		}

		return getCommerceStockLevelCalculationStrategy().calculateAvailability(stockLevels);
	}

	@Override
	public StockLevelStatus getStockLevelStatusForProductAndPointOfService(final ProductModel product,
			final PointOfServiceModel pointOfService)
	{
		validateParameterNotNull(product, "product cannot be null");
		validateParameterNotNull(pointOfService, "pointOfService cannot be null");

		final Collection<WarehouseModel> pickupWarehouses = getPickupWarehouseSelectionStrategy().getWarehouses(pointOfService);

		if (pointOfService.getWarehouses().isEmpty())
		{
			return StockLevelStatus.OUTOFSTOCK;
		}

		return getStockService().getProductStatus(product, pickupWarehouses);
	}

	protected PickupWarehouseSelectionStrategy getPickupWarehouseSelectionStrategy()
	{
		return pickupWarehouseSelectionStrategy;
	}

	@Required
	public void setPickupWarehouseSelectionStrategy(final PickupWarehouseSelectionStrategy pickupWarehouseSelectionStrategy)
	{
		this.pickupWarehouseSelectionStrategy = pickupWarehouseSelectionStrategy;
	}
}
