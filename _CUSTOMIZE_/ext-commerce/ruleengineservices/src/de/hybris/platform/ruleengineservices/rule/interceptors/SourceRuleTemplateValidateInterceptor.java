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
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.model.SourceRuleTemplateModel;
import de.hybris.platform.ruleengineservices.rule.data.RuleActionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.services.RuleActionsRegistry;
import de.hybris.platform.ruleengineservices.rule.services.RuleActionsService;
import de.hybris.platform.ruleengineservices.rule.services.RuleConditionsRegistry;
import de.hybris.platform.ruleengineservices.rule.services.RuleConditionsService;
import de.hybris.platform.ruleengineservices.rule.services.RuleService;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleTypeMappingException;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;

import java.util.Map;

import org.springframework.beans.factory.annotation.Required;


/**
 * Load interceptor for {@link SourceRuleTemplateModel} which checks if the representations of conditions and actions
 * are valid.
 */
public class SourceRuleTemplateValidateInterceptor implements ValidateInterceptor
{
	private RuleConditionsService ruleConditionsService;
	private RuleConditionsRegistry ruleConditionsRegistry;
	private RuleActionsService ruleActionsService;
	private RuleActionsRegistry ruleActionsRegistry;
	private RuleService ruleService;

	@Override
	public void onValidate(final Object model, final InterceptorContext ctx) throws InterceptorException
	{
		if (model instanceof SourceRuleTemplateModel)
		{
			final SourceRuleTemplateModel sourceRuleTemplate = (SourceRuleTemplateModel) model;

			Class<? extends AbstractRuleModel> ruleClass;
			try
			{
				ruleClass = ruleService.getRuleTypeFromTemplate(sourceRuleTemplate.getClass());
			}
			catch (final RuleTypeMappingException ce)
			{
				throw new InterceptorException("Cannot find rule type from the template type  " + sourceRuleTemplate.getClass(), ce);
			}

			try
			{
				final Map<String, RuleConditionDefinitionData> conditionDefinitions = ruleConditionsRegistry
						.getConditionDefinitionsForRuleTypeAsMap(ruleClass);

				ruleConditionsService.convertConditionsFromString(sourceRuleTemplate.getConditions(), conditionDefinitions);
			}
			catch (final RuleEngineServiceException ce)
			{
				throw new InterceptorException("Invalid conditions for rule template with code " + sourceRuleTemplate.getCode(), ce);
			}

			try
			{
				final Map<String, RuleActionDefinitionData> actionDefinitions = ruleActionsRegistry
						.getActionDefinitionsForRuleTypeAsMap(ruleClass);
				ruleActionsService.convertActionsFromString(sourceRuleTemplate.getActions(), actionDefinitions);
			}
			catch (final RuleEngineServiceException ce)
			{
				throw new InterceptorException("Invalid actions for rule template with code" + sourceRuleTemplate.getCode(), ce);
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

	public RuleService getRuleService()
	{
		return ruleService;
	}

	@Required
	public void setRuleService(final RuleService ruleService)
	{
		this.ruleService = ruleService;
	}
}
