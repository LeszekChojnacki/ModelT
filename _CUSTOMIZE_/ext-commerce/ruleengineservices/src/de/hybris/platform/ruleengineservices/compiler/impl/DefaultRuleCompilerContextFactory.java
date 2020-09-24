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
package de.hybris.platform.ruleengineservices.compiler.impl;

import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContextFactory;
import de.hybris.platform.ruleengineservices.compiler.RuleIrVariablesGenerator;
import de.hybris.platform.ruleengineservices.maintenance.RuleCompilationContext;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.rule.data.RuleActionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.services.RuleActionsRegistry;
import de.hybris.platform.ruleengineservices.rule.services.RuleConditionsRegistry;

import java.util.Map;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link RuleCompilerContextFactory}.
 */
public class DefaultRuleCompilerContextFactory implements RuleCompilerContextFactory<DefaultRuleCompilerContext>
{

	private RuleConditionsRegistry ruleConditionsRegistry;
	private RuleActionsRegistry ruleActionsRegistry;

	@Override
	public DefaultRuleCompilerContext createContext(final RuleCompilationContext ruleCompilationContext,
			final AbstractRuleModel rule, final String moduleName, final RuleIrVariablesGenerator variablesGenerator)
	{
		final DefaultRuleCompilerContext context = new DefaultRuleCompilerContext(ruleCompilationContext, rule, moduleName,
				variablesGenerator);
		populateDefinitionsForRule(rule, context);
		return context;
	}

	protected void populateDefinitionsForRule(final AbstractRuleModel rule, final DefaultRuleCompilerContext context)
	{
		final Map<String, RuleConditionDefinitionData> conditionDefinitions = ruleConditionsRegistry
				.getConditionDefinitionsForRuleTypeAsMap(rule.getClass());
		context.getConditionDefinitions().putAll(conditionDefinitions);

		final Map<String, RuleActionDefinitionData> actionDefinitions = ruleActionsRegistry
				.getActionDefinitionsForRuleTypeAsMap(rule.getClass());
		context.getActionDefinitions().putAll(actionDefinitions);
	}

	public RuleConditionsRegistry getRuleConditionsRegistry()
	{
		return ruleConditionsRegistry;
	}

	@Required
	public void setRuleConditionsRegistry(final RuleConditionsRegistry ruleConditionsRegistry)
	{
		this.ruleConditionsRegistry = ruleConditionsRegistry;
	}

	public RuleActionsRegistry getRuleActionsRegistry()
	{
		return ruleActionsRegistry;
	}

	@Required
	public void setRuleActionsRegistry(final RuleActionsRegistry ruleActionsRegistry)
	{
		this.ruleActionsRegistry = ruleActionsRegistry;
	}
}
