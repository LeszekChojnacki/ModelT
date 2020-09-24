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
package de.hybris.platform.ruleengineservices.calculation.impl;

import de.hybris.platform.ruleengineservices.calculation.EntriesSelectionStrategy;
import de.hybris.platform.ruleengineservices.rao.EntriesSelectionStrategyRPD;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;


/**
 * Default implementation of {@link EntriesSelectionStrategy}.
 *
 */
public abstract class AbstractEntriesSelectionStrategy implements EntriesSelectionStrategy
{
	@Override
	public Map<Integer, Integer> pickup(final EntriesSelectionStrategyRPD strategy,
			final Map<Integer, Integer> consumableQuantities)
	{
		final Map<Integer, Integer> result = Maps.newHashMap();

		int itemsToConsume = strategy.getQuantity();

		for (final OrderEntryRAO orderEntry : getOrderEntriesToProcess(strategy))
		{
			Integer consumableQuantity = consumableQuantities.get(orderEntry.getEntryNumber());

			if (Objects.isNull(consumableQuantity))
			{
				consumableQuantity = orderEntry.getQuantity();
			}

			if (itemsToConsume > 0)
			{
				int applicableUnits;
				if (itemsToConsume >= consumableQuantity)
				{
					applicableUnits = consumableQuantity;
					itemsToConsume -= consumableQuantity;
				}
				else
				{
					applicableUnits = itemsToConsume;
					itemsToConsume = 0;
				}
				result.put(orderEntry.getEntryNumber(), Integer.valueOf(applicableUnits));
			}
			else
			{
				break;
			}
		}

		if (itemsToConsume > 0)
		{
			throw new IllegalArgumentException("The Order Entries have less units than required to pickup.");
		}

		return result;
	}

	/**
	 * Provides ordered list of Order Entries
	 */
	protected abstract List<OrderEntryRAO> getOrderEntriesToProcess(final EntriesSelectionStrategyRPD strategy);


	@Override
	@Deprecated
	public Map<Integer, Integer> pickup(final EntriesSelectionStrategyRPD strategy) // NOSONAR
	{
		final Map<Integer, Integer> result = Maps.newHashMap();
		int itemsToConsume = strategy.getQuantity();
		for (final OrderEntryRAO orderEntry : getOrderEntriesToProcess(strategy))
		{
			if (itemsToConsume > 0)
			{
				int applicableUnits;
				if (itemsToConsume >= orderEntry.getQuantity())
				{
					applicableUnits = orderEntry.getQuantity();
					itemsToConsume -= orderEntry.getQuantity();
				}
				else
				{
					applicableUnits = itemsToConsume;
					itemsToConsume = 0;
				}
				result.put(orderEntry.getEntryNumber(), Integer.valueOf(applicableUnits));
			}
			else
			{
				break;
			}
		}
		if (itemsToConsume > 0)
		{
			throw new IllegalArgumentException("The Order Entries have less units than required to pickup.");
		}
		return result;
	}
}
