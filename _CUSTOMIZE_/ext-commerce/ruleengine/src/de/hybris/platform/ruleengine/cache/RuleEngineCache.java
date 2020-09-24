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

import de.hybris.platform.ruleengine.model.DroolsKIEBaseModel;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;

import java.util.Map;


/**
 * The RuleEngineCache is designed to cache entities like globals or facts that are "static", i.e. that
 * don't change during the life time of a rules module version.
 * The cache is written to during rule module initialization. For this a
 * It provides methods to create a cache builder which is used to build up the cache for
 */
public interface RuleEngineCache
{
	/**
	 * creates a RuleEngineKIEModuleCacheBuilder object for the given kieModule.
	 *
	 * @return a newly instantiated cache builder for the given kieModule
	 */
	KIEModuleCacheBuilder createKIEModuleCacheBuilder(DroolsKIEModuleModel kieModule);

	/**
	 * adds the given cache builder to the global globals cache.
	 *
	 * @param cacheBuilder
	 *           the cache builder for which to add the cache
	 */
	void addKIEModuleCache(KIEModuleCacheBuilder cacheBuilder);

	/**
	 * returns the cached globals for the given kie base.
	 */
	Map<String, Object> getGlobalsForKIEBase(DroolsKIEBaseModel kieBase);

}
