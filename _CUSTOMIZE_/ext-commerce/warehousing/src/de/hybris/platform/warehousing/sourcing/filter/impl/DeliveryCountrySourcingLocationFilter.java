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

import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.warehousing.warehouse.service.WarehousingWarehouseService;

import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * This filter returns a set of sourcing locations situated in the country specified in the order delivery address.
 */
public class DeliveryCountrySourcingLocationFilter extends AbstractBaseSourcingLocationFilter
{
	private static Logger LOGGER = LoggerFactory.getLogger(DeliveryCountrySourcingLocationFilter.class);

	private WarehousingWarehouseService warehousingWarehouseService;

	@Override
	public Collection<WarehouseModel> applyFilter(final AbstractOrderModel order, final Set<WarehouseModel> locations)
	{
		if (order.getDeliveryAddress() != null && order.getDeliveryAddress().getCountry() != null && order.getStore() != null)
		{
			final CountryModel country = order.getDeliveryAddress().getCountry();
			final Collection<WarehouseModel> result = getWarehousingWarehouseService().getWarehousesByBaseStoreDeliveryCountry(
					order.getStore(), country);
			LOGGER.debug("Filter '{}' found '{}' warehouses.", getClass().getSimpleName(), result.size());
			return result;
		}
		else
		{
			// no delivery address available for pickup order or no base store for order
			// skip applying this filter
			return locations;
		}
	}

	protected WarehousingWarehouseService getWarehousingWarehouseService()
	{
		return warehousingWarehouseService;
	}

	@Required
	public void setWarehousingWarehouseService(final WarehousingWarehouseService warehousingWarehouseService)
	{
		this.warehousingWarehouseService = warehousingWarehouseService;
	}

}
