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

import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.DiscountRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;
import de.hybris.platform.ruleengineservices.rule.evaluation.RuleActionContext;
import de.hybris.platform.ruleengineservices.rule.evaluation.actions.AbstractRuleExecutableSupport;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;


public class RuleOrderFixedDiscountRAOAction extends AbstractRuleExecutableSupport
{

	@Override
	public boolean performActionInternal(final RuleActionContext context)
	{
		final Map<String, BigDecimal> values = (Map<String, BigDecimal>) context.getParameter(VALUE_PARAM);
		final CartRAO cartRao = context.getCartRao();

		final BigDecimal discountValueForCartCurrency = values.get(cartRao.getCurrencyIsoCode());
		return nonNull(discountValueForCartCurrency) && performAction(context, discountValueForCartCurrency);
	}

	protected boolean performAction(final RuleActionContext context, final BigDecimal amount)
	{
		final CartRAO cartRao = context.getCartRao();
		if(CollectionUtils.isNotEmpty(cartRao.getEntries()))
		{
			final RuleEngineResultRAO result = context.getRuleEngineResultRao();

			final DiscountRAO discount = getRuleEngineCalculationService().addOrderLevelDiscount(cartRao, true, amount);
			result.getActions().add(discount);
			setRAOMetaData(context, discount);
			context.scheduleForUpdate(cartRao, result);
			context.insertFacts(discount);

			return true;
		}
		return false;
	}

}
