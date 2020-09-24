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

import de.hybris.platform.ruleengineservices.rao.EntriesSelectionStrategyRPD;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Defines method(s) to pickup the cheapest products from set of Order Entries to be used in a rule for a group of
 * products.
 *
 */
public class CheapestEntriesSelectionStrategy extends AbstractEntriesSelectionStrategy
{
	/**
	 * Gets ordered list of Order Entries from strategy according to product prices comparing OrderEntryRAO.price -
	 * the cheapest first.
	 */
	@Override
	protected List<OrderEntryRAO> getOrderEntriesToProcess(final EntriesSelectionStrategyRPD strategy)
	{
		return strategy.getOrderEntries().stream().sorted(Comparator.comparing(OrderEntryRAO::getPrice))
				.collect(Collectors.toList());
	}
}
