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
package de.hybris.platform.ruleengineservices.rule.services.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.ruleengine.dao.EngineRuleDao;
import de.hybris.platform.ruleengine.enums.RuleType;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengine.model.DroolsRuleEngineContextModel;
import de.hybris.platform.ruleengine.strategies.RuleEngineContextFinderStrategy;
import de.hybris.platform.ruleengine.versioning.ModuleVersionResolver;
import de.hybris.platform.ruleengineservices.enums.RuleStatus;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.model.AbstractRuleTemplateModel;
import de.hybris.platform.ruleengineservices.rule.dao.RuleDao;
import de.hybris.platform.ruleengineservices.rule.services.RuleService;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleTypeMappingStrategy;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.keygenerator.KeyGenerator;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Lists;


/**
 * Default Implementation of {@link RuleService}
 */
public class DefaultRuleService implements RuleService
{
	private RuleDao ruleDao;
	private EngineRuleDao engineRuleDao;
	private ModelService modelService;
	private KeyGenerator sourceRuleCodeGenerator;
	private CommonI18NService commonI18NService;
	private RuleTypeMappingStrategy ruleTypeMappingStrategy;
	private RuleEngineContextFinderStrategy ruleEngineContextFinderStrategy;
	private ModuleVersionResolver<DroolsKIEModuleModel> moduleVersionResolver;

	@Override
	public <T extends AbstractRuleModel> List<T> getAllRules()
	{
		return getRuleDao().findAllRules();
	}

	@Override
	public <T extends AbstractRuleModel> List<T> getAllRulesForType(final Class ruleType)
	{
		return getRuleDao().findAllRulesByType(ruleType);
	}

	@Override
	public <T extends AbstractRuleModel> List<T> getAllActiveRules()
	{
		return getRuleDao().findAllActiveRules();
	}

	@Override
	public List<AbstractRuleModel> getAllActiveRulesForType(final Class ruleType)
	{
		return getRuleDao().findAllActiveRulesByType(ruleType);
	}

	/**
	 * @deprecated since 1811
	 */
	@Override
	@Deprecated
	public <T extends AbstractRuleModel> List<T> getAllToBePublishedRules()
	{
		return getRuleDao().findAllToBePublishedRules();
	}

	/**
	 * @deprecated since 1811
	 */
	@Override
	@Deprecated
	public <T extends AbstractRuleModel> List<T> getAllToBePublishedRulesForType(final Class ruleType)
	{
		return getRuleDao().findAllToBePublishedRulesByType(ruleType);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends AbstractRuleModel> List<T> getActiveRulesForCatalogVersionAndRuleType(
			final CatalogVersionModel catalogVersion, final RuleType ruleType)
	{
		validateParameterNotNullStandardMessage("catalogVersion", catalogVersion);
		validateParameterNotNullStandardMessage("ruleType", ruleType);

		final Optional<DroolsRuleEngineContextModel> ruleEngineContext = getRuleEngineContextFinderStrategy()
				.getRuleEngineContextForCatalogVersions(singletonList(catalogVersion), ruleType);

		if (ruleEngineContext.isPresent())
		{
			validateParameterNotNull(ruleEngineContext.get().getKieSession().getKieBase(),
					"rule engine context must have a kie session and kie base set");

			final DroolsKIEModuleModel kieModule = ruleEngineContext.get().getKieSession().getKieBase().getKieModule();

			final Optional<Long> deployedModuleVersion = getModuleVersionResolver().getDeployedModuleVersion(kieModule);

			if (deployedModuleVersion.isPresent())
			{
				final List<AbstractRuleEngineRuleModel> activeRules = getEngineRuleDao().getActiveRulesForVersion(kieModule.getName(),
						deployedModuleVersion.get());

				return activeRules.stream().map(r -> (T) r.getSourceRule()).collect(toList());
			}
		}

		return emptyList();
	}

	@Override
	public AbstractRuleModel getRuleForCode(final String code)
	{
		return getRuleDao().findRuleByCode(code);
	}

	@Override
	public <T extends AbstractRuleModel> List<T> getAllRulesForCode(final String code)
	{
		validateParameterNotNull(code, "code must not be null");

		return getRuleDao().findAllRuleVersionsByCode(code);
	}

	@Override
	public <T extends AbstractRuleModel> List<T> getAllRulesForCodeAndStatus(final String code, final RuleStatus... ruleStatuses)
	{
		validateParameterNotNull(code, "code must not be null");
		validateParameterNotNull(ruleStatuses, "rule status must not be null");

		return getRuleDao().findAllRuleVersionsByCodeAndStatuses(code, ruleStatuses);
	}

	@Override
	public <T extends AbstractRuleModel> List<T> getAllRulesForStatus(final RuleStatus... ruleStatuses)
	{
		validateParameterNotNull(ruleStatuses, "rule status must not be null");

		return getRuleDao().findAllRulesWithStatuses(ruleStatuses);
	}

	@Override
	public <T extends AbstractRuleModel> T createRuleFromTemplate(final AbstractRuleTemplateModel ruleTemplate)
	{
		validateParameterNotNull(ruleTemplate, "rule template must not be null");
		validateParameterNotNull(ruleTemplate.getCode(), "rule template code must not be null");

		final String newRuleCode = ruleTemplate.getCode() + "-" + (String) getSourceRuleCodeGenerator().generate();
		return createRuleFromTemplate(newRuleCode, ruleTemplate);
	}

	@Override
	public <T extends AbstractRuleModel> T createRuleFromTemplate(final String newRuleCode,
			final AbstractRuleTemplateModel ruleTemplate)
	{
		validateParameterNotNull(newRuleCode, "rule code must not be null");
		validateParameterNotNull(ruleTemplate, "rule template must not be null");

		final Class<?> ruleType = getRuleTypeFromTemplate(ruleTemplate.getClass());
		final T rule = (T) getModelService().clone(ruleTemplate, ruleType);

		rule.setStatus(RuleStatus.UNPUBLISHED);
		rule.setCode(newRuleCode);

		getModelService().save(rule);

		return rule;
	}

	@Override
	public AbstractRuleModel cloneRule(final AbstractRuleModel source)
	{
		validateParameterNotNull(source, "rule must not be null");

		final String code = (String) getSourceRuleCodeGenerator().generate();
		return cloneRule(source.getCode() + "-" + code, source);
	}

	@Override
	public AbstractRuleModel cloneRule(final String newRuleCode, final AbstractRuleModel source)
	{
		validateParameterNotNull(newRuleCode, "rule code must not be null");
		validateParameterNotNull(source, "rule must not be null");

		final AbstractRuleModel target = getModelService().clone(source);
		target.setCode(newRuleCode);
		target.setUuid(null);
		target.setStatus(RuleStatus.UNPUBLISHED);
		target.setVersion(0L);
		target.setRulesModules(Lists.newArrayList());

		getModelService().save(target);

		return target;
	}

	@Override
	public Class<? extends AbstractRuleModel> getRuleTypeFromTemplate(
			final Class<? extends AbstractRuleTemplateModel> templateType)
	{
		return getRuleTypeMappingStrategy().findRuleType(templateType);
	}

	@Override
	public RuleType getEngineRuleTypeForRuleType(final Class<?> type)
	{
		final RuleType ruleType = getRuleDao().findEngineRuleTypeByRuleType(type);
		if (ruleType == null)
		{
			return RuleType.DEFAULT;
		}
		return ruleType;
	}

	protected RuleDao getRuleDao()
	{
		return ruleDao;
	}

	@Required
	public void setRuleDao(final RuleDao ruleDao)
	{
		this.ruleDao = ruleDao;
	}

	public EngineRuleDao getEngineRuleDao()
	{
		return engineRuleDao;
	}

	@Required
	public void setEngineRuleDao(final EngineRuleDao engineRuleDao)
	{
		this.engineRuleDao = engineRuleDao;
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

	protected KeyGenerator getSourceRuleCodeGenerator()
	{
		return sourceRuleCodeGenerator;
	}

	@Required
	public void setSourceRuleCodeGenerator(final KeyGenerator sourceRuleCodeGenerator)
	{
		this.sourceRuleCodeGenerator = sourceRuleCodeGenerator;
	}

	protected CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	protected RuleTypeMappingStrategy getRuleTypeMappingStrategy()
	{
		return ruleTypeMappingStrategy;
	}

	@Required
	public void setRuleTypeMappingStrategy(final RuleTypeMappingStrategy ruleTypeMappingStrategy)
	{
		this.ruleTypeMappingStrategy = ruleTypeMappingStrategy;
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

	protected RuleEngineContextFinderStrategy getRuleEngineContextFinderStrategy()
	{
		return ruleEngineContextFinderStrategy;
	}

	@Required
	public void setRuleEngineContextFinderStrategy(final RuleEngineContextFinderStrategy ruleEngineContextFinderStrategy)
	{
		this.ruleEngineContextFinderStrategy = ruleEngineContextFinderStrategy;
	}
}
