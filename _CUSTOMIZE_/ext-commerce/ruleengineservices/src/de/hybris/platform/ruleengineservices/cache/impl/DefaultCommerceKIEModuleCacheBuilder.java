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

import de.hybris.platform.regioncache.ConcurrentHashSet;
import de.hybris.platform.ruleengine.cache.RuleGlobalsBeanProvider;
import de.hybris.platform.ruleengine.cache.impl.DefaultKIEModuleCacheBuilder;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.model.DroolsKIEBaseModel;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.ruleengineservices.rao.providers.RAOProvider;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;


/**
 * The DefaultCommerceKIEModuleCacheBuilder adds caching of fact templates.
 */
public class DefaultCommerceKIEModuleCacheBuilder extends DefaultKIEModuleCacheBuilder
{
	/**
	 * the cache structure: for each kie base (Object) a collection of facts are stored
	 */
	private final Map<Object, Collection<Object>> factTemplateCache = new ConcurrentHashMap<>();

	private List<RAOProvider> raoCacheCreators;

	public DefaultCommerceKIEModuleCacheBuilder(final RuleGlobalsBeanProvider ruleGlobalsBeanProvider, final DroolsKIEModuleModel kieModule,
			final List<RAOProvider> raoCacheCreators, final Function<DroolsKIEBaseModel, Object> kieBaseCacheKeyGenerator, final boolean failOnBeanMismatch)
	{
		super(ruleGlobalsBeanProvider, kieModule, kieBaseCacheKeyGenerator, failOnBeanMismatch);
		this.raoCacheCreators = raoCacheCreators;
	}

	@Override
	public <T extends AbstractRuleEngineRuleModel> void processRule(final T rule)
	{
		// validate and add globals first
		super.processRule(rule);
		final DroolsRuleModel droolsRule = (DroolsRuleModel)rule;

		// create facts for the given rule using the registered rao cache creators (RAOProviders)
		final Collection<Object> cacheSegment = getFactTemplateCacheSegmentForKieBase(droolsRule.getKieBase());
		getRaoCacheCreators().forEach(creator -> cacheSegment.addAll(creator.expandFactModel(rule)));
	}

	/**
	 * returns the non-null cache segment for the given kie base.
	 */
	protected Collection<Object> getFactTemplateCacheSegmentForKieBase(final DroolsKIEBaseModel kieBase)
	{
		return factTemplateCache.computeIfAbsent(getKieBaseCacheKeyGenerator().apply(kieBase), k -> new ConcurrentHashSet<>());
	}

	protected List<RAOProvider> getRaoCacheCreators()
	{
		return raoCacheCreators;
	}

	public Map<Object, Collection<Object>> getFactTemplateCache()
	{
		return factTemplateCache;
	}

}
