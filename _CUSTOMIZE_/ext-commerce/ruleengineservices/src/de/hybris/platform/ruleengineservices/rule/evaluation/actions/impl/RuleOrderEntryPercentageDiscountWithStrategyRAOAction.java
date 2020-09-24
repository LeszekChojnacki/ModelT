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
package de.hybris.platform.ruleengineservices.rule.evaluation.actions.impl;

import com.google.common.collect.Lists;
import de.hybris.platform.ruleengineservices.rao.DiscountRAO;
import de.hybris.platform.ruleengineservices.rao.EntriesSelectionStrategyRPD;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;
import de.hybris.platform.ruleengineservices.rule.evaluation.RuleActionContext;
import de.hybris.platform.ruleengineservices.rule.evaluation.actions.AbstractRulePartnerProductAction;
import de.hybris.platform.ruleengineservices.util.RAOConstants;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.hybris.platform.ruleengineservices.util.RAOConstants.VALUE_PARAM;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;


/**
 * @deprecated since 18.11
 */
@Deprecated
public class RuleOrderEntryPercentageDiscountWithStrategyRAOAction extends AbstractRulePartnerProductAction
{

	@Override
	public boolean performActionInternal(final RuleActionContext context)
	{
		final List<EntriesSelectionStrategyRPD> strategies = (List<EntriesSelectionStrategyRPD>) context
				.getParameter(RAOConstants.SELECTION_STRATEGY_RPDS_PARAM);

		return extractAmountForCurrency(context, context.getParameter(VALUE_PARAM)).map(
				amount -> performAction(context, strategies, amount)).orElse(false);
	}

	protected boolean performAction(final RuleActionContext context,
			final List<EntriesSelectionStrategyRPD> entriesSelectionStrategyRPDs, final BigDecimal amount)
	{
		boolean isPerformed = false;
		validateSelectionStrategy(entriesSelectionStrategyRPDs, context);

		if (hasEnoughQuantity(context, entriesSelectionStrategyRPDs))
		{
			isPerformed = true;

			if (!isConsumptionEnabled())
			{
				// if order entry consumption is turned off, the container
				// based actions need to consume "all in one go" like in previous versions
				adjustStrategyQuantity(entriesSelectionStrategyRPDs, -1);
			}

			final List<EntriesSelectionStrategyRPD> selectionStrategyRPDsForAction = Lists.newArrayList();
			final List<EntriesSelectionStrategyRPD> selectionStrategyRPDsForTriggering = Lists.newArrayList();

			splitEntriesSelectionStrategies(entriesSelectionStrategyRPDs, selectionStrategyRPDsForAction,
					selectionStrategyRPDsForTriggering);

			final List<DiscountRAO> discounts = addDiscountAndConsume(context, selectionStrategyRPDsForAction, false, amount);
			if (isNotEmpty(selectionStrategyRPDsForTriggering))
			{
				//Additional consumed entries need to be added to the action(discount). It is enough to add them to only one discount from created discounts.
				consumeOrderEntries(context, selectionStrategyRPDsForTriggering, discounts.isEmpty()?null:discounts.get(0));
				updateFactsWithOrderEntries(context, selectionStrategyRPDsForTriggering);
			}
		}
		return isPerformed;
	}

	protected void updateFactsWithOrderEntries(final RuleActionContext context,
			final List<EntriesSelectionStrategyRPD> entriesSelectionStrategyRPDS)
	{
		for (final EntriesSelectionStrategyRPD selectionStrategyRPDForTriggering : entriesSelectionStrategyRPDS)
		{
			for (final OrderEntryRAO orderEntryRao : selectionStrategyRPDForTriggering.getOrderEntries())
			{
				context.scheduleForUpdate(orderEntryRao);
			}
		}
	}

	protected List<DiscountRAO> addDiscountAndConsume(final RuleActionContext context,
			final List<EntriesSelectionStrategyRPD> selectionStrategies, final boolean absolute, final BigDecimal price)
	{
		final Map<Integer, Integer> selectedOrderEntryMap = getSelectedOrderEntryQuantities(context, selectionStrategies);
		final Set<OrderEntryRAO> selectedOrderEntryRaos = getSelectedOrderEntryRaos(selectionStrategies, selectedOrderEntryMap);

		final List<DiscountRAO> discounts = getRuleEngineCalculationService().addOrderEntryLevelDiscount(selectedOrderEntryMap,
				selectedOrderEntryRaos, absolute, price);
		for (final DiscountRAO discount : discounts)
		{
			final OrderEntryRAO entry = (OrderEntryRAO) discount.getAppliedToObject();
			consumeOrderEntry(entry, selectedOrderEntryMap.get(entry.getEntryNumber()).intValue(), adjustUnitPrice(entry), discount);

			if (!mergeDiscounts(context, discount, entry))
			{
				final RuleEngineResultRAO result = context.getRuleEngineResultRao();
				result.getActions().add(discount);
				setRAOMetaData(context, discount);
				context.insertFacts(discount);
				context.insertFacts(discount.getConsumedEntries());
				//have to update firedRuleCode so consumed order entries can be created for the PromotionResult and message can be shown on entry level
				discount.getConsumedEntries().forEach(coe -> coe.setFiredRuleCode(discount.getFiredRuleCode()));
			}
			context.scheduleForUpdate(discount.getAppliedToObject());
		}
		return discounts;
	}

}
