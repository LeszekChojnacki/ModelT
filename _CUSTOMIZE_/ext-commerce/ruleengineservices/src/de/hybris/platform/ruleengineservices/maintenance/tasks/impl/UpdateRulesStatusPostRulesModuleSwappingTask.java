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

import static de.hybris.platform.ruleengine.util.RuleEngineUtils.isDroolsKieModuleDeployed;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;

import de.hybris.platform.ruleengine.RuleEngineActionResult;
import de.hybris.platform.ruleengine.dao.EngineRuleDao;
import de.hybris.platform.ruleengine.init.tasks.PostRulesModuleSwappingTask;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.ruleengine.util.RuleMappings;
import de.hybris.platform.ruleengineservices.enums.RuleStatus;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.rule.services.RuleService;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Sets;


/**
 * Post rules module swapping task, updating the source rules statuses
 */
public class UpdateRulesStatusPostRulesModuleSwappingTask implements PostRulesModuleSwappingTask
{

	private RuleService ruleService;
	private ModelService modelService;
	private EngineRuleDao engineRuleDao;

	@Override
	public boolean execute(final RuleEngineActionResult result)
	{
		if (!result.isActionFailed())
		{
			final Collection<String> modifiedRuleCodes = result.getExecutionContext().getModifiedRuleCodes();

			markRulesAsPublished(modifiedRuleCodes);

			updateRuleStatusIfInactive(modifiedRuleCodes);

			return true;
		}
		else
		{
			if (nonNull(result.getExecutionContext()) && MapUtils.isNotEmpty(result.getExecutionContext().getRuleVersions()))
			{
				final Set<AbstractRuleEngineRuleModel> compiledRules = getEngineRulesToRevert(
						result.getExecutionContext().getRuleVersions(),	result.getModuleName());

				getModelService().removeAll(compiledRules);
			}

			return false;
		}
	}

	protected Set<AbstractRuleEngineRuleModel> getEngineRulesToRevert(final Map<String, Long> ruleVersions,
			final String moduleName)
	{
		return ruleVersions.entrySet().stream().filter(e -> nonNull(e.getValue()))
				.map(e -> getEngineRuleDao().getActiveRuleByCodeAndMaxVersion(e.getKey(), moduleName, e.getValue())).collect(toSet());
	}

	protected void markRulesAsPublished(final Collection<String> ruleCodes)
	{
		final Set<AbstractRuleModel> compiledSourceRules = ruleCodes.stream().map(code -> getRuleService().getRuleForCode(code))
				.filter(Objects::nonNull).collect(toSet());

		updateRulesStatus(compiledSourceRules, RuleStatus.PUBLISHED);
	}

	protected void updateRulesStatus(final Collection<AbstractRuleModel> rules, final RuleStatus status)
	{
		rules.forEach(r -> r.setStatus(status));
		getModelService().saveAll(rules);
	}

	/**
	 * This method is used to update status of rules to {@link RuleStatus#INACTIVE} that have been in {@link RuleStatus#PUBLISHED}
	 * status, but their status has been affected after recent deployment or undeployment
	 * actions
	 * @param ruleCodes
	 *           list or rule codes
	 */
	protected void updateRuleStatusIfInactive(final Collection<String> ruleCodes)
	{
		final Set<AbstractRuleModel> publishedSourceRules = ruleCodes.stream()
				.flatMap(
						code -> getRuleService().getAllRulesForCodeAndStatus(code, RuleStatus.PUBLISHED, RuleStatus.INACTIVE).stream())
				.collect(toSet());

		final Set<AbstractRuleEngineRuleModel> activeEngineRules = publishedSourceRules.stream()
				.flatMap(s -> s.getEngineRules().stream()).
				filter(AbstractRuleEngineRuleModel::getActive).collect(toSet());

		final Set<AbstractRuleModel> publishedRules = Sets.newHashSet();
		for (final AbstractRuleEngineRuleModel droolsRule : activeEngineRules)
		{
			final DroolsKIEModuleModel rulesModule = RuleMappings.module((DroolsRuleModel) droolsRule);
			getModelService().refresh(rulesModule);

			if (!isDroolsKieModuleDeployed(rulesModule))
			{
				continue;
			}

			final AbstractRuleEngineRuleModel deployedDroolsRule = getEngineRuleDao()
					.getRuleByCodeAndMaxVersion(droolsRule.getCode(), rulesModule.getName(), rulesModule.getVersion());

			getModelService().refresh(deployedDroolsRule);
			getModelService().refresh(droolsRule);

			if (deployedDroolsRule.getActive() && deployedDroolsRule.getVersion().equals(droolsRule.getVersion()))
			{
				final AbstractRuleModel sourceRule = deployedDroolsRule.getSourceRule();
				if (sourceRule.getStatus().equals(RuleStatus.INACTIVE))
				{
					publishedRules.add(sourceRule);
				}
				else if (sourceRule.getStatus().equals(RuleStatus.PUBLISHED))
				{
					publishedSourceRules.remove(sourceRule);
				}
			}
		}

		publishedSourceRules.forEach(r -> checkIfPublishedAndUpdateStatus(publishedRules, r));
		getModelService().saveAll(publishedSourceRules);
	}

	protected void checkIfPublishedAndUpdateStatus(final Set<AbstractRuleModel> publishedRules, final AbstractRuleModel rule)
	{
		if (publishedRules.contains(rule))
		{
			rule.setStatus(RuleStatus.PUBLISHED);
		}
		else
		{
			rule.setStatus(RuleStatus.INACTIVE);
		}
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

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected EngineRuleDao getEngineRuleDao()
	{
		return engineRuleDao;
	}

	@Required
	public void setEngineRuleDao(final EngineRuleDao engineRuleDao)
	{
		this.engineRuleDao = engineRuleDao;
	}

}
