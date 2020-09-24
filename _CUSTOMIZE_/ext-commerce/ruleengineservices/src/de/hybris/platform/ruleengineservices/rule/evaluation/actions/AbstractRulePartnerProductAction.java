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
package de.hybris.platform.ruleengineservices.rule.evaluation.actions;

import static java.util.Objects.nonNull;

import de.hybris.platform.ruleengineservices.enums.OrderEntrySelectionStrategy;
import de.hybris.platform.ruleengineservices.rao.EntriesSelectionStrategyRPD;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.rule.evaluation.RuleActionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.assertj.core.util.Lists;


public class AbstractRulePartnerProductAction extends AbstractRuleExecutableSupport
{
	protected List<EntriesSelectionStrategyRPD> createSelectionStrategyRPDsQualifyingProducts(final RuleActionContext context,
			final OrderEntrySelectionStrategy selectionStrategy, final Map<String, Integer> qualifyingProductsContainers)
	{

		final List<EntriesSelectionStrategyRPD> entriesSelectionStrategyRPDs = Lists.newArrayList();
		if (nonNull(qualifyingProductsContainers))
		{
			for (final Entry<String, Integer> entry : qualifyingProductsContainers.entrySet())
			{
				final Set<OrderEntryRAO> orderEntries = getOrderEntries(context, entry);
				entriesSelectionStrategyRPDs
						.add(createSelectionStrategyRPD(selectionStrategy, entry.getValue(), orderEntries, false));
			}
		}
		return entriesSelectionStrategyRPDs;
	}

	protected List<EntriesSelectionStrategyRPD> createSelectionStrategyRPDsTargetProducts(final RuleActionContext context,
			final OrderEntrySelectionStrategy selectionStrategy, final Map<String, Integer> targetProductsContainers)
	{

		final List<EntriesSelectionStrategyRPD> entriesSelectionStrategyRPDs = Lists.newArrayList();
		if (targetProductsContainers != null)
		{
			for (final Entry<String, Integer> entry : targetProductsContainers.entrySet())
			{
				final Set<OrderEntryRAO> orderEntries = getOrderEntries(context, entry);
				entriesSelectionStrategyRPDs.add(createSelectionStrategyRPD(selectionStrategy, entry.getValue(), orderEntries, true));
			}
		}
		return entriesSelectionStrategyRPDs;
	}

	protected Set<OrderEntryRAO> getOrderEntries(final RuleActionContext context, final Entry<String, Integer> entry)
	{
		final String conditionsContainer = entry.getKey();
		return context.getValues(OrderEntryRAO.class, conditionsContainer);
	}

	protected EntriesSelectionStrategyRPD createSelectionStrategyRPD(final OrderEntrySelectionStrategy selectionStrategy,
			final Integer quantity, final Set<OrderEntryRAO> orderEntries, final boolean isAction)
	{
		final EntriesSelectionStrategyRPD selectionStrategyRPD = new EntriesSelectionStrategyRPD();
		selectionStrategyRPD.setSelectionStrategy(selectionStrategy);
		selectionStrategyRPD.setOrderEntries(new ArrayList<>(orderEntries));
		selectionStrategyRPD.setQuantity(quantity.intValue());
		selectionStrategyRPD.setTargetOfAction(isAction);
		return selectionStrategyRPD;
	}
}
