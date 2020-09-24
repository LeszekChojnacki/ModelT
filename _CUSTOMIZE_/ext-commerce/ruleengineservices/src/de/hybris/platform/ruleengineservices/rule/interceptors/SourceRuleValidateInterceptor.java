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
package de.hybris.platform.ruleengineservices.rule.interceptors;

import de.hybris.platform.ruleengineservices.RuleEngineServiceException;
import de.hybris.platform.ruleengineservices.model.SourceRuleModel;
import de.hybris.platform.ruleengineservices.rule.data.RuleActionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.services.RuleActionsRegistry;
import de.hybris.platform.ruleengineservices.rule.services.RuleActionsService;
import de.hybris.platform.ruleengineservices.rule.services.RuleConditionsRegistry;
import de.hybris.platform.ruleengineservices.rule.services.RuleConditionsService;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;

import java.util.Map;

import org.springframework.beans.factory.annotation.Required;


/**
 * Load interceptor for {@link SourceRuleModel} which checks if the representations of conditions and actions are valid.
 */
public class SourceRuleValidateInterceptor implements ValidateInterceptor
{
	private RuleConditionsService ruleConditionsService;
	private RuleConditionsRegistry ruleConditionsRegistry;
	private RuleActionsService ruleActionsService;
	private RuleActionsRegistry ruleActionsRegistry;

	@Override
	public void onValidate(final Object model, final InterceptorContext ctx) throws InterceptorException
	{
		if (model instanceof SourceRuleModel)
		{
			final SourceRuleModel sourceRule = (SourceRuleModel) model;

			try
			{
				final Map<String, RuleConditionDefinitionData> conditionDefinitions = ruleConditionsRegistry
						.getConditionDefinitionsForRuleTypeAsMap(sourceRule.getClass());
				ruleConditionsService.convertConditionsFromString(sourceRule.getConditions(), conditionDefinitions);
			}
			catch (final RuleEngineServiceException ce)
			{
				throw new InterceptorException("Invalid conditions for rule with code " + sourceRule.getCode(), ce);
			}

			try
			{
				final Map<String, RuleActionDefinitionData> actionDefinitions = ruleActionsRegistry
						.getActionDefinitionsForRuleTypeAsMap(sourceRule.getClass());
				ruleActionsService.convertActionsFromString(sourceRule.getActions(), actionDefinitions);
			}
			catch (final RuleEngineServiceException ce)
			{
				throw new InterceptorException("Invalid actions for rule with code" + sourceRule.getCode(), ce);
			}
		}
	}

	public RuleConditionsService getRuleConditionsService()
	{
		return ruleConditionsService;
	}

	@Required
	public void setRuleConditionsService(final RuleConditionsService ruleConditionsService)
	{
		this.ruleConditionsService = ruleConditionsService;
	}

	public RuleActionsService getRuleActionsService()
	{
		return ruleActionsService;
	}

	@Required
	public void setRuleActionsService(final RuleActionsService ruleActionsService)
	{
		this.ruleActionsService = ruleActionsService;
	}

	protected RuleConditionsRegistry getRuleConditionsRegistry()
	{
		return ruleConditionsRegistry;
	}

	@Required
	public void setRuleConditionsRegistry(final RuleConditionsRegistry ruleConditionsRegistry)
	{
		this.ruleConditionsRegistry = ruleConditionsRegistry;
	}

	protected RuleActionsRegistry getRuleActionsRegistry()
	{
		return ruleActionsRegistry;
	}

	@Required
	public void setRuleActionsRegistry(final RuleActionsRegistry ruleActionsRegistry)
	{
		this.ruleActionsRegistry = ruleActionsRegistry;
	}

}
