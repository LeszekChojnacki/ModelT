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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.hybris.platform.ruleengineservices.util.RAOConstants.SELECTION_STRATEGY_PARAM;
import static de.hybris.platform.ruleengineservices.util.RAOConstants.VALUE_PARAM;
import static java.util.Objects.isNull;


public class RuleTargetBundlePriceRAOAction extends AbstractRulePartnerProductAction
{
	public static final String QUALIFYING_CONTAINERS_PARAM = "qualifying_containers";

	@Override
	public boolean performActionInternal(final RuleActionContext context)
	{
		final Map<String, BigDecimal> targetBundlePriceValue = (Map<String, BigDecimal>) context.getParameter(VALUE_PARAM);
		OrderEntrySelectionStrategy selectionStrategy = (OrderEntrySelectionStrategy) context
				.getParameter(SELECTION_STRATEGY_PARAM);
		final Map<String, Integer> bundleProductQuantitiesPerContainer = (Map<String, Integer>) context
				.getParameter(QUALIFYING_CONTAINERS_PARAM);

		final CartRAO cart = context.getValue(CartRAO.class);
		final BigDecimal targetBundlePrice = targetBundlePriceValue.get(cart.getCurrencyIsoCode());
		if (isNull(targetBundlePrice))
		{
			return false;
		}

		if (isNull(selectionStrategy))
		{
			selectionStrategy = OrderEntrySelectionStrategy.DEFAULT;
		}

		final List<EntriesSelectionStrategyRPD> selectionContainers = createSelectionStrategyRPDsTargetProducts(context,
				selectionStrategy, bundleProductQuantitiesPerContainer);

		return performAction(context, selectionContainers, targetBundlePrice);
	}

	protected boolean performAction(final RuleActionContext context,
			final List<EntriesSelectionStrategyRPD> entriesSelectionStrategyRPDs, final BigDecimal amount)
	{
		boolean isPerformed = false;
		validateSelectionStrategy(entriesSelectionStrategyRPDs, context);
		if (hasEnoughQuantity(context, entriesSelectionStrategyRPDs))
		{
			DiscountRAO discount;

			BigDecimal fixedPrice = amount;
			if (!isConsumptionEnabled())
			{
				// if order entry consumption is turned off, the container
				// based actions need to consume "all in one go" like in previous versions
				final int count = adjustStrategyQuantity(entriesSelectionStrategyRPDs, -1);
				fixedPrice = amount.multiply(BigDecimal.valueOf(count));
			}

			final Map<Integer, Integer> selectedOrderEntryMap = getSelectedOrderEntryQuantities(context, entriesSelectionStrategyRPDs);
			final Set<OrderEntryRAO> selectedOrderEntryRaos = getSelectedOrderEntryRaos(entriesSelectionStrategyRPDs,
					selectedOrderEntryMap);

			final BigDecimal currentPriceOfToBeDiscountedOrderEntries = this.getRuleEngineCalculationService().getCurrentPrice(
					selectedOrderEntryRaos, selectedOrderEntryMap);

			if (currentPriceOfToBeDiscountedOrderEntries.compareTo(fixedPrice) > 0)
			{
				isPerformed = true;
				final RuleEngineResultRAO result = context.getRuleEngineResultRao();
				final CartRAO cartRAO = context.getCartRao();
				final BigDecimal discountAmount = getCurrencyUtils().applyRounding(
						currentPriceOfToBeDiscountedOrderEntries.subtract(fixedPrice), cartRAO.getCurrencyIsoCode());

				// merge to an existing discount if applicable (not create the new one)
				discount = getRuleEngineCalculationService().addOrderLevelDiscount(cartRAO, true, discountAmount);

				result.getActions().add(discount);
				setRAOMetaData(context, discount);

				consumeOrderEntries(selectedOrderEntryRaos, selectedOrderEntryMap, discount);
				discount.getConsumedEntries().forEach(coe -> coe.setFiredRuleCode(discount.getFiredRuleCode()));

				context.insertFacts(discount, discount.getConsumedEntries());
				for (final OrderEntryRAO selectedOrderEntry : selectedOrderEntryRaos)
				{
					getRaoUtils().addAction(selectedOrderEntry, discount);
					context.scheduleForUpdate(selectedOrderEntry);
				}
				context.scheduleForUpdate(cartRAO, result);
			}
		}
		return isPerformed;
	}
}
