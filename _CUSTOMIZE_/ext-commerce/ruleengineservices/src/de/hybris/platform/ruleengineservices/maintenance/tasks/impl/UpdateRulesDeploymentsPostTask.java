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
package de.hybris.platform.ruleengineservices.maintenance.tasks.impl;

import de.hybris.platform.ruleengine.RuleEngineActionResult;
import de.hybris.platform.ruleengine.init.tasks.PostRulesModuleSwappingTask;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.ruleengine.strategies.RulesModuleResolver;
import de.hybris.platform.ruleengineservices.enums.RuleStatus;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.rule.services.RuleService;
import de.hybris.platform.servicelayer.model.ModelService;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toSet;

/**
 * Post rules module swapping task, updating the source rules deployments
 */
public class UpdateRulesDeploymentsPostTask implements PostRulesModuleSwappingTask
{
	private RulesModuleResolver rulesModuleResolver;

	private ModelService modelService;

	private RuleService ruleService;

	@Override
	public boolean execute(final RuleEngineActionResult result)
	{
		if (!result.isActionFailed())
		{
			final Collection<String> modifiedRuleCodes = result.getExecutionContext().getModifiedRuleCodes();

			final Set<AbstractRuleModel> rules = getRulesForCodes(modifiedRuleCodes);

			rules.forEach(r -> r.setRulesModules(calculateDeployments(r)));

			getModelService().saveAll(rules);
		}

		return true;
	}

	protected Set<AbstractRuleModel> getRulesForCodes(final Collection<String> ruleCodes)
	{
		return ruleCodes.stream().map(code -> getRuleService().getAllRulesForCode(code)).flatMap(Collection::stream)
				.filter(Objects::nonNull).collect(toSet());
	}

	protected List<AbstractRulesModuleModel> calculateDeployments(final AbstractRuleModel rule)
	{
		if (RuleStatus.PUBLISHED.equals(rule.getStatus()))
		{
			refreshRuleModules(rule);

			return getRulesModuleResolver().lookupForRulesModules(rule);
		}

		return emptyList();
	}

	protected void refreshRuleModules(final AbstractRuleModel rule)
	{
		rule.getEngineRules().stream()
				.filter(DroolsRuleModel.class::isInstance).map(r -> ((DroolsRuleModel) r).getKieBase().getKieModule())
				.forEach(getModelService()::refresh);
	}

	protected RulesModuleResolver getRulesModuleResolver()
	{
		return rulesModuleResolver;
	}

	@Required
	public void setRulesModuleResolver(final RulesModuleResolver rulesModuleResolver)
	{
		this.rulesModuleResolver = rulesModuleResolver;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected RuleService getRuleService()
	{
		return ruleService;
	}

	@Required
	public void setRuleService(final RuleService ruleService)
	{
		this.ruleService = ruleService;
	}
}
