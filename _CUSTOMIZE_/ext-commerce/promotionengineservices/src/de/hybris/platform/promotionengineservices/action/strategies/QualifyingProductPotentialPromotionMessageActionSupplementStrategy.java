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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;


/**
 * The class supplements {@link DisplayMessageRAO} with dynamically-evaluated data for Qualifying Product condition.
 *
 */
public class QualifyingProductPotentialPromotionMessageActionSupplementStrategy implements ActionSupplementStrategy
{
	protected static final String PRODUCTS_QUANTITY_PARAMETER = "qualifying_products_quantity";
	protected static final String PRODUCTS_QUANTITY_PARAMETER_UUID = PRODUCTS_QUANTITY_PARAMETER + UUID_SUFFIX;
	protected static final String PRODUCTS_PARAMETER = "qualifying_products";
	protected static final String PRODUCTS_PARAMETER_UUID = PRODUCTS_PARAMETER + UUID_SUFFIX;

	@Override
	public boolean isActionProperToHandle(final AbstractRuleActionRAO actionRao, final RuleActionContext context)
	{
		return isNotEmpty(context.getParameters()) && actionRao instanceof DisplayMessageRAO
				&& isMessageForQualifiedProduct(context.getParameters());
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

		final Integer targetItemQuantity = (Integer) context.getParameter(PRODUCTS_QUANTITY_PARAMETER);
		final List<String> conditionProduct = (List<String>) context.getParameter(PRODUCTS_PARAMETER);
		final CartRAO cartRao = context.getCartRao();
		final int actualItemQuantity = cartRao.getEntries().stream()
				.filter(e -> conditionProduct.contains(e.getProduct().getCode())).mapToInt(e -> e.getQuantity()).sum();
		if (targetItemQuantity.intValue() > actualItemQuantity)
		{
			displayMessageRAO.getParameters().put(context.getParameter(PRODUCTS_QUANTITY_PARAMETER_UUID).toString(),
					Integer.valueOf(targetItemQuantity.intValue() - actualItemQuantity));
		}
	}

	protected boolean isMessageForQualifiedProduct(final Map<String, Object> parameters)
	{
		return hasNotNullParameter(parameters, PRODUCTS_QUANTITY_PARAMETER)
				&& parameters.containsKey(PRODUCTS_QUANTITY_PARAMETER_UUID) && hasNotNullParameter(parameters, PRODUCTS_PARAMETER)
				&& parameters.containsKey(PRODUCTS_PARAMETER_UUID);
	}

	protected boolean hasNotNullParameter(final Map<String, Object> parameters, final String paramName)
	{
		return parameters.containsKey(paramName) && parameters.get(paramName) != null;
	}
}
