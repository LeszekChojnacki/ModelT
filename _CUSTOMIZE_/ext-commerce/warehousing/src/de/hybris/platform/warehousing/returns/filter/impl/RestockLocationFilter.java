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
package de.hybris.platform.warehousing.returns.filter.impl;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.warehousing.sourcing.filter.impl.AbstractBaseSourcingLocationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * This filter returns a set of locations that takes into consideration the if the warehouse allows restock (if any)
 */
public class RestockLocationFilter extends AbstractBaseSourcingLocationFilter
{
	private static Logger LOGGER = LoggerFactory.getLogger(RestockLocationFilter.class);


	@Override
	public Collection<WarehouseModel> applyFilter(final AbstractOrderModel order, final Set<WarehouseModel> locations)
	{
		ServicesUtil.validateParameterNotNull(locations, "Parameter locations cannot be null.");
		LOGGER.debug("Filter '{}' found '{}' warehouses.", getClass().getSimpleName(), locations.size());
		return locations.stream().filter(warehouse -> Boolean.TRUE.equals(warehouse.getIsAllowRestock())).collect(Collectors.toList());
	}
}
