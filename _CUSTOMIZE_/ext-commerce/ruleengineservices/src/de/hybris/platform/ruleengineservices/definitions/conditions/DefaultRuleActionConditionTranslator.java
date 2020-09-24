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
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblemFactory;
import de.hybris.platform.ruleengineservices.compiler.RuleConditionTranslator;
import de.hybris.platform.ruleengineservices.compiler.RuleConditionsTranslator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeOperator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrEmptyCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrLocalVariablesContainer;
import de.hybris.platform.ruleengineservices.compiler.RuleIrNotCondition;
import de.hybris.platform.ruleengineservices.rao.AbstractRuleActionRAO;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Preconditions;


/**
 * Creates the intermediate representation of the Rule Action allowed condition.
 */
public class DefaultRuleActionConditionTranslator implements RuleConditionTranslator
{
	public static final String FIRED_RULE_CODE_ATTRIBUTE = "firedRuleCode";
	public static final String RULE_PARAM = "rule";
	public static final String ALLOWED_PARAM = "allowed";

	private RuleConditionsTranslator ruleConditionsTranslator;
	private RuleCompilerProblemFactory ruleCompilerProblemFactory;

	@Override
	public RuleIrCondition translate(final RuleCompilerContext context, final RuleConditionData condition,
			final RuleConditionDefinitionData conditionDefinition)
	{
		Preconditions.checkNotNull(context, "Rule Compiler Context is not expected to be NULL here");
		Preconditions.checkNotNull(condition, "Rule Condition Data is not expected to be NULL here");

		final RuleParameterData ruleParameter = condition.getParameters().get(RULE_PARAM);
		final RuleParameterData allowedParameter = condition.getParameters().get(ALLOWED_PARAM);

		if (ruleParameter == null || allowedParameter == null)
		{
			return new RuleIrEmptyCondition();
		}

		final String referencedRuleCode = ruleParameter.getValue();
		final Boolean referencedRuleActionAllowed = allowedParameter.getValue();

		if (referencedRuleCode == null || referencedRuleActionAllowed == null)
		{
			return new RuleIrEmptyCondition();
		}

		return translate(context, referencedRuleCode, referencedRuleActionAllowed);
	}

	protected RuleIrCondition translate(final RuleCompilerContext context, final String referencedRuleCode,
			final Boolean referencedRuleActionAllowed)
	{
		final RuleIrAttributeCondition irReferencedRuleActionCondition = new RuleIrAttributeCondition();
		irReferencedRuleActionCondition.setAttribute(FIRED_RULE_CODE_ATTRIBUTE);
		irReferencedRuleActionCondition.setOperator(RuleIrAttributeOperator.EQUAL);
		irReferencedRuleActionCondition.setValue(referencedRuleCode);

		if (referencedRuleActionAllowed.booleanValue())
		{
			final String ruleActionRaoVariable = context.generateVariable(AbstractRuleActionRAO.class);
			irReferencedRuleActionCondition.setVariable(ruleActionRaoVariable);

			return irReferencedRuleActionCondition;
		}
		else
		{
			final RuleIrLocalVariablesContainer variablesContainer = context.createLocalContainer();
			final String ruleActionRaoVariable = context.generateLocalVariable(variablesContainer, AbstractRuleActionRAO.class);
			irReferencedRuleActionCondition.setVariable(ruleActionRaoVariable);

			final RuleIrNotCondition irNotReferencedRuleActionCondition = new RuleIrNotCondition();
			irNotReferencedRuleActionCondition.setVariablesContainer(variablesContainer);
			irNotReferencedRuleActionCondition.setChildren(Arrays.asList(irReferencedRuleActionCondition));
			return irNotReferencedRuleActionCondition;
		}
	}

	protected RuleConditionsTranslator getRuleConditionsTranslator()
	{
		return ruleConditionsTranslator;
	}

	@Required
	public void setRuleConditionsTranslator(final RuleConditionsTranslator ruleConditionsTranslator)
	{
		this.ruleConditionsTranslator = ruleConditionsTranslator;
	}

	protected RuleCompilerProblemFactory getRuleCompilerProblemFactory()
	{
		return ruleCompilerProblemFactory;
	}

	@Required
	public void setRuleCompilerProblemFactory(final RuleCompilerProblemFactory ruleCompilerProblemFactory)
	{
		this.ruleCompilerProblemFactory = ruleCompilerProblemFactory;
	}
}
