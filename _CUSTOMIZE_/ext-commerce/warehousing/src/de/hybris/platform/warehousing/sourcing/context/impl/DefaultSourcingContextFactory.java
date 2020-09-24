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
package de.hybris.platform.warehousing.sourcing.context.impl;

import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.warehousing.data.sourcing.SourcingContext;
import de.hybris.platform.warehousing.data.sourcing.SourcingLocation;
import de.hybris.platform.warehousing.data.sourcing.SourcingResults;
import de.hybris.platform.warehousing.sourcing.context.SourcingContextFactory;
import de.hybris.platform.warehousing.sourcing.context.grouping.OrderEntryGroup;
import de.hybris.platform.warehousing.sourcing.context.populator.SourcingLocationPopulator;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 * Factory used for creating a {@link SourcingContext}.
 */
public class DefaultSourcingContextFactory implements SourcingContextFactory, InitializingBean
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSourcingContextFactory.class);

	private Set<SourcingLocationPopulator> sourcingLocationPopulators;

	@Override
	public Collection<SourcingContext> create(final Collection<OrderEntryGroup> groups, final Collection<WarehouseModel> locations)
			throws IllegalArgumentException
	{
		final Collection<SourcingContext> contexts = Lists.newArrayList();

		if (CollectionUtils.isEmpty(groups))
		{
			LOGGER.info("No order groups to source.");
			return contexts;
		}
		if (CollectionUtils.isEmpty(locations))
		{
			LOGGER.info("No sourcing locations found for sourcing order groups.");
			return contexts;
		}

		for (final OrderEntryGroup group : groups)
		{
			final SourcingContext context = new SourcingContext();
			final SourcingResults results = new SourcingResults();
			results.setResults(Sets.newHashSet());
			results.setComplete(Boolean.FALSE);
			context.setResult(results);
			context.setOrderEntries(Lists.newArrayList(group.getEntries()));

			final Set<SourcingLocation> sourcingLocations = Sets.newHashSet();
			locations.forEach(location -> sourcingLocations.add(createSourcingLocation(context, location)));
			context.setSourcingLocations(sourcingLocations);

			contexts.add(context);
		}

		return contexts;
	}

	/**
	 * Create a new sourcing location and populate it.
	 *
	 * @param context
	 *           - the sourcing context
	 * @param location
	 *           - the warehouse model
	 * @return the sourcing location; never <tt>null</tt>
	 */
	protected SourcingLocation createSourcingLocation(final SourcingContext context, final WarehouseModel location)
	{
		final SourcingLocation sourcingLocation = new SourcingLocation();
		sourcingLocation.setWarehouse(location);
		sourcingLocation.setContext(context);
		getSourcingLocationPopulators().forEach(populator -> populator.populate(location, sourcingLocation));
		return sourcingLocation;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		// Make sure that we have some populators registered
		if (CollectionUtils.isEmpty(getSourcingLocationPopulators()))
		{
			throw new IllegalArgumentException("Sourcing location populators cannot be empty.");
		}
	}

	protected Set<SourcingLocationPopulator> getSourcingLocationPopulators()
	{
		return sourcingLocationPopulators;
	}

	@Required
	public void setSourcingLocationPopulators(final Set<SourcingLocationPopulator> populators)
	{
		this.sourcingLocationPopulators = populators;
	}

}
