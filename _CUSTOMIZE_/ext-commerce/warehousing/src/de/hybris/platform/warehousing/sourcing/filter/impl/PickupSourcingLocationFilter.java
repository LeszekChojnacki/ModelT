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
import de.hybris.platform.warehousing.atp.strategy.PickupWarehouseSelectionStrategy;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * This filter returns a set of sourcing locations that match all point of services selected as pickup locations.
 */
public class PickupSourcingLocationFilter extends AbstractBaseSourcingLocationFilter
{
	private static Logger LOGGER = LoggerFactory.getLogger(PickupSourcingLocationFilter.class);
	private PickupWarehouseSelectionStrategy pickupWarehouseSelectionStrategy;

	@Override
	public Collection<WarehouseModel> applyFilter(final AbstractOrderModel order, final Set<WarehouseModel> locations)
	{
		final Collection<WarehouseModel> result = order.getEntries().stream()
				.filter(entry -> entry.getDeliveryPointOfService() != null)
				.flatMap(entry -> getPickupWarehouseSelectionStrategy().getWarehouses(entry.getDeliveryPointOfService()).stream())
				.collect(Collectors.toSet());
		LOGGER.debug("Filter '{}' found '{}' warehouses.", getClass().getSimpleName(), result.size());
		return result;
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
