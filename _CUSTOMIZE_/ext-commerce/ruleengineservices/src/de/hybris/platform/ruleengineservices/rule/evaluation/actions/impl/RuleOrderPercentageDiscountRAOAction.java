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

import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.DiscountRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;
import de.hybris.platform.ruleengineservices.rule.evaluation.RuleActionContext;
import de.hybris.platform.ruleengineservices.rule.evaluation.actions.AbstractRuleExecutableSupport;

import java.math.BigDecimal;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RuleOrderPercentageDiscountRAOAction extends AbstractRuleExecutableSupport
{

	private static final Logger LOG = LoggerFactory.getLogger(RuleOrderPercentageDiscountRAOAction.class);


	@Override
	public boolean performActionInternal(final RuleActionContext context)
	{
		return extractAmountForCurrency(context, context.getParameter(VALUE_PARAM)).map(a -> performAction(context, a)).orElseGet(
				() -> {
					LOG.error("no matching discount amount specified for rule {}, cannot apply rule action.", getRuleCode(context));
					return false;
				});
	}

	protected boolean performAction(final RuleActionContext context, final BigDecimal amount)
	{
		final CartRAO cartRao = context.getCartRao();
		if(CollectionUtils.isNotEmpty(cartRao.getEntries()))
		{
			final DiscountRAO discount = getRuleEngineCalculationService().addOrderLevelDiscount(cartRao, false, amount);

			final RuleEngineResultRAO result = context.getRuleEngineResultRao();
			result.getActions().add(discount);
			setRAOMetaData(context, discount);
			context.scheduleForUpdate(cartRao, result);
			context.insertFacts(discount);

			return true;
		}
		return false;
	}

}
