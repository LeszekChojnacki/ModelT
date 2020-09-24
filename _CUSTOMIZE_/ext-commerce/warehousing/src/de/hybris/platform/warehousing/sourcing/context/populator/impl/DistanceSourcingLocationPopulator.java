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

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import de.hybris.platform.warehousing.data.sourcing.SourcingLocation;
import de.hybris.platform.warehousing.sourcing.context.PosSelectionStrategy;
import de.hybris.platform.warehousing.sourcing.context.populator.SourcingLocationPopulator;
import de.hybris.platform.warehousing.sourcing.context.util.HaversineCalculator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Populate a sourcing location's distance to the delivery address.
 */
public class DistanceSourcingLocationPopulator implements SourcingLocationPopulator
{
	private static Logger LOG = LoggerFactory.getLogger(DistanceSourcingLocationPopulator.class);

	private PosSelectionStrategy posSelectionStrategy;

	@Override
	public void populate(final WarehouseModel source, final SourcingLocation target)
	{
		final AbstractOrderEntryModel orderEntry = target.getContext().getOrderEntries().iterator().next();

		final AddressModel deliveryAddress = orderEntry.getOrder().getDeliveryAddress();
		if (deliveryAddress == null)
		{
			LOG.info("Order did not have a delivery address; setting target distance to null.");
			target.setDistance(null);
			return;
		}

		final PointOfServiceModel pointOfService = getPosSelectionStrategy().getPointOfService(orderEntry.getOrder(), source);
		if (pointOfService == null)
		{
			LOG.info("Warehouse did not have a valid point of service; setting target distance to null.");
			target.setDistance(null);
			return;
		}
		if (pointOfService.getAddress() == null)
		{
			LOG.info("Point of service did not have an address defined; setting target distance to null.");
			target.setDistance(null);
			return;
		}

		final Double orderLat = deliveryAddress.getLatitude();
		final Double orderLon = deliveryAddress.getLongitude();
		final Double posLat = pointOfService.getLatitude();
		final Double posLon = pointOfService.getLongitude();

		if (orderLat == null || orderLon == null)
		{
			LOG.info("Order did not have lat/long value; setting target distance to null.");
			target.setDistance(null);
		}
		else if (posLat == null || posLon == null)
		{
			LOG.info("PointOfServiceModel with [name: {}] did not have lat/long value; setting target distance to null.",
					source.getCode());
			target.setDistance(null);
		}
		else
		{
			target.setDistance(HaversineCalculator.calculate(posLat, posLon, orderLat, orderLon));
		}
	}

	protected PosSelectionStrategy getPosSelectionStrategy()
	{
		return posSelectionStrategy;
	}

	@Required
	public void setPosSelectionStrategy(final PosSelectionStrategy posSelectionStrategy)
	{
		this.posSelectionStrategy = posSelectionStrategy;
	}

}
