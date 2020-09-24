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

import de.hybris.platform.ruleengineservices.enums.OrderEntrySelectionStrategy;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.DiscountRAO;
import de.hybris.platform.ruleengineservices.rao.EntriesSelectionStrategyRPD;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;
import de.hybris.platform.ruleengineservices.rule.evaluation.RuleActionContext;
import de.hybris.platform.ruleengineservices.rule.evaluation.actions.AbstractRulePartnerProductAction;
import de.hybris.platform.ruleengineservices.util.RAOConstants;
import org.apache.commons.collections.CollectionUtils;
import org.assertj.core.util.Lists;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.hybris.platform.ruleengineservices.util.RAOConstants.VALUE_PARAM;
import static java.util.Objects.isNull;


public class RulePartnerOrderEntryFixedPriceRAOAction extends AbstractRulePartnerProductAction
{
	public static final String QUALIFYING_CONTAINERS_PARAM = "qualifying_containers";
	public static final String PARTNER_CONTAINERS_PARAM = "target_containers";

	@Override
	public boolean performActionInternal(final RuleActionContext context)
	{
		final Map<String, BigDecimal> value = (Map<String, BigDecimal>) context.getParameter(VALUE_PARAM);
		OrderEntrySelectionStrategy selectionStrategy = (OrderEntrySelectionStrategy) context
				.getParameter(RAOConstants.SELECTION_STRATEGY_PARAM);
		final Map<String, Integer> qualifyingProductsContainers = (Map<String, Integer>) context
				.getParameter(QUALIFYING_CONTAINERS_PARAM);
		final Map<String, Integer> partnerProductsContainers = (Map<String, Integer>) context
				.getParameter(PARTNER_CONTAINERS_PARAM);

		final CartRAO cart = context.getValue(CartRAO.class);

		final BigDecimal discountValueForCartCurrency = value.get(cart.getCurrencyIsoCode());
		if (isNull(discountValueForCartCurrency))
		{
			return false;
		}

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

		return performAction(context, selectionStrategyRPDs, discountValueForCartCurrency);
	}

	public boolean performAction(final RuleActionContext context,
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

			final List<EntriesSelectionStrategyRPD> selectionStrategyRPDsForAction = new ArrayList<>();
			final List<EntriesSelectionStrategyRPD> selectionStrategyRPDsForTriggering = new ArrayList<>();
			splitEntriesSelectionStrategies(entriesSelectionStrategyRPDs, selectionStrategyRPDsForAction,
					selectionStrategyRPDsForTriggering);


			final RuleEngineResultRAO result = context.getRuleEngineResultRao();
			final List<DiscountRAO> discounts = addFixedPriceEntryDiscount(selectionStrategyRPDsForAction, amount, context);
			for (final DiscountRAO discount : discounts)
			{
				result.getActions().add(discount);
				setRAOMetaData(context, discount);
				context.insertFacts(discount);
				context.insertFacts(discount.getConsumedEntries());
				discount.getConsumedEntries().forEach(coe -> coe.setFiredRuleCode(discount.getFiredRuleCode()));
				context.scheduleForUpdate(discount.getAppliedToObject());
			}

			context.scheduleForUpdate(entriesSelectionStrategyRPDs.get(0).getOrderEntries().get(0).getOrder(), result);

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

	protected List<DiscountRAO> addFixedPriceEntryDiscount(final List<EntriesSelectionStrategyRPD> selectionStrategies,
			final BigDecimal fixedPrice, final RuleActionContext context)
	{
		final Map<Integer, Integer> selectedOrderEntryMap = getSelectedOrderEntryQuantities(context, selectionStrategies);
		final Set<OrderEntryRAO> selectedOrderEntryRaos = getSelectedOrderEntryRaos(selectionStrategies, selectedOrderEntryMap);

		final CartRAO cartRao = (CartRAO) selectionStrategies.get(0).getOrderEntries().get(0).getOrder();
		final List<DiscountRAO> discounts = getRuleEngineCalculationService().addFixedPriceEntriesDiscount(cartRao,
				selectedOrderEntryMap, selectedOrderEntryRaos, fixedPrice);

		for (final DiscountRAO discount : discounts)
		{
			final OrderEntryRAO orderEntryRao = (OrderEntryRAO) discount.getAppliedToObject();
			consumeOrderEntry(orderEntryRao, (int) discount.getAppliedToQuantity(), adjustUnitPrice(orderEntryRao), discount);
		}

		return discounts;
	}
}
