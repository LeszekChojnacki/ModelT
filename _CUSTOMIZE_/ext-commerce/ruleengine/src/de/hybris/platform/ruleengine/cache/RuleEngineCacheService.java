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
package de.hybris.platform.ruleengine.cache;

import de.hybris.platform.ruleengine.RuleEvaluationContext;
import de.hybris.platform.ruleengine.impl.DefaultPlatformRuleEngineService;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;


/**
 * The RuleEngineCacheService is designed to facilitate the caching of entities
 * like globals or facts that are "static", i.e. that don't change during the life time
 * of a rules module version.
 * The cache service allows to create cache builders for rules module (see
 * {@link #createKIEModuleCacheBuilder(DroolsKIEModuleModel)}. The {@link KIEModuleCacheBuilder} is then filled using
 * its {@link KIEModuleCacheBuilder#processRule(AbstractRuleEngineRuleModel)}
 * during rule module initialization. The {@link #addToCache(KIEModuleCacheBuilder)} is used to add the cache to the
 * cache service (and replace any previously existing cache for that module).
 * During rule evaluation the {@link #provideCachedEntities(RuleEvaluationContext)} is invoked to enhance the given rule
 * evaluation context with the cached entities.
 */
public interface RuleEngineCacheService
{
	/**
	 * creates a KIEModuleCacheBuilder object for the given kieModule. The cache builder processes
	 * the kie module's rules (see
	 * {@link KIEModuleCacheBuilder#processRule(AbstractRuleEngineRuleModel)}.
	 * In order to add the cache for the module use {@link #addToCache(KIEModuleCacheBuilder)}.
	 *
	 * @param kieModule
	 * 			 the instance of {@link DroolsKIEModuleModel} for which to create the cached items
	 * @return a newly instantiated cache builder for the given kieModule
	 */
	KIEModuleCacheBuilder createKIEModuleCacheBuilder(DroolsKIEModuleModel kieModule);

	/**
	 * adds the cached entities of the given cache builder to the global caching structure.
	 *
	 * @param cacheBuilder
	 * 			 the cache builder to use
	 */
	void addToCache(KIEModuleCacheBuilder cacheBuilder);

	/**
	 * is invoked during
	 * {@link DefaultPlatformRuleEngineService#evaluate(de.hybris.platform.ruleengine.RuleEvaluationContext)} and adds
	 * any cached entities (such as globals or facts) to the given context.
	 *
	 * @param context
	 * 			 the context object to enhance with facts
	 */
	void provideCachedEntities(RuleEvaluationContext context);

}
