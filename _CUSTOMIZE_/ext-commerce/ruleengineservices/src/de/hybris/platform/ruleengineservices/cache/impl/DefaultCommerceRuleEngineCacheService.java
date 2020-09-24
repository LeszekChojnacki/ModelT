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
package de.hybris.platform.ruleengineservices.cache.impl;

import de.hybris.platform.ruleengine.RuleEvaluationContext;
import de.hybris.platform.ruleengine.cache.KIEModuleCacheBuilder;
import de.hybris.platform.ruleengine.cache.impl.DefaultRuleEngineCacheService;
import de.hybris.platform.ruleengine.model.DroolsRuleEngineContextModel;
import de.hybris.platform.ruleengineservices.cache.CommerceRuleEngineCache;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Required;


/**
 * The DefaultCommerceRuleEngineCacheService enhances the DefaultRuleEngineCacheService by allowing to add "fact
 * templates" to the cache.
 */
public class DefaultCommerceRuleEngineCacheService extends DefaultRuleEngineCacheService
{
	private CommerceRuleEngineCache commerceRuleEngineCache;

	@Override
	public void addToCache(final KIEModuleCacheBuilder cacheBuilder)
	{
		getCommerceRuleEngineCache().addKIEModuleCache(cacheBuilder);
	}

	@Override
	public void provideCachedEntities(final RuleEvaluationContext context)
	{
		super.provideCachedEntities(context);
		final DroolsRuleEngineContextModel engineContext = (DroolsRuleEngineContextModel) context.getRuleEngineContext();

		final Set<Object> facts = getOrCreateFacts(context);
		final Collection<Object> cachedFacts = getCommerceRuleEngineCache()
				.getCachedFacts(engineContext.getKieSession().getKieBase());
		facts.addAll(cachedFacts);
	}

	protected Set<Object> getOrCreateFacts(final RuleEvaluationContext context)
	{
		if (context.getFacts() == null)
		{
			final Set<Object> facts = new HashSet<>();
			context.setFacts(facts);
		}
		return context.getFacts();
	}

	protected CommerceRuleEngineCache getCommerceRuleEngineCache()
	{
		return commerceRuleEngineCache;
	}

	@Required
	public void setCommerceRuleEngineCache(final CommerceRuleEngineCache factTemplateCache)
	{
		this.commerceRuleEngineCache = factTemplateCache;
	}
}
