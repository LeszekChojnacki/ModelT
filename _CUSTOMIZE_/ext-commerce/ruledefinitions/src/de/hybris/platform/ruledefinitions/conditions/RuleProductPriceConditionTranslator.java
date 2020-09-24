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

import static com.google.common.collect.Lists.newArrayList;
import static de.hybris.platform.ruledefinitions.conditions.builders.IrConditions.empty;
import static de.hybris.platform.ruledefinitions.conditions.builders.RuleIrAttributeConditionBuilder.newAttributeConditionFor;
import static de.hybris.platform.ruledefinitions.conditions.builders.RuleIrAttributeRelConditionBuilder.newAttributeRelationConditionFor;
import static de.hybris.platform.ruledefinitions.conditions.builders.RuleIrGroupConditionBuilder.newGroupConditionOf;

import de.hybris.platform.ruledefinitions.AmountOperator;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeOperator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrGroupCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrGroupOperator;
import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Creates the intermediate representation of the OrderEntryRAO.basePrice condition
 */
public class RuleProductPriceConditionTranslator extends AbstractRuleConditionTranslator
{

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
				return getProductPriceConditions(context, operator, value);
			}
		}
		return empty();
	}

	protected RuleIrGroupCondition getProductPriceConditions(final RuleCompilerContext context, final AmountOperator operator,
			final Map<String, BigDecimal> value)
	{
		final RuleIrGroupCondition irGroupCondition = newGroupConditionOf(RuleIrGroupOperator.OR).build();
		addProductPriceConditions(context, operator, value, irGroupCondition);
		return irGroupCondition;
	}

	protected void addProductPriceConditions(final RuleCompilerContext context,
				 final AmountOperator operator,
				 final Map<String, BigDecimal> value, final RuleIrGroupCondition irGroupCondition)
	{
		final String orderEntryRaoVariable = context.generateVariable(OrderEntryRAO.class);
		final String cartRaoVariable = context.generateVariable(CartRAO.class);

		for (final Entry<String, BigDecimal> entry : value.entrySet())
		{
			if (verifyAllPresent(entry.getKey(), entry.getValue()))
			{
				final List<RuleIrCondition> conditions = newArrayList();

				final RuleIrGroupCondition irCurrencyGroupCondition = newGroupConditionOf(RuleIrGroupOperator.AND)
						.withChildren(conditions)
						.build();

				conditions.add(newAttributeConditionFor(cartRaoVariable)
						.withAttribute(CART_RAO_CURRENCY_ATTRIBUTE)
						.withOperator(RuleIrAttributeOperator.EQUAL)
						.withValue(entry.getKey())
						.build());
				conditions.add(newAttributeConditionFor(orderEntryRaoVariable)
						.withAttribute(ORDER_ENTRY_RAO_BASE_PRICE_ATTRIBUTE)
						.withOperator(RuleIrAttributeOperator.valueOf(operator.name()))
						.withValue(entry.getValue())
						.build());
				conditions.add(newAttributeRelationConditionFor(cartRaoVariable)
						.withAttribute(CART_RAO_ENTRIES_ATTRIBUTE)
						.withOperator(RuleIrAttributeOperator.CONTAINS)
						.withTargetVariable(orderEntryRaoVariable)
						.build());

				conditions.addAll(createProductConsumedCondition(context, orderEntryRaoVariable));

				irGroupCondition.getChildren().add(irCurrencyGroupCondition);
			}
		}
	}

}
