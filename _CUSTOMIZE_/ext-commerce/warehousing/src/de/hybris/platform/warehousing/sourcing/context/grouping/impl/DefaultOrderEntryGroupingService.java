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
package de.hybris.platform.warehousing.sourcing.context.grouping.impl;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.warehousing.sourcing.context.grouping.OrderEntryGroup;
import de.hybris.platform.warehousing.sourcing.context.grouping.OrderEntryGroupingService;
import de.hybris.platform.warehousing.sourcing.context.grouping.OrderEntryMatcher;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


/**
 * Default order entry grouping implementation.
 */
public class DefaultOrderEntryGroupingService implements OrderEntryGroupingService
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultOrderEntryGroupingService.class);

	@Override
	public Set<OrderEntryGroup> splitOrderByMatchers(final AbstractOrderModel order, final Collection<OrderEntryMatcher<?>> matchers)
	{
		Preconditions.checkArgument(matchers != null, "Matchers collection cannot be null.");

		LOGGER.debug("Start creating order entry groups");

		Set<OrderEntryGroup> finalSet = new HashSet<>();
		final OrderEntryGroup initialGroup = new OrderEntryGroup(excludeCompletedEntries(order));
		finalSet.add(initialGroup);

		for (final OrderEntryMatcher<?> matcher : matchers)
		{
			LOGGER.debug("Splitting order entry groups using matcher '{}'", matcher.getClass().getSimpleName());
			finalSet = splitGroupsByMatcher(finalSet, matcher);
		}

		return finalSet;
	}

	@Override
	public Set<OrderEntryGroup> splitGroupsByMatcher(final Set<OrderEntryGroup> groups, final OrderEntryMatcher<?> matcher)
	{
		final Set<OrderEntryGroup> result = groups.stream().map(group -> splitGroupByMatcher(group, matcher)).flatMap(Collection::stream)
				.collect(Collectors.toSet());

		return (result == null ? new HashSet<>() : result);
	}

	@Override
	public Set<OrderEntryGroup> splitGroupByMatcher(final OrderEntryGroup group, final OrderEntryMatcher<?> matcher)
	{
		final Map<Object, OrderEntryGroup> mapper = new HashMap<>();

		for (final AbstractOrderEntryModel orderEntry : group.getEntries())
		{
			final OrderEntryGroup mapEntry = mapper.get(matcher.getMatchingObject(orderEntry));
			if (mapEntry == null)
			{
				mapper.put(matcher.getMatchingObject(orderEntry), new OrderEntryGroup(Sets.newHashSet(orderEntry)));
			}
			else
			{
				mapEntry.add(orderEntry);
			}
		}

		return Sets.newHashSet(mapper.values());
	}


	/**
	 * Excludes the orderEntries from being grouped, if the there is nothing to be sourced in those orderEntries
	 * @param order
	 * 	the order containing the orderEntries to be excluded
	 * @return list of orderEntries which needs to be grouped for sourcing
	 */
	protected List<AbstractOrderEntryModel> excludeCompletedEntries(final AbstractOrderModel order)
	{
		return order.getEntries().stream().filter(orderEntry -> ((OrderEntryModel)orderEntry).getQuantityUnallocated().longValue() > 0)
				.collect(Collectors.toList());
	}
}
