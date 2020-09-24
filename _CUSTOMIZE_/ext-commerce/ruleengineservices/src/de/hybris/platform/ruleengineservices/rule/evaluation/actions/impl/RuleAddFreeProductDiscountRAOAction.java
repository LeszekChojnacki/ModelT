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

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;

import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.DiscountRAO;
import de.hybris.platform.ruleengineservices.rao.EntriesSelectionStrategyRPD;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;
import de.hybris.platform.ruleengineservices.rule.evaluation.RuleActionContext;
import de.hybris.platform.ruleengineservices.rule.evaluation.actions.AbstractRuleExecutableSupport;
import de.hybris.platform.ruleengineservices.util.RAOConstants;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;


/**
 * @deprecated since 18.11
 */
@Deprecated
public class RuleAddFreeProductDiscountRAOAction extends AbstractRuleExecutableSupport
{

	@Override
	public boolean performActionInternal(final RuleActionContext context)
	{
		final List<EntriesSelectionStrategyRPD> strategies = (List<EntriesSelectionStrategyRPD>) context
				.getParameter(RAOConstants.SELECTION_STRATEGY_RPDS_PARAM);
		final Integer quantity = context.getParameter(RAOConstants.QUANTITY_PARAM, Integer.class);
		checkArgument(nonNull(quantity), "Products quantity must be defined. Is NULL now");
		final Integer consumed = context.getParameter(RAOConstants.CONSUMED_PARAM, Integer.class);
		checkArgument(nonNull(consumed), "Products consumed must be defined. Is NULL now");

		if (nonNull(strategies))
		{
			this.validateSelectionStrategy(strategies, context);
		}

		performAction(context, strategies, quantity.intValue(), consumed.intValue());
		return true;
	}

	protected void performAction(final RuleActionContext context, final Collection<EntriesSelectionStrategyRPD> strategies,
			final int quantity, final int consumed)
	{
		final int consumableQuantity = consumed * quantity;

		final List<DiscountRAO> discounts = addFreeOrderEntryLevelDiscount(strategies, consumableQuantity, context);

		final RuleEngineResultRAO result = context.getRuleEngineResultRao();
		for (final DiscountRAO discount : discounts)
		{
			result.getActions().add(discount);
			setRAOMetaData(context, discount);
			context.insertFacts(discount, discount.getConsumedEntries());
		}

		for (final EntriesSelectionStrategyRPD strategy : strategies)
		{
			for (final OrderEntryRAO orderEntryRao : strategy.getOrderEntries())
			{
				context.scheduleForUpdate(orderEntryRao);
			}
			context.scheduleForUpdate(strategy.getOrderEntries().get(0).getOrder(), result);
		}
	}

	protected List<DiscountRAO> addFreeOrderEntryLevelDiscount(final Collection<EntriesSelectionStrategyRPD> strategies,
			final int consumeableQty, final RuleActionContext context)
	{
		final Map<Integer, Integer> selectedOrderEntryMap = getSelectedOrderEntryQuantities(context, strategies);
		final Set<OrderEntryRAO> selectedOrderEntryRaos = getSelectedOrderEntryRaos(strategies, selectedOrderEntryMap);

		if (CollectionUtils.isNotEmpty(strategies))
		{
			final EntriesSelectionStrategyRPD selectionStrategy = strategies.iterator().next();
			final CartRAO cartRao = (CartRAO) selectionStrategy.getOrderEntries().get(0).getOrder();
			final List<DiscountRAO> discounts = getRuleEngineCalculationService().addFixedPriceEntriesDiscount(cartRao,
					selectedOrderEntryMap, selectedOrderEntryRaos, BigDecimal.valueOf(0.0));

			for (final DiscountRAO discount : discounts)
			{
				setRAOMetaData(context, discount);
				final OrderEntryRAO orderEntryRao = (OrderEntryRAO) discount.getAppliedToObject();
				consumeOrderEntry(orderEntryRao, (int) discount.getAppliedToQuantity(), adjustUnitPrice(orderEntryRao), discount);
			}
			for (final EntriesSelectionStrategyRPD strategy : strategies)
			{
				consumeAdditionalEntries(strategy, selectedOrderEntryMap, consumeableQty, !discounts.isEmpty() ? discounts.get(0)
						: null);
			}

			return discounts;
		}
		return Collections.emptyList();
	}

	//consumes entries with discount amount=0 for entries that are triggering promotion, but do not have a discount applied directly
	protected void consumeAdditionalEntries(final EntriesSelectionStrategyRPD selectionStrategy,
			final Map<Integer, Integer> selectedOrderEntryMap, final int totalToConsume, final DiscountRAO discount)
	{
		final List<OrderEntryRAO> list = selectionStrategy.getOrderEntries().stream()
				.filter(e -> selectedOrderEntryMap.keySet().contains(e.getEntryNumber())).collect(Collectors.toList());
		list.addAll(selectionStrategy.getOrderEntries().stream()
				.filter(e -> !selectedOrderEntryMap.keySet().contains(e.getEntryNumber())).collect(Collectors.toList()));

		int totalAmount = totalToConsume;
		for (final OrderEntryRAO orderEntry : list)
		{
			final int alreadyConsumedQty = selectedOrderEntryMap.containsKey(orderEntry.getEntryNumber()) ? selectedOrderEntryMap
					.get(orderEntry.getEntryNumber()).intValue() : 0;
			//qtyToConsume the quantity that should be consumed by the order entry in total
			final int qtyToConsume = Math.min(orderEntry.getQuantity(), totalAmount) - alreadyConsumedQty;

			if (qtyToConsume > 0)
			{
				consumeOrderEntry(orderEntry, qtyToConsume, BigDecimal.ZERO, discount);
			}

			totalAmount -= qtyToConsume + alreadyConsumedQty;
			if (totalAmount <= 0)
			{
				break;
			}
		}
	}


}
