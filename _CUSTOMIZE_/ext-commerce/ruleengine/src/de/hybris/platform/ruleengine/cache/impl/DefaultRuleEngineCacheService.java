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
package de.hybris.platform.ruleengine.cache.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Maps.newHashMap;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static org.apache.commons.collections.MapUtils.isNotEmpty;

import de.hybris.platform.ruleengine.RuleEvaluationContext;
import de.hybris.platform.ruleengine.cache.KIEModuleCacheBuilder;
import de.hybris.platform.ruleengine.cache.RuleEngineCache;
import de.hybris.platform.ruleengine.cache.RuleEngineCacheService;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengine.model.DroolsRuleEngineContextModel;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * The DefaultRuleEngineCacheService of the RuleEngineCacheService provides methods to add and retrieve the cached
 * entities from the global cache.
 */
public class DefaultRuleEngineCacheService implements RuleEngineCacheService
{

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRuleEngineCacheService.class);

	private RuleEngineCache ruleEngineCache;

	@Override
	public KIEModuleCacheBuilder createKIEModuleCacheBuilder(final DroolsKIEModuleModel kieModule)
	{
		return getRuleEngineCache().createKIEModuleCacheBuilder(kieModule);
	}

	@Override
	public void addToCache(final KIEModuleCacheBuilder cacheBuilder)
	{
		getRuleEngineCache().addKIEModuleCache(cacheBuilder);
	}

	@Override
	public void provideCachedEntities(final RuleEvaluationContext context)
	{
		validateParameterNotNull(context, "context must not be null");
		checkArgument(context.getRuleEngineContext() instanceof DroolsRuleEngineContextModel,
					 "rule engine context must be of type DroolsRuleEngineContext");
		final DroolsRuleEngineContextModel engineContext = (DroolsRuleEngineContextModel) context.getRuleEngineContext();
		validateParameterNotNull(engineContext.getKieSession().getKieBase(),
					 "rule engine context must have a kie session and kie base set");
		// make a mutable copy as the context might still be modified by other parties
		final Map<String, Object> globalsForKIEBase = getRuleEngineCache()
					 .getGlobalsForKIEBase(engineContext.getKieSession().getKieBase());

		Map<String, Object> globals = newHashMap();
		if (isNotEmpty(globalsForKIEBase))
		{
			globals = newHashMap(globalsForKIEBase);
		}
		else
		{
			LOGGER.warn(
					"Globals map for evaluation context [{}] is empty. Either there are no globals defined in the actions or the cache is broken.",
						 context.getRuleEngineContext().getName());
		}
		context.setGlobals(globals);
	}

	protected RuleEngineCache getRuleEngineCache()
	{
		return ruleEngineCache;
	}

	@Required
	public void setRuleEngineCache(final RuleEngineCache globalsCache)
	{
		this.ruleEngineCache = globalsCache;
	}
}
