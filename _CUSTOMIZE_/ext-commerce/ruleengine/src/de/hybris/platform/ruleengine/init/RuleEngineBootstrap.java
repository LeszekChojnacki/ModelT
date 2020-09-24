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
package de.hybris.platform.ruleengine.init;

import de.hybris.platform.ruleengine.RuleEngineActionResult;
import de.hybris.platform.ruleengine.cache.KIEModuleCacheBuilder;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;


/**
 * Rule engine bootstrapping interface
 */
public interface RuleEngineBootstrap<SERVICES, CONTAINER, MODULE extends AbstractRulesModuleModel>
{

	/**
	 * retrieve rule engine infrastructure services handler
	 *
	 * @return a handler for the rule engine system
	 */
	SERVICES getEngineServices();

	/**
	 * starts up the rules engine for a given rules module from scratch (removing currently running and blocking for any rule
	 * evaluation). Primarily intended for a clean rule engine startup during the platform initialization/bootstrap
	 *
	 * @param moduleName
	 * 		the name of the rules module to bottsrpa the engine for
	 * @return instance of {@link RuleEngineActionResult} with a summary of start-up status
	 */
	RuleEngineActionResult startup(String moduleName);

	/**
	 * warms up the rules engine (triggers the internal structure for rules evaluation construction)
	 *
	 * @param rulesModule
	 * 		rules module, instance of {@link AbstractRulesModuleModel}
	 * @param rulesContainer
	 * 		knowledgebase-specific container, to be warmed-up
	 */
	void warmUpRuleEngineContainer(final MODULE rulesModule, final CONTAINER rulesContainer);

	/**
	 * Runs through the new container activation check list and undertakes necessary actions
	 *
	 * @param rulesContainer
	 * 		Rule engine container (knowledgebase-specific)
	 * @param cacheBuilder
	 * 		module CacheBuilder (instance of {@link KIEModuleCacheBuilder}) used to warm-up the cache
	 * @param ruleEngineActionResult
	 * 		instance of {@link RuleEngineActionResult} that collects the results of initialization
	 * @param rulesModule
	 * 		rules module instance
	 * @param deployedReleaseIdVersion
	 * 		currently deployed version of the module, null if none
	 */
	void activateNewRuleEngineContainer(final CONTAINER rulesContainer,
			final KIEModuleCacheBuilder cacheBuilder, final RuleEngineActionResult ruleEngineActionResult, final MODULE rulesModule,
			final String deployedReleaseIdVersion);

}
