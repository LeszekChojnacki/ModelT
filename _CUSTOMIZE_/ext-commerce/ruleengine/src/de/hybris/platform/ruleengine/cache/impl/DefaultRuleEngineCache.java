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
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import de.hybris.platform.ruleengine.cache.KIEModuleCacheBuilder;
import de.hybris.platform.ruleengine.cache.RuleEngineCache;
import de.hybris.platform.ruleengine.cache.RuleGlobalsBeanProvider;
import de.hybris.platform.ruleengine.model.DroolsKIEBaseModel;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.ImmutableMap;


/**
 * Default implementation for the rule engine cache. The default implementation caches the globals of rule modules.
 */
public class DefaultRuleEngineCache implements RuleEngineCache
{
	/**
	 * the cache structure: key(KIEModule) -> key(KIEBase) -> Map (global name to global)
	 */
	private final ConcurrentHashMap<Object, Map<Object, Map<String, Object>>> globalsCache = new ConcurrentHashMap<>();


	private ConfigurationService configurationService;
	private RuleGlobalsBeanProvider ruleGlobalsBeanProvider;

	/**
	 * the default cache uses the string representation of the rule module's PK as cache key
	 */
	protected Function<DroolsKIEModuleModel, Object> kieModuleCacheKeyGenerator = km ->
	{
		validateParameterNotNull(km, "kie module must not be null");
		validateParameterNotNull(km.getPk(), "kie module must have a pk");
		return km.getPk().getLongValueAsString();
	};

	/**
	 * the default cache uses the string representation of the kie base's PK as cache key
	 */
	protected Function<DroolsKIEBaseModel, Object> kieBaseCacheKeyGenerator = kb ->
	{
		validateParameterNotNull(kb, "kie base must not be null");
		validateParameterNotNull(kb.getPk(), "kie base must have a pk");
		return kb.getPk().getLongValueAsString();
	};

	@Override
	public KIEModuleCacheBuilder createKIEModuleCacheBuilder(final DroolsKIEModuleModel kieModule)
	{
		return new DefaultKIEModuleCacheBuilder(getRuleGlobalsBeanProvider(), kieModule, getKieBaseCacheKeyGenerator(),
				getConfigurationService()
						.getConfiguration().getBoolean("defaultRuleEngineCacheService.globals.fail.on.bean.mismatch", false));
	}

	@Override
	public void addKIEModuleCache(final KIEModuleCacheBuilder cacheBuilder)
	{
		checkArgument(cacheBuilder instanceof DefaultKIEModuleCacheBuilder,
				"cache must be of type DefaultRuleEngineKIEModuleCacheBuilder");
		final DefaultKIEModuleCacheBuilder moduleCache = (DefaultKIEModuleCacheBuilder) cacheBuilder;

		final Map<Object, Map<String, Object>> globals = moduleCache.getGlobalsCache();
		globalsCache.put(getKieModuleCacheKeyGenerator().apply(moduleCache.getKieModule()), ImmutableMap.copyOf(globals));
	}


	@Override
	public Map<String, Object> getGlobalsForKIEBase(final DroolsKIEBaseModel kieBase)
	{
		final Object key = getKieBaseCacheKeyGenerator().apply(kieBase);
		return getGlobalsCacheForKIEModule(kieBase.getKieModule()).orElseGet(() -> emptyMap()).getOrDefault(key,
				emptyMap());
	}

	/**
	 * returns the optional of the cached globals for the given kie module
	 */
	protected Optional<Map<Object, Map<String, Object>>> getGlobalsCacheForKIEModule(final DroolsKIEModuleModel kieModule)
	{
		final Object key = getKieModuleCacheKeyGenerator().apply(kieModule);
		return ofNullable(globalsCache.get(key));
	}

	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	protected Function<DroolsKIEModuleModel, Object> getKieModuleCacheKeyGenerator()
	{
		return kieModuleCacheKeyGenerator;
	}

	public void setKieModuleCacheKeyGenerator(final Function<DroolsKIEModuleModel, Object> kieModuleCacheKeyGenerator)
	{
		this.kieModuleCacheKeyGenerator = kieModuleCacheKeyGenerator;
	}

	protected Function<DroolsKIEBaseModel, Object> getKieBaseCacheKeyGenerator()
	{
		return kieBaseCacheKeyGenerator;
	}

	public void setKieBaseCacheKeyGenerator(final Function<DroolsKIEBaseModel, Object> kieBaseCacheKeyGenerator)
	{
		this.kieBaseCacheKeyGenerator = kieBaseCacheKeyGenerator;
	}

	protected RuleGlobalsBeanProvider getRuleGlobalsBeanProvider()
	{
		return ruleGlobalsBeanProvider;
	}

	@Required
	public void setRuleGlobalsBeanProvider(final RuleGlobalsBeanProvider ruleGlobalsBeanProvider)
	{
		this.ruleGlobalsBeanProvider = ruleGlobalsBeanProvider;
	}
}
