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
package de.hybris.platform.warehousing.sourcing.filter;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;



/**
 * This is a utility class that filters locations according to a collection of filters.
 */
public class SourcingFilterProcessor implements InitializingBean
{
	private static final Logger LOGGER = LoggerFactory.getLogger(SourcingFilterProcessor.class);

	private Collection<SourcingLocationFilter> filters;

	/**
	 * Start the filter chain execution and returns a set of locations to be used for sourcing.
	 *
	 * @param order
	 *           - Order to be sourced (input value); cannot be <tt>null</tt>
	 * @param locations
	 *           - Set of sourcing locations (output value); cannot be <tt>null</tt>
	 * @throws IllegalArgumentException
	 *            when order and/or locations parameters are null
	 * @throws IllegalStateException
	 *            when no filter is set
	 */
	public void filterLocations(final AbstractOrderModel order, final Set<WarehouseModel> locations)
			throws IllegalArgumentException, IllegalStateException
	{
		if (order == null || locations == null)
		{
			throw new IllegalArgumentException("Parameters order and locations cannot be null");
		}

		if (getFilters() == null || getFilters().isEmpty())
		{
			throw new IllegalStateException("At least one sourcing filter must be specified");
		}

		// start filtering process
		LOGGER.debug("Start filtering locations");
		for (final SourcingLocationFilter filter : getFilters())
		{
			filter.filterLocations(order, locations);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		if (CollectionUtils.isEmpty(getFilters()))
		{
			throw new IllegalArgumentException("Filters collection cannot be empty.");
		}
	}

	protected Collection<SourcingLocationFilter> getFilters()
	{
		return filters;
	}

	/**
	 * Set the list of filters to be processed (in FIFO order).
	 *
	 * @param filters
	 *           - ordered list of filters to be processed; cannot be <tt>null</tt> or empty
	 */
	public void setFilters(final Collection<SourcingLocationFilter> filters)
	{
		this.filters = filters;
	}
}
