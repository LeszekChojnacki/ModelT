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
package de.hybris.platform.ruleengineservices.calculation;

import de.hybris.platform.ruleengineservices.rao.EntriesSelectionStrategyRPD;

import java.util.Map;


/**
 * Defines method(s) to pickup products to be used in a rule with a group of products.
 */
public interface EntriesSelectionStrategy
{
	/**
	 * Evaluates what Order Entries and how many units of each of them should be picked up to be used.
	 *
	 * @param strategy
	 * 		defines how many total units and from which Order Entries the ones should be picked up.
	 * @return Map with entries having orderEntry.entryNumber as a key and number of Items To Consumed as value
	 * @deprecated since 6.6
	 */
	@Deprecated
	Map<Integer, Integer> pickup(EntriesSelectionStrategyRPD strategy);                    // NOSONAR


	/**
	 * Evaluates what Order Entries and how many units of each of them should be picked up to be used.
	 *
	 * @param strategy
	 * 		defines how many total units and from which Order Entries the ones should be picked up.
	 * @param consumableQuantities
	 * 		map, containing the consumable order entry quantities
	 * @return Map with entries having orderEntry.entryNumber as a key and number of Items To Consumed as value
	 */
	Map<Integer, Integer> pickup(EntriesSelectionStrategyRPD strategy, Map<Integer, Integer> consumableQuantities);

}
