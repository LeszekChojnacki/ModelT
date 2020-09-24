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
package de.hybris.platform.warehousing.sourcing.result.impl;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.warehousing.data.sourcing.SourcingLocation;
import de.hybris.platform.warehousing.data.sourcing.SourcingResult;
import de.hybris.platform.warehousing.data.sourcing.SourcingResults;
import de.hybris.platform.warehousing.sourcing.result.SourcingResultFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Factory used for creating a {@link SourcingResult}.
 */
public class DefaultSourcingResultFactory implements SourcingResultFactory
{

	@Override
	public SourcingResult create(final AbstractOrderEntryModel orderEntry, final SourcingLocation sourcingLocation,
			final Long quantity)
	{
		final SourcingResult sourcingResult = new SourcingResult();
		final Map<AbstractOrderEntryModel, Long> allocation = new HashMap<>();
		if (quantity.longValue() > 0)
		{
			allocation.put(orderEntry, quantity);
		}
		sourcingResult.setAllocation(allocation);
		sourcingResult.setWarehouse(sourcingLocation.getWarehouse());
		return sourcingResult;
	}

	@Override
	public SourcingResult create(final Collection<AbstractOrderEntryModel> orderEntries, final SourcingLocation sourcingLocation)
	{

		final SourcingResult sourcingResult = new SourcingResult();
		final Map<AbstractOrderEntryModel, Long> allocation = new HashMap<>();
		orderEntries.stream().filter(entry -> ((OrderEntryModel) entry).getQuantityUnallocated().longValue() > 0)
				.forEach(entry -> allocation.put(entry, ((OrderEntryModel) entry).getQuantityUnallocated()));
		sourcingResult.setAllocation(allocation);
		sourcingResult.setWarehouse(sourcingLocation.getWarehouse());
		return sourcingResult;
	}

	@Override
	public SourcingResult create(final Map<AbstractOrderEntryModel, Long> allocations, final SourcingLocation sourcingLocation)
	{
		final SourcingResult sourcingResult = new SourcingResult();
		sourcingResult.setAllocation(Maps.newHashMap(allocations));
		sourcingResult.setWarehouse(sourcingLocation.getWarehouse());
		return sourcingResult;
	}

	@Override
	public SourcingResults create(final Collection<SourcingResults> results)
	{
		final SourcingResults target = new SourcingResults();
		target.setResults(Sets.newHashSet());
		target.setComplete(!results.isEmpty() && !results.stream().anyMatch(result -> !result.isComplete()));
		results.forEach(source -> mergeResults(source, target));
		return target;
	}

	/**
	 * Merges the sourcing results of the source and target results containers into the target container.</br> This
	 * implementation will set the collections in the target as type {@link java.util.HashSet} and merges the collections
	 * using {@link Iterables#concat(Iterable, Iterable)}.
	 *
	 * @param source
	 *           - the source container
	 * @param target
	 *           - the target container
	 */
	protected void mergeResults(final SourcingResults source, final SourcingResults target)
	{
		target.setResults(Sets.newHashSet(Iterables.concat(source.getResults(), target.getResults())));
	}
}
