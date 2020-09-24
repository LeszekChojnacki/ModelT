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
package de.hybris.platform.droolsruleengineservices.versioning.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import de.hybris.platform.ruleengine.RuleEngineService;
import de.hybris.platform.ruleengine.dao.EngineRuleDao;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;
import de.hybris.platform.ruleengine.model.DroolsKIEBaseModel;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.ruleengine.versioning.ModuleVersionResolver;
import de.hybris.platform.ruleengine.versioning.ModuleVersioningService;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;


/**
 * Drools specific implementation of module versioning service
 */
public class DroolsModuleVersioningService implements ModuleVersioningService
{
	private static final Long DEFAULT_VERSION = 0L;
	public static final String ENGINE_RULE_MODEL_NULL_MESSAGE = "AbstractRuleEngineRuleModel instance is not expected to be null here";

	private RuleEngineService ruleEngineService;
	private EngineRuleDao engineRuleDao;
	private ModelService modelService;
	private ModuleVersionResolver<DroolsKIEModuleModel> moduleVersionResolver;

	@Override
	public Optional<Long> getModuleVersion(final AbstractRuleEngineRuleModel ruleModel)
	{
		checkArgument(nonNull(ruleModel), ENGINE_RULE_MODEL_NULL_MESSAGE);

		Optional<Long> moduleVersion = empty();
		if (ruleModel instanceof DroolsRuleModel)
		{
			final DroolsRuleModel droolsRuleModel = (DroolsRuleModel) ruleModel;
			final DroolsKIEBaseModel kieBase = droolsRuleModel.getKieBase();
			if (nonNull(kieBase))
			{
				final DroolsKIEModuleModel moduleModel = kieBase.getKieModule();
				if (nonNull(moduleModel))
				{
					moduleVersion = getModuleVersionResolver().getDeployedModuleVersion(moduleModel);
				}
			}
		}
		return moduleVersion;
	}

	@Override
	public void assertRuleModuleVersion(final AbstractRuleEngineRuleModel engineRule, final AbstractRulesModuleModel rulesModule)
	{
		checkArgument(nonNull(engineRule), ENGINE_RULE_MODEL_NULL_MESSAGE);
		checkArgument(nonNull(rulesModule), "AbstractRulesModuleModel instance is not expected to be null here");

		Long version = engineRule.getVersion();
		if (isNull(version))
		{
			version = 0L;
		}
		getModelService().refresh(rulesModule);
		if (rulesModule.getVersion() <= version)
		{
			rulesModule.setVersion(version);
			getModelService().save(rulesModule);
		}
	}

	@Override
	public void assertRuleModuleVersion(final AbstractRulesModuleModel ruleModule,
			final Set<AbstractRuleEngineRuleModel> rules)
	{
		checkArgument(nonNull(ruleModule), ENGINE_RULE_MODEL_NULL_MESSAGE);

		final Long moduleVersion = ruleModule.getVersion();
		final Long currentRulesVersion = getCurrentRulesVersion(ruleModule);
		if (nonNull(moduleVersion) && nonNull(currentRulesVersion) && currentRulesVersion.longValue() > moduleVersion.longValue())
		{
			ruleModule.setVersion(currentRulesVersion);
		}
		if (isNotEmpty(rules))
		{
			final Set<DroolsRuleModel> droolRules = rules.stream().map(r -> (DroolsRuleModel) r).collect(toSet());
			droolRules.stream().max(comparing(DroolsRuleModel::getVersion))
					.ifPresent(r -> setNewVersionIfApplicable(ruleModule, currentRulesVersion, r.getVersion()));
		}
	}

	@Override
	public Optional<Long> getDeployedModuleVersionForRule(final String ruleCode, final String moduleName)
	{
		validateParameterNotNull(ruleCode, "Rule code should be provided here");
		validateParameterNotNull(moduleName, "Module name must be provided here");

		final AbstractRuleEngineRuleModel rule = getEngineRuleDao().getRuleByCode(ruleCode, moduleName);
		if (nonNull(rule) && rule instanceof DroolsRuleModel)
		{
			final DroolsRuleModel droolsRule = (DroolsRuleModel) rule;
			final DroolsKIEBaseModel kieBase = droolsRule.getKieBase();
			if (nonNull(kieBase))
			{
				final DroolsKIEModuleModel kieModule = kieBase.getKieModule();
				if (nonNull(kieModule))
				{
					return getModuleVersionResolver().getDeployedModuleVersion(kieModule);
				}
			}
		}
		return empty();
	}

	protected Long getCurrentRulesVersion(final AbstractRulesModuleModel rulesModule)
	{
		Long maxRuleVersion = getEngineRuleDao().getCurrentRulesSnapshotVersion(rulesModule);
		if (isNull(maxRuleVersion))
		{
			maxRuleVersion = DEFAULT_VERSION;
		}

		return maxRuleVersion;
	}

	protected void setNewVersionIfApplicable(final AbstractRulesModuleModel rulesModule, final Long currentRulesVersion,
			final Long newVersion)
	{
		Long moduleVersion = null;
		if (nonNull(newVersion))
		{
			if (isNull(rulesModule.getVersion()) && nonNull(currentRulesVersion))
			{
				moduleVersion = currentRulesVersion;
			}
			if (isNull(currentRulesVersion) || newVersion > currentRulesVersion)
			{
				moduleVersion = newVersion;
			}
			if (nonNull(moduleVersion) && !moduleVersion.equals(rulesModule.getVersion()))
			{
				rulesModule.setVersion(moduleVersion);
			}
		}
	}

	protected RuleEngineService getRuleEngineService()
	{
		return ruleEngineService;
	}

	@Required
	public void setRuleEngineService(final RuleEngineService ruleEngineService)
	{
		this.ruleEngineService = ruleEngineService;
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

	protected ModuleVersionResolver<DroolsKIEModuleModel> getModuleVersionResolver()
	{
		return moduleVersionResolver;
	}

	@Required
	public void setModuleVersionResolver(final ModuleVersionResolver<DroolsKIEModuleModel> moduleVersionResolver)
	{
		this.moduleVersionResolver = moduleVersionResolver;
	}
}
