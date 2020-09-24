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

import static de.hybris.platform.ruleengineservices.util.RAOConstants.SELECTION_STRATEGY_PARAM;
import static de.hybris.platform.ruleengineservices.util.RAOConstants.VALUE_PARAM;
import static java.util.Objects.isNull;

import de.hybris.platform.ruleengineservices.enums.OrderEntrySelectionStrategy;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.DiscountRAO;
import de.hybris.platform.ruleengineservices.rao.EntriesSelectionStrategyRPD;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;
import de.hybris.platform.ruleengineservices.rule.evaluation.RuleActionContext;
import de.hybris.platform.ruleengineservices.rule.evaluation.actions.AbstractRulePartnerProductAction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.google.common.collect.Lists;


public class RulePartnerOrderEntryPercentageDiscountRAOAction extends AbstractRulePartnerProductAction
{

	public static final String QUALIFYING_CONTAINERS_PARAM = "qualifying_containers";
	public static final String PARTNER_CONTAINERS_PARAM = "target_containers";

	@Override
	public boolean performActionInternal(final RuleActionContext context)
	{
		OrderEntrySelectionStrategy selectionStrategy = (OrderEntrySelectionStrategy) context
				.getParameter(SELECTION_STRATEGY_PARAM);
		final Map<String, Integer> qualifyingProductsContainers = (Map<String, Integer>) context
				.getParameter(QUALIFYING_CONTAINERS_PARAM);
		final Map<String, Integer> partnerProductsContainers = (Map<String, Integer>) context
				.getParameter(PARTNER_CONTAINERS_PARAM);

		if (isNull(selectionStrategy))
		{
			selectionStrategy = OrderEntrySelectionStrategy.DEFAULT;
		}

		final List<EntriesSelectionStrategyRPD> selectionStrategyRPDs = Lists.newArrayList();

		final List<EntriesSelectionStrategyRPD> triggeringSelectionStrategyRPDs = createSelectionStrategyRPDsQualifyingProducts(
				context, selectionStrategy, qualifyingProductsContainers);
		selectionStrategyRPDs.addAll(triggeringSelectionStrategyRPDs);

		final List<EntriesSelectionStrategyRPD> targetingSelectionStrategyRPDs = createSelectionStrategyRPDsTargetProducts(context,
				selectionStrategy, partnerProductsContainers);
		selectionStrategyRPDs.addAll(targetingSelectionStrategyRPDs);

		return extractAmountForCurrency(context, context.getParameter(VALUE_PARAM)).map(
				amount -> performAction(context, selectionStrategyRPDs, amount)).orElse(false);
	}

	protected boolean performAction(final RuleActionContext context,
			final List<EntriesSelectionStrategyRPD> entriesSelectionStrategyRPDs, final BigDecimal amount)
	{
		boolean isPerformed = false;
		validateSelectionStrategy(entriesSelectionStrategyRPDs, context);

		if (hasEnoughQuantity(context, entriesSelectionStrategyRPDs))
		{
			isPerformed = true;
			final List<EntriesSelectionStrategyRPD> selectionStrategyRPDsForAction = Lists.newArrayList();
			final List<EntriesSelectionStrategyRPD> selectionStrategyRPDsForTriggering = Lists.newArrayList();
			splitEntriesSelectionStrategies(entriesSelectionStrategyRPDs, selectionStrategyRPDsForAction,
					selectionStrategyRPDsForTriggering);

			final List<DiscountRAO> discounts = addDiscountAndConsume(context, selectionStrategyRPDsForAction, false, amount);
			if (CollectionUtils.isNotEmpty(selectionStrategyRPDsForTriggering))
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
				context.scheduleForUpdate(result);
			}
			context.scheduleForUpdate(discount.getAppliedToObject());
		}
		if (CollectionUtils.isNotEmpty(selectedOrderEntryRaos))
		{
			final CartRAO cartRAO = (CartRAO) selectedOrderEntryRaos.iterator().next().getOrder();
			context.scheduleForUpdate(cartRAO);
		}
		return discounts;
	}

}
