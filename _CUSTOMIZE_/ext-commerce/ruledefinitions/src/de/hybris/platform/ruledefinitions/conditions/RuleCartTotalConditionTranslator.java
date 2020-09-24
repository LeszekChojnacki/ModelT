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
package de.hybris.platform.ruledefinitions.conditions;

import static de.hybris.platform.ruledefinitions.conditions.builders.IrConditions.empty;
import static de.hybris.platform.ruledefinitions.conditions.builders.RuleIrAttributeConditionBuilder.newAttributeConditionFor;
import static de.hybris.platform.ruledefinitions.conditions.builders.RuleIrGroupConditionBuilder.newGroupConditionOf;

import de.hybris.platform.ruledefinitions.AmountOperator;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeOperator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrGroupCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrGroupOperator;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Required;


public class RuleCartTotalConditionTranslator extends AbstractRuleConditionTranslator
{
	/**
	 * the attribute used for the cart threshold condition ("subTotal" or "total")
	 */
	private String cartThresholdConditionAttribute;

	@Override
	public RuleIrCondition translate(final RuleCompilerContext context, final RuleConditionData condition,
				 final RuleConditionDefinitionData conditionDefinition)
	{
		final Map<String, RuleParameterData> conditionParameters = condition.getParameters();
		final RuleParameterData operatorParameter = conditionParameters.get(OPERATOR_PARAM);
		final RuleParameterData valueParameter = conditionParameters.get(VALUE_PARAM);

		if (verifyAllPresent(operatorParameter, valueParameter))
		{
			final AmountOperator operator = operatorParameter.getValue();
			final Map<String, BigDecimal> value = valueParameter.getValue();
			if (verifyAllPresent(operator, value))
			{
				return getCartTotalConditions(context, operator, value);
			}
		}

		return empty();
	}

	protected RuleIrGroupCondition getCartTotalConditions(final RuleCompilerContext context, final AmountOperator operator,
				 final Map<String, BigDecimal> value)
	{
		final RuleIrGroupCondition irCartTotalCondition = newGroupConditionOf(RuleIrGroupOperator.OR).build();
		addCartTotalConditions(context, operator, value, irCartTotalCondition);
		return irCartTotalCondition;
	}

	protected void addCartTotalConditions(final RuleCompilerContext context, final AmountOperator operator,
				 final Map<String, BigDecimal> value, final RuleIrGroupCondition irCartTotalCondition)
	{
		final String cartRaoVariable = context.generateVariable(CartRAO.class);


		for (final Entry<String, BigDecimal> entry : value.entrySet())
		{
			if (verifyAllPresent(entry.getKey(), entry.getValue()))
			{
				final RuleIrGroupCondition irCurrencyGroupCondition = newGroupConditionOf(RuleIrGroupOperator.AND).build();
				final List<RuleIrCondition> ruleIrConditions = irCurrencyGroupCondition.getChildren();

				ruleIrConditions.add(newAttributeConditionFor(cartRaoVariable)
							 .withAttribute(CART_RAO_CURRENCY_ATTRIBUTE)
							 .withOperator(RuleIrAttributeOperator.EQUAL)
							 .withValue(entry.getKey())
							 .build());
				ruleIrConditions.add(newAttributeConditionFor(cartRaoVariable)
						.withAttribute(getCartThresholdConditionAttribute())
							 .withOperator(RuleIrAttributeOperator.valueOf(operator.name()))
							 .withValue(entry.getValue())
							 .build());
				irCartTotalCondition.getChildren().add(irCurrencyGroupCondition);
			}
		}
	}

	protected String getCartThresholdConditionAttribute()
	{
		return cartThresholdConditionAttribute;
	}

	/**
	 * attribute name used for the order threshold condition ("total" by default, you can redeclare this property to use
	 * e.g. "subTotal" instead)
	 *
	 * @param cartThresholdConditionAttribute
	 *           attribute name used for the order threshold condition
	 */
	@Required
	public void setCartThresholdConditionAttribute(final String cartThresholdConditionAttribute)
	{
		this.cartThresholdConditionAttribute = cartThresholdConditionAttribute;
	}
}
