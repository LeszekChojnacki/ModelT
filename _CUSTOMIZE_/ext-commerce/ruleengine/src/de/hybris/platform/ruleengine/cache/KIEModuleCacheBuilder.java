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

import de.hybris.platform.ruleengine.RuleEngineActionResult;
import de.hybris.platform.ruleengine.RuleEngineService;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;


/**
 * The KIEModuleCacheBuilder builds up the cached entities for a single {@link AbstractRulesModuleModel}.
 * The cache builder is created during rule module initialization (see
 * {@link RuleEngineService#initialize(AbstractRulesModuleModel, String, boolean, boolean, RuleEngineActionResult)}) and
 * stores 'cache-able' entities (i.e. facts, globals etc.) of all rules that have been processed by the cache builder
 * (see {@link #processRule(AbstractRuleEngineRuleModel)}). Once rule module initialization finishes, the cache built up cache is
 * added to the global cache (see {@link RuleEngineCacheService#addToCache(KIEModuleCacheBuilder)}
 *
 */
public interface KIEModuleCacheBuilder
{

	/**
	 * processes the given rule by adding it's cache-able data (e.g. it's globals and "static" facts)
	 * to its internal caching structures.
	 *
	 * @param rule
	 *           the rule for which to add the cache-able data
	 */
	<T extends AbstractRuleEngineRuleModel> void processRule(final T rule);

}
