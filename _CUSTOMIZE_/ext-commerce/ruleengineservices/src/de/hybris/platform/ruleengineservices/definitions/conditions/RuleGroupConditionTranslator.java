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
package de.hybris.platform.ruleengineservices.definitions.conditions;

import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleConditionTranslator;
import de.hybris.platform.ruleengineservices.compiler.RuleConditionValidator;
import de.hybris.platform.ruleengineservices.compiler.RuleConditionsTranslator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrGroupCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrGroupOperator;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


public class RuleGroupConditionTranslator implements RuleConditionTranslator, RuleConditionValidator
{
	public static final String OPERATOR_PARAM = "operator";

	private RuleConditionsTranslator ruleConditionsTranslator;

	public RuleConditionsTranslator getRuleConditionsTranslator()
	{
		return ruleConditionsTranslator;
	}

	@Required
	public void setRuleConditionsTranslator(final RuleConditionsTranslator ruleConditionsTranslator)
	{
		this.ruleConditionsTranslator = ruleConditionsTranslator;
	}

	@Override
	public void validate(final RuleCompilerContext context, final RuleConditionData condition,
			final RuleConditionDefinitionData conditionDefinition)
	{
		if (CollectionUtils.isNotEmpty(condition.getChildren()))
		{
			ruleConditionsTranslator.validate(context, condition.getChildren());
		}
	}

	@Override
	public RuleIrCondition translate(final RuleCompilerContext context, final RuleConditionData condition,
			final RuleConditionDefinitionData conditionDefinition)
	{
		RuleGroupOperator operator = null;

		final RuleParameterData operatorParameter = condition.getParameters().get(OPERATOR_PARAM);
		if (operatorParameter != null && operatorParameter.getValue() != null)
		{
			operator = operatorParameter.getValue();
		}
		else
		{
			operator = RuleGroupOperator.AND;
		}

		final RuleIrGroupOperator irOperator = RuleIrGroupOperator.valueOf(operator.name());
		final List<RuleIrCondition> irChildren = ruleConditionsTranslator.translate(context, condition.getChildren());

		final RuleIrGroupCondition irGroupCondition = new RuleIrGroupCondition();
		irGroupCondition.setOperator(irOperator);
		irGroupCondition.setChildren(irChildren);

		return irGroupCondition;
	}
}
