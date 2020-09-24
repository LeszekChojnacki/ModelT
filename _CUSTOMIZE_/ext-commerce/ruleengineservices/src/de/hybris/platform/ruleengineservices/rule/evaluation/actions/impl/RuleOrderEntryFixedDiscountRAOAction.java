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

import static de.hybris.platform.ruleengineservices.util.RAOConstants.VALUE_PARAM;
import static java.util.Objects.nonNull;

import de.hybris.platform.ruleengineservices.rao.DiscountRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;
import de.hybris.platform.ruleengineservices.rule.evaluation.RuleActionContext;
import de.hybris.platform.ruleengineservices.rule.evaluation.actions.AbstractRuleExecutableSupport;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;


public class RuleOrderEntryFixedDiscountRAOAction extends AbstractRuleExecutableSupport
{

	@Override
	public boolean performActionInternal(final RuleActionContext context)
	{
		final Set<OrderEntryRAO> orderEntries = context.getValues(OrderEntryRAO.class);

		final Map<String, BigDecimal> values = (Map<String, BigDecimal>) context.getParameter(VALUE_PARAM);
		boolean isPerformed = false;
		for (final OrderEntryRAO orderEntry : orderEntries)
		{
			final BigDecimal valueForCurrency = values.get(orderEntry.getCurrencyIsoCode());
			if (nonNull(valueForCurrency))
			{
				isPerformed |= performAction(context, orderEntry, valueForCurrency);
			}
		}
		return isPerformed;
	}

	protected boolean performAction(final RuleActionContext context, final OrderEntryRAO orderEntryRao,
			final BigDecimal valueForCurrency)
	{
		boolean isPerformed = false;
		final int consumableQuantity = getConsumableQuantity(orderEntryRao);
		if (consumableQuantity > 0)
		{
			isPerformed = true;
			final DiscountRAO discount = getRuleEngineCalculationService().addOrderEntryLevelDiscount(orderEntryRao, true,
					valueForCurrency);
			setRAOMetaData(context, discount);

			consumeOrderEntry(orderEntryRao, consumableQuantity, adjustUnitPrice(orderEntryRao, consumableQuantity), discount);

			final RuleEngineResultRAO result = context.getRuleEngineResultRao();
			result.getActions().add(discount);

			context.scheduleForUpdate(orderEntryRao, orderEntryRao.getOrder(), result);
			context.insertFacts(discount);
			context.insertFacts(discount.getConsumedEntries());
		}
		return isPerformed;
	}

}
