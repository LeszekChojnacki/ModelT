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
package de.hybris.platform.promotionengineservices.action.strategies;

import static org.apache.commons.collections.MapUtils.isNotEmpty;

import de.hybris.platform.ruleengineservices.rao.AbstractRuleActionRAO;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.DisplayMessageRAO;
import de.hybris.platform.ruleengineservices.rule.evaluation.RuleActionContext;
import de.hybris.platform.ruleengineservices.rule.evaluation.actions.ActionSupplementStrategy;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;


/**
 * The class supplements {@link DisplayMessageRAO} with dynamically-evaluated data for Cart Total Threshold condition.
 *
 */
public class CartTotalThresholdPotentialPromotionMessageActionSupplementStrategy implements ActionSupplementStrategy
{
	protected static final String CART_TOTAL_THRESHOLD_PARAMETER = "cart_total_threshold_parameter";
	protected static final String CART_TOTAL_THRESHOLD_PARAMETER_UUID = CART_TOTAL_THRESHOLD_PARAMETER + UUID_SUFFIX;

	@Override
	public boolean isActionProperToHandle(final AbstractRuleActionRAO actionRao, final RuleActionContext context)
	{
		return isNotEmpty(context.getParameters()) && actionRao instanceof DisplayMessageRAO
				&& isMessageForCartTotalThreshold(context.getParameters());
	}

	@Override
	public void postProcessAction(final AbstractRuleActionRAO actionRao, final RuleActionContext context)
	{
		Preconditions.checkArgument(isActionProperToHandle(actionRao, context), "The strategy is not proper to handle the action.");

		final DisplayMessageRAO displayMessageRAO = (DisplayMessageRAO) actionRao;
		if (displayMessageRAO.getParameters() == null)
		{
			displayMessageRAO.setParameters(new HashMap<String, Object>());
		}

		final Map<String, BigDecimal> cartTotalThresholdParameters = (Map<String, BigDecimal>) context
				.getParameter(CART_TOTAL_THRESHOLD_PARAMETER);
		final CartRAO cartRao = context.getCartRao();
		final BigDecimal cartSubtTotal = cartRao.getSubTotal();
		final BigDecimal cartTotalThresholdParameter = cartTotalThresholdParameters.get(cartRao.getCurrencyIsoCode());
		if (cartTotalThresholdParameter.compareTo(cartSubtTotal) > 0)
		{
			displayMessageRAO.getParameters().put(context.getParameter(CART_TOTAL_THRESHOLD_PARAMETER_UUID).toString(),
					cartTotalThresholdParameter.subtract(cartSubtTotal));
		}
	}

	protected boolean isMessageForCartTotalThreshold(final Map<String, Object> parameters)
	{
		return parameters.containsKey(CART_TOTAL_THRESHOLD_PARAMETER)
				&& parameters.containsKey(CART_TOTAL_THRESHOLD_PARAMETER_UUID)
				&& parameters.get(CART_TOTAL_THRESHOLD_PARAMETER) != null;
	}
}
