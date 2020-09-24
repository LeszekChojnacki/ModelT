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
package de.hybris.platform.ruleengine.init.impl;

import static java.util.Objects.nonNull;

import de.hybris.platform.ruleengine.RuleEngineActionResult;
import de.hybris.platform.ruleengine.cache.KIEModuleCacheBuilder;
import de.hybris.platform.ruleengine.cache.RuleEngineCacheService;
import de.hybris.platform.ruleengine.dao.RulesModuleDao;
import de.hybris.platform.ruleengine.drools.KieModuleService;
import de.hybris.platform.ruleengine.enums.DroolsSessionType;
import de.hybris.platform.ruleengine.init.RuleEngineBootstrap;
import de.hybris.platform.ruleengine.init.RuleEngineContainerRegistry;
import de.hybris.platform.ruleengine.init.RuleEngineKieModuleSwapper;
import de.hybris.platform.ruleengine.model.DroolsKIEBaseModel;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengine.model.DroolsKIESessionModel;

import java.util.Collection;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.drools.compiler.kie.builder.impl.KieContainerImpl;
import org.kie.api.KieServices;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Preconditions;


/**
 * Default implementation of {@link de.hybris.platform.ruleengine.init.RuleEngineBootstrap}
 */
public class DefaultRuleEngineBootstrap implements RuleEngineBootstrap<KieServices, KieContainer, DroolsKIEModuleModel>
{

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRuleEngineBootstrap.class);

	private RulesModuleDao rulesModuleDao;
	private RuleEngineKieModuleSwapper ruleEngineKieModuleSwapper;
	private RuleEngineCacheService ruleEngineCacheService;
	private RuleEngineContainerRegistry<ReleaseId, KieContainer> ruleEngineContainerRegistry;
	private KieModuleService kieModuleService;

	@Override
	public KieServices getEngineServices()
	{
		return KieServices.get();
	}

	@Override
	public RuleEngineActionResult startup(final String moduleName)
	{
		Preconditions.checkArgument(nonNull(moduleName), "Module name should be provided");

		final DroolsKIEModuleModel rulesModule = getRulesModuleDao().findByName(moduleName);
		final RuleEngineActionResult result = new RuleEngineActionResult();
		result.setActionFailed(true);
		if (nonNull(rulesModule))
		{
			result.setActionFailed(false);
			final String releaseIdAsString = getRuleEngineKieModuleSwapper().getReleaseId(rulesModule).toExternalForm();
			final Optional<KieModule> restoredKieModule = getKieModuleService().loadKieModule(moduleName, releaseIdAsString);
			if (restoredKieModule.isPresent())
			{
				LOGGER.info("Restored KieModule for name {} and releaseId {}", moduleName, releaseIdAsString);
            final KIEModuleCacheBuilder cache = getRuleEngineCacheService().createKIEModuleCacheBuilder(rulesModule);
            final Collection<DroolsKIEBaseModel> kieBases = rulesModule.getKieBases();
            kieBases.forEach(kb -> getRuleEngineKieModuleSwapper().addRulesToCache(kb, cache));
            final KieContainer kieContainer = getRuleEngineKieModuleSwapper()
            		.initializeNewKieContainer(rulesModule, restoredKieModule.get(), result);
            warmUpRuleEngineContainer(rulesModule, kieContainer);
            activateNewRuleEngineContainer(kieContainer, cache, result, rulesModule, null);
			}
			else
			{
				final Pair<KieModule, KIEModuleCacheBuilder> kieModuleCacheBuilderPair = getRuleEngineKieModuleSwapper()
						.createKieModule(rulesModule, result);
				final KieContainer kieContainer = getRuleEngineKieModuleSwapper()
						.initializeNewKieContainer(rulesModule, kieModuleCacheBuilderPair.getLeft(), result);
				warmUpRuleEngineContainer(rulesModule, kieContainer);
				activateNewRuleEngineContainer(kieContainer, kieModuleCacheBuilderPair.getRight(), result, rulesModule, null);
			}
		}
		return result;
	}

	@Override
	public void activateNewRuleEngineContainer(final KieContainer kieContainer, final KIEModuleCacheBuilder cache,
			final RuleEngineActionResult ruleEngineActionResult, final DroolsKIEModuleModel rulesModule,
			final String deployedReleaseIdVersion)
	{
		LOGGER.debug("Activating Kie Container [{}] for module [{}] and deployed version [{}]", kieContainer, rulesModule.getName(),
				deployedReleaseIdVersion);
		// KieContainerImpl.getReleaseId() != KieContainerImpl.getContainerReleaseId()
		final ReleaseId releaseId = ((KieContainerImpl)kieContainer).getContainerReleaseId();
		final Optional<ReleaseId> deployedReleaseId = getRuleEngineKieModuleSwapper()
				.getDeployedReleaseId(rulesModule, deployedReleaseIdVersion);
		LOGGER.debug("Invoking module swapper process of Kie Module activation for module [{}]", rulesModule.getName());
		final String deployedMvnVersion = getRuleEngineKieModuleSwapper().activateKieModule(rulesModule);
		LOGGER.info("The new rule module with deployedMvnVersion [{}] was activated successfully",
				rulesModule.getDeployedMvnVersion());
		LOGGER.info("Swapping to a newly created kie container [{}]", releaseId);
		getRuleEngineContainerRegistry().setActiveContainer(releaseId, kieContainer);
		getRuleEngineCacheService().addToCache(cache);
		deployedReleaseId.filter(r -> !releaseId.getVersion().equals(r.getVersion()))
				.ifPresent(getRuleEngineContainerRegistry()::removeActiveContainer);
		ruleEngineActionResult.setDeployedVersion(deployedMvnVersion);
	}

	/**
	 * provide the steps to "warm-up" the updated KieContainer. After merging the kieFileSystem this provides the necessary
	 * reorganisation and optimisation of the nodes. The default implementation creates the new stateless KIE session, that
	 * triggers such optimisation. This significantly reduces the first evaluation call timeout
	 *
	 * @param rulesModule
	 * 		instance of {@link DroolsKIEModuleModel}
	 * @param rulesContainer
	 * 		{@link KieContainer} to be optimised
	 */
	@Override
	public void warmUpRuleEngineContainer(final DroolsKIEModuleModel rulesModule, final KieContainer rulesContainer)
	{
		Preconditions.checkArgument(nonNull(rulesContainer), "rulesContainer should not be null");

		final Collection<DroolsKIEBaseModel> kieBases = rulesModule.getKieBases();
		if (CollectionUtils.isNotEmpty(kieBases) && kieBases.size() == 1)
		{
			final DroolsKIEBaseModel kieBase = kieBases.iterator().next();
			final DroolsKIESessionModel defaultKIESession = kieBase.getDefaultKIESession();
			if (nonNull(defaultKIESession))
			{
				final String kieSessionName = defaultKIESession.getName();
				if (DroolsSessionType.STATEFUL.equals(defaultKIESession.getSessionType()))
				{
					LOGGER.debug("Initializing and disposing the session to optimize the tree...");
					rulesContainer.newKieSession(kieSessionName).dispose();
				}
				else
				{
					LOGGER.debug("Initializing the stateless session to optimize the tree...");
					rulesContainer.newStatelessKieSession(kieSessionName);
				}
			}
		}
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

	protected RuleEngineKieModuleSwapper getRuleEngineKieModuleSwapper()
	{
		return ruleEngineKieModuleSwapper;
	}

	@Required
	public void setRuleEngineKieModuleSwapper(final RuleEngineKieModuleSwapper ruleEngineKieModuleSwapper)
	{
		this.ruleEngineKieModuleSwapper = ruleEngineKieModuleSwapper;
	}

	protected RuleEngineCacheService getRuleEngineCacheService()
	{
		return ruleEngineCacheService;
	}

	@Required
	public void setRuleEngineCacheService(final RuleEngineCacheService ruleEngineCacheService)
	{
		this.ruleEngineCacheService = ruleEngineCacheService;
	}

	protected RuleEngineContainerRegistry<ReleaseId, KieContainer> getRuleEngineContainerRegistry()
	{
		return ruleEngineContainerRegistry;
	}

	@Required
	public void setRuleEngineContainerRegistry(
			final RuleEngineContainerRegistry<ReleaseId, KieContainer> ruleEngineContainerRegistry)
	{
		this.ruleEngineContainerRegistry = ruleEngineContainerRegistry;
	}

	protected KieModuleService getKieModuleService()
	{
		return kieModuleService;
	}

	@Required
	public void setKieModuleService(final KieModuleService kieModuleService)
	{
		this.kieModuleService = kieModuleService;
	}
}
