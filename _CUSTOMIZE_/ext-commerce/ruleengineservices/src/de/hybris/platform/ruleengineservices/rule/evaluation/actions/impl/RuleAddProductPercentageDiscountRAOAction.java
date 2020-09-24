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
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

import de.hybris.platform.ruleengineservices.rao.DiscountRAO;
import de.hybris.platform.ruleengineservices.rao.ProductRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;
import de.hybris.platform.ruleengineservices.rule.evaluation.RuleActionContext;
import de.hybris.platform.ruleengineservices.rule.evaluation.actions.AbstractRuleExecutableSupport;

import java.math.BigDecimal;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated since 18.11
 */
@Deprecated
public class RuleAddProductPercentageDiscountRAOAction extends AbstractRuleExecutableSupport
{

	private static final Logger LOG = LoggerFactory.getLogger(RuleAddProductPercentageDiscountRAOAction.class);

	@Override
	public boolean performActionInternal(final RuleActionContext context)
	{
		final Optional<BigDecimal> amount = extractAmountForCurrency(context, context.getParameter(VALUE_PARAM));
		if (amount.isPresent())
		{
			performAction(context, amount.get());
			return true;
		}
		else
		{
			LOG.error("no matching discount amount specified for rule {}, cannot apply rule action.", getRuleCode(context));
			return false;
		}
	}

	protected void performAction(final RuleActionContext context, final BigDecimal amount)
	{
		final ProductRAO productRao = context.getValue(ProductRAO.class);
		validateParameterNotNull(productRao, "product rao must not be null");
		validateParameterNotNull(amount, "amount must not be bull");

		final RuleEngineResultRAO result = context.getRuleEngineResultRao();
		final DiscountRAO discount = new DiscountRAO();
		discount.setAppliedToObject(productRao);
		discount.setValue(amount);
		result.getActions().add(discount);
	}
}
