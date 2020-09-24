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

import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.DiscountRAO;
import de.hybris.platform.ruleengineservices.rao.EntriesSelectionStrategyRPD;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;
import de.hybris.platform.ruleengineservices.rule.evaluation.RuleActionContext;
import de.hybris.platform.ruleengineservices.rule.evaluation.actions.AbstractRuleExecutableSupport;
import de.hybris.platform.ruleengineservices.util.RAOConstants;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.hybris.platform.ruleengineservices.util.RAOConstants.VALUE_PARAM;


/**
 * @deprecated since 6.6 (not used in any conditions)
 */
@Deprecated
public class RuleOrderEntryGroupFixedDiscountRAOAction extends AbstractRuleExecutableSupport //NOSONAR
{

	@Override
	public boolean performActionInternal(final RuleActionContext context)
	{
		final List<EntriesSelectionStrategyRPD> strategies = (List<EntriesSelectionStrategyRPD>) context
				.getParameter(RAOConstants.SELECTION_STRATEGY_RPDS_PARAM);

		return extractAmountForCurrency(context, context.getParameter(VALUE_PARAM)).map(
				amount -> performAction(context, strategies, amount)).orElse(false);
	}

	protected boolean performAction(final RuleActionContext context, final List<EntriesSelectionStrategyRPD> strategies,
			final BigDecimal targetPrice)
	{
		boolean isPerformed = false;
		validateSelectionStrategy(strategies, context);
		if (hasEnoughQuantity(context, strategies))
		{
			DiscountRAO discount;
			final Map<Integer, Integer> selectedOrderEntryMap = getSelectedOrderEntryQuantities(context, strategies);
			final Set<OrderEntryRAO> selectedOrderEntryRaos = getSelectedOrderEntryRaos(strategies, selectedOrderEntryMap);

			final BigDecimal currentPriceOfToBeDiscountedOrderEntries = this.getRuleEngineCalculationService().getCurrentPrice(
					selectedOrderEntryRaos, selectedOrderEntryMap);

			if (currentPriceOfToBeDiscountedOrderEntries.compareTo(targetPrice) > 0)
			{
				isPerformed = true;
				final RuleEngineResultRAO result = context.getRuleEngineResultRao();
				final CartRAO cartRAO = context.getCartRao();
				final BigDecimal discountAmount = getCurrencyUtils().applyRounding(
						currentPriceOfToBeDiscountedOrderEntries.subtract(targetPrice), cartRAO.getCurrencyIsoCode());

				discount = getRuleEngineCalculationService().addOrderLevelDiscount(cartRAO, true, discountAmount);
				consumeOrderEntries(selectedOrderEntryRaos, selectedOrderEntryMap, discount);
				result.getActions().add(discount);
				setRAOMetaData(context, discount);
				context.insertFacts(discount, discount.getConsumedEntries());
				for (final OrderEntryRAO selectedOrderEntry : selectedOrderEntryRaos)
				{
					context.scheduleForUpdate(selectedOrderEntry);
				}
				context.scheduleForUpdate(cartRAO, result);
			}
		}
		return isPerformed;
	}

}
