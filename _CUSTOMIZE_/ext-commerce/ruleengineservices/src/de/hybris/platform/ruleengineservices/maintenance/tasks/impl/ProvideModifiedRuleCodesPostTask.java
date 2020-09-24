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

import com.google.common.collect.Lists;
import de.hybris.platform.ruleengine.RuleEngineActionResult;
import de.hybris.platform.ruleengine.dao.EngineRuleDao;
import de.hybris.platform.ruleengine.init.tasks.PostRulesModuleSwappingTask;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengine.versioning.ModuleVersionResolver;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;

/**
 * Post rules module swapping task, filling in modified rule codes for the @{@link de.hybris.platform.ruleengine.ExecutionContext}
 */
public class ProvideModifiedRuleCodesPostTask implements PostRulesModuleSwappingTask
{
	private ModuleVersionResolver<DroolsKIEModuleModel> moduleVersionResolver;

	private EngineRuleDao engineRuleDao;

	@Override
	public boolean execute(final RuleEngineActionResult result)
	{
		if (!result.isActionFailed())
		{
			final Collection<String> modifiedRuleCodes = getModifiedRuleCodes(result);
			result.getExecutionContext().setModifiedRuleCodes(modifiedRuleCodes);

			return true;
		}

		return false;
	}

	protected Collection<String> getModifiedRuleCodes(final RuleEngineActionResult result)
	{
		final Long deployedVersion = getModuleVersionResolver().extractModuleVersion(result.getModuleName(),
				result.getDeployedVersion());
		final Long oldVersion = getModuleVersionResolver().extractModuleVersion(result.getModuleName(), result.getOldVersion());

		List<AbstractRuleEngineRuleModel> oldVersionRules = Lists.newArrayList();
		if (nonNull(oldVersion))
		{
			oldVersionRules = getEngineRuleDao().getRulesForVersion(result.getModuleName(), oldVersion);
		}

		List<AbstractRuleEngineRuleModel> deployedVersionRules = Lists.newArrayList();
		if (nonNull(deployedVersion))
		{
			deployedVersionRules = getEngineRuleDao().getRulesForVersion(result.getModuleName(), deployedVersion);
		}

		final Collection<String> modifiedRuleCodes = Stream.concat(oldVersionRules.stream(), deployedVersionRules.stream())
				.map(AbstractRuleEngineRuleModel::getCode).collect(toSet());

		for (final AbstractRuleEngineRuleModel deployedVersionRule : deployedVersionRules)
		{
			if (oldVersionRules.stream().anyMatch(r -> rulesAreEqual(r, deployedVersionRule)))
			{
				modifiedRuleCodes.remove(deployedVersionRule.getCode());
			}
		}

		return modifiedRuleCodes;
	}

	protected boolean rulesAreEqual(final AbstractRuleEngineRuleModel rule1, final AbstractRuleEngineRuleModel rule2)
	{
		boolean areEqual = false;
		if (nonNull(rule1) && nonNull(rule2) && nonNull(rule1.getVersion()))
		{
			areEqual = rule1.getCode().equals(rule2.getCode()) && rule1.getVersion().equals(rule2.getVersion());
		}
		return areEqual;
	}

	protected ModuleVersionResolver<DroolsKIEModuleModel> getModuleVersionResolver()
	{
		return moduleVersionResolver;
	}

	@Required
	public void setModuleVersionResolver(final ModuleVersionResolver<DroolsKIEModuleModel> moduleVersionResolver)
	{
		this.moduleVersionResolver = moduleVersionResolver;
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
