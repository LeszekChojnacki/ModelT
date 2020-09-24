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
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import de.hybris.platform.ruleengineservices.rao.DiscountRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;
import de.hybris.platform.ruleengineservices.rule.evaluation.RuleActionContext;
import de.hybris.platform.ruleengineservices.rule.evaluation.actions.AbstractRuleExecutableSupport;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;


public class RuleOrderEntryPercentageDiscountRAOAction extends AbstractRuleExecutableSupport
{

	@Override
	public boolean performActionInternal(final RuleActionContext context)
	{
		boolean isPerformed = false;
		final Optional<BigDecimal> amount = extractAmountForCurrency(context, context.getParameter(VALUE_PARAM));
		if (amount.isPresent())
		{
			final Set<OrderEntryRAO> orderEntries = context.getValues(OrderEntryRAO.class);
			if (isNotEmpty(orderEntries))
			{
				for (final OrderEntryRAO orderEntryRAO : orderEntries)
				{
					isPerformed |= processOrderEntry(context, orderEntryRAO, amount.get());
				}
			}
		}

		return isPerformed;
	}

	protected boolean processOrderEntry(final RuleActionContext context, final OrderEntryRAO orderEntryRao, final BigDecimal value)
	{
		boolean isPerformed = false;
		final int consumableQuantity = getConsumableQuantity(orderEntryRao);
		if (consumableQuantity > 0)
		{
			final DiscountRAO discount = getRuleEngineCalculationService().addOrderEntryLevelDiscount(orderEntryRao, false, value);
			setRAOMetaData(context, discount);

			consumeOrderEntry(orderEntryRao, consumableQuantity, adjustUnitPrice(orderEntryRao, consumableQuantity), discount);

			final RuleEngineResultRAO result = context.getValue(RuleEngineResultRAO.class);
			result.getActions().add(discount);
			context.scheduleForUpdate(orderEntryRao, orderEntryRao.getOrder(), result);
			context.insertFacts(discount);
			context.insertFacts(discount.getConsumedEntries());
			isPerformed = true;
		}
		return isPerformed;
	}

}
