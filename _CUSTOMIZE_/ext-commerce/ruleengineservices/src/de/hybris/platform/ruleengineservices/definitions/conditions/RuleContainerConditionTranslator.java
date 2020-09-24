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
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblem;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblemFactory;
import de.hybris.platform.ruleengineservices.compiler.RuleConditionTranslator;
import de.hybris.platform.ruleengineservices.compiler.RuleConditionValidator;
import de.hybris.platform.ruleengineservices.compiler.RuleConditionsTranslator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrEmptyCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrGroupCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrGroupOperator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrVariablesGenerator;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


public class RuleContainerConditionTranslator implements RuleConditionTranslator, RuleConditionValidator
{
	public static final String ID_PARAM = "id";
	public static final String NO_CHILDREN = "rule.validation.error.container.children.notexist";

	private RuleConditionsTranslator ruleConditionsTranslator;

	private RuleCompilerProblemFactory ruleCompilerProblemFactory;

	@Override
	public void validate(final RuleCompilerContext context, final RuleConditionData condition,
			final RuleConditionDefinitionData conditionDefinition)
	{
		if (CollectionUtils.isNotEmpty(condition.getChildren()))
		{
			ruleConditionsTranslator.validate(context, condition.getChildren());
		}
		else
		{
			context.addProblem(ruleCompilerProblemFactory.createProblem(RuleCompilerProblem.Severity.ERROR, NO_CHILDREN,
					conditionDefinition.getName()));
		}
	}

	@Override
	public RuleIrCondition translate(final RuleCompilerContext context, final RuleConditionData condition,
			final RuleConditionDefinitionData conditionDefinition)
	{
		final RuleParameterData idParameter = condition.getParameters().get(ID_PARAM);

		if (idParameter == null)
		{
			return new RuleIrEmptyCondition();
		}

		final String id = idParameter.getValue();
		if (id == null)
		{
			return new RuleIrEmptyCondition();
		}

		final RuleIrVariablesGenerator variablesGenerator = context.getVariablesGenerator();

		try
		{
			variablesGenerator.createContainer(id);

			final RuleIrGroupOperator irOperator = RuleIrGroupOperator.AND;
			final List<RuleIrCondition> irChildren = ruleConditionsTranslator.translate(context, condition.getChildren());

			final RuleIrGroupCondition irGroupCondition = new RuleIrGroupCondition();
			irGroupCondition.setOperator(irOperator);
			irGroupCondition.setChildren(irChildren);

			return irGroupCondition;
		}
		finally
		{
			variablesGenerator.closeContainer();
		}
	}

	public RuleConditionsTranslator getRuleConditionsTranslator()
	{
		return ruleConditionsTranslator;
	}

	@Required
	public void setRuleConditionsTranslator(final RuleConditionsTranslator ruleConditionsTranslator)
	{
		this.ruleConditionsTranslator = ruleConditionsTranslator;
	}

	public RuleCompilerProblemFactory getRuleCompilerProblemFactory()
	{
		return ruleCompilerProblemFactory;
	}

	@Required
	public void setRuleCompilerProblemFactory(final RuleCompilerProblemFactory ruleCompilerProblemFactory)
	{
		this.ruleCompilerProblemFactory = ruleCompilerProblemFactory;
	}
}
