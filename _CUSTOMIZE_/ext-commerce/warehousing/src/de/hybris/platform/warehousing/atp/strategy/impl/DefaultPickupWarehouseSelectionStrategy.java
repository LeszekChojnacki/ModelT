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
package de.hybris.platform.warehousing.atp.strategy.impl;

import de.hybris.platform.commerceservices.model.PickUpDeliveryModeModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import de.hybris.platform.warehousing.warehouse.filter.WarehousesFilterProcessor;
import de.hybris.platform.warehousing.atp.strategy.PickupWarehouseSelectionStrategy;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default strategy implementation to get all warehouses associated to a given point of service which support pickup as
 * delivery mode.
 */
public class DefaultPickupWarehouseSelectionStrategy implements PickupWarehouseSelectionStrategy
{
	protected static final String CODE_PICKUP = "pickup";

	private WarehousesFilterProcessor warehousesFilterProcessor;

	@Override
	public Collection<WarehouseModel> getWarehouses(final PointOfServiceModel pos)
	{
		ServicesUtil.validateParameterNotNull(pos, "point of service cannot be null");

		Set<WarehouseModel> pickupWarehouses = null;
		if (Objects.nonNull(pos.getWarehouses()))
		{
			pickupWarehouses = pos
					.getWarehouses()
					.stream()
					.filter(warehouse -> Objects.nonNull(warehouse.getDeliveryModes()))
					.filter(
							warehouse -> warehouse.getDeliveryModes().stream()
									.anyMatch(
											deliveryMode -> deliveryMode instanceof PickUpDeliveryModeModel
													|| CODE_PICKUP.equals(deliveryMode.getCode())))
					.collect(Collectors.toSet());

			pickupWarehouses = getWarehousesFilterProcessor().filterLocations(pickupWarehouses);
		}
		return Objects.isNull(pickupWarehouses) ? Collections.emptySet() : pickupWarehouses;
	}

	protected WarehousesFilterProcessor getWarehousesFilterProcessor()
	{
		return warehousesFilterProcessor;
	}

	@Required
	public void setWarehousesFilterProcessor(final WarehousesFilterProcessor warehousesFilterProcessor)
	{
		this.warehousesFilterProcessor = warehousesFilterProcessor;
	}
}
