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

import java.util.ArrayList;
import java.util.List;


/**
 * Default implementation of {@link EntriesSelectionStrategy}.
 *
 */
public class DefaultEntriesSelectionStrategy extends AbstractEntriesSelectionStrategy
{

	/**
	 * Gets not ordered list of Order Entries from the strategy.
	 */
	@Override
	protected List<OrderEntryRAO> getOrderEntriesToProcess(final EntriesSelectionStrategyRPD strategy)
	{
		return new ArrayList<>(strategy.getOrderEntries());
	}

}
