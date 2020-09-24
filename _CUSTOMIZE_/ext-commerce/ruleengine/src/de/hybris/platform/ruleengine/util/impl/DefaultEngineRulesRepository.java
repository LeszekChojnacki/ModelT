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
package de.hybris.platform.ruleengine.util.impl;

import static java.util.Objects.nonNull;

import de.hybris.platform.ruleengine.dao.EngineRuleDao;
import de.hybris.platform.ruleengine.dao.RulesModuleDao;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengine.util.EngineRulesRepository;
import de.hybris.platform.ruleengine.versioning.ModuleVersionResolver;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Preconditions;


/**
 * Default implementation of {@link EngineRulesRepository}
 */
public class DefaultEngineRulesRepository implements EngineRulesRepository
{

	private RulesModuleDao rulesModuleDao;
	private EngineRuleDao engineRuleDao;
	private ModuleVersionResolver<DroolsKIEModuleModel> moduleVersionResolver;

	@Override
	public <T extends AbstractRuleEngineRuleModel> boolean checkEngineRuleDeployedForModule(final T engineRule,
			final String moduleName)
	{
		Preconditions.checkArgument(nonNull(engineRule), "Engine rule should be provided");
		Preconditions.checkArgument(nonNull(moduleName), "Module name should be specified");

		final DroolsKIEModuleModel rulesModule = getRulesModuleDao().findByName(moduleName);
		if (nonNull(rulesModule))
		{
			final Optional<Long> deployedModuleVersion = getModuleVersionResolver().getDeployedModuleVersion(rulesModule);
			if (deployedModuleVersion.isPresent())
			{
				final AbstractRuleEngineRuleModel deployedRule = getEngineRuleDao()
						.getRuleByCodeAndMaxVersion(engineRule.getCode(), moduleName,
								deployedModuleVersion.get());
				return Objects.nonNull(deployedRule) && deployedRule.getActive() && deployedRule.equals(engineRule);
			}
		}
		return false;
	}

	@Override
	public <T extends AbstractRuleEngineRuleModel> Collection<T> getDeployedEngineRulesForModule(final String moduleName)
	{
		Preconditions.checkArgument(nonNull(moduleName), "Module name should be specified");
		final DroolsKIEModuleModel rulesModule = getRulesModuleDao().findByName(moduleName);
		if (nonNull(rulesModule))
		{
			final Optional<Long> deployedModuleVersion = getModuleVersionResolver().getDeployedModuleVersion(rulesModule);
			if (deployedModuleVersion.isPresent())
			{
				return getEngineRuleDao().getActiveRulesForVersion(moduleName, deployedModuleVersion.get());
			}
		}
		return Collections.emptyList();
	}

	@Override
	public long countDeployedEngineRulesForModule(final String moduleName)
	{
		return getDeployedEngineRulesForModule(moduleName).size();
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

	protected RulesModuleDao getRulesModuleDao()
	{
		return rulesModuleDao;
	}

	@Required
	public void setRulesModuleDao(final RulesModuleDao rulesModuleDao)
	{
		this.rulesModuleDao = rulesModuleDao;
	}

	protected ModuleVersionResolver<DroolsKIEModuleModel> getModuleVersionResolver()
	{
		return moduleVersionResolver;
	}

	@Required
	public void setModuleVersionResolver(
			final ModuleVersionResolver<DroolsKIEModuleModel> moduleVersionResolver)
	{
		this.moduleVersionResolver = moduleVersionResolver;
	}
}
