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

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections4.MapUtils.isNotEmpty;

import de.hybris.platform.ruleengine.cache.KIEModuleCacheBuilder;
import de.hybris.platform.ruleengine.cache.impl.DefaultRuleEngineCache;
import de.hybris.platform.ruleengine.model.DroolsKIEBaseModel;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengineservices.cache.CommerceRuleEngineCache;
import de.hybris.platform.ruleengineservices.rao.providers.RAOProvider;
import de.hybris.platform.ruleengineservices.rao.providers.impl.DefaultRuleConfigurationRRDProvider;
import de.hybris.platform.ruleengineservices.rao.providers.impl.DefaultRuleGroupExecutionRRDProvider;
import de.hybris.platform.ruleengineservices.rrd.RuleConfigurationRRD;
import de.hybris.platform.ruleengineservices.rrd.RuleGroupExecutionRRD;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.ImmutableList;


/**
 * DefaultCommerceRuleEngineCache is the default implementation for caching fact templates. The
 * {@link #getRaoCacheCreators()} are used by this cache's cache builders
 * ({@link #createKIEModuleCacheBuilder(DroolsKIEModuleModel)}) to create "facts templates" during the initialization of
 * a module. The {@link #getRaoProviders()} are used to generate copies of the cached "fact templates" (see
 * {@link #getCachedFacts(DroolsKIEBaseModel)}).
 *
 * Note: During the {@link #addKIEModuleCache(KIEModuleCacheBuilder)} a check (see {@link #checkFactTemplates(Map)}) is
 * done to ensure that for each created fact template there is a registered {@link RAOProvider} (matched by its class)
 * that can create a copy of the fact template. This check can be disabled (via setting the
 * {@code defaultCommerceRuleEngineCache.checkRAOProvidersForCache} property to false). If the check is disabled and a
 * fact template has no corresponding RAOProvider to create a copy, it will simply be inserted directly as a fact into
 * the rule evaluation context. This can be useful if a fact template doesn't require to have a copy created for each
 * rule evaluation (e.g. if the fact template is not modified by any rules during rule evaluation).
 *
 * Out of the box two fact template types are implemented: {@link RuleConfigurationRRD} objects which represent a rule
 * and track/control its execution and {@link RuleGroupExecutionRRD} objects which represent rule groups and
 * track/control their execution.
 */
public class DefaultCommerceRuleEngineCache extends DefaultRuleEngineCache implements CommerceRuleEngineCache, InitializingBean
{
	/**
	 * the cache structure: key(KIEModule) -> key(KIEBase) -> Collection(fact templates)
	 */
	private final ConcurrentHashMap<Object, Map<Object, Collection<Object>>> factTemplateCache = new ConcurrentHashMap<>();

	/**
	 * the identity rao provider returns a singleton set of the given fact template. This is used as a default rao
	 * provider if none is registered for the class of the given fact template.
	 */
	protected static final RAOProvider<Object> identityRAOProvider = Collections::singleton;

	/**
	 * the rao providers are used to create "copies" of the cached fact templates during rule evaluation
	 */
	private Map<Class, RAOProvider> raoProviders;

	/**
	 * the cache creators are used by the cache builders to create fact templates during rule module initialization
	 */
	private List<RAOProvider> raoCacheCreators;

	private boolean useDeprecatedRRDsInCache = false;

	@Override
	public KIEModuleCacheBuilder createKIEModuleCacheBuilder(final DroolsKIEModuleModel kieModule)
	{
		return new DefaultCommerceKIEModuleCacheBuilder(getRuleGlobalsBeanProvider(), kieModule,
				ImmutableList.copyOf(getRaoCacheCreators()), getKieBaseCacheKeyGenerator(), false);
	}

	@Override
	public void addKIEModuleCache(final KIEModuleCacheBuilder cacheBuilder)
	{
		checkArgument(cacheBuilder instanceof DefaultCommerceKIEModuleCacheBuilder,
				"cache must be of type DefaultCommerceRuleEngineKIEModuleCache");

		super.addKIEModuleCache(cacheBuilder);

		final DefaultCommerceKIEModuleCacheBuilder cacheBuilderImpl = (DefaultCommerceKIEModuleCacheBuilder) cacheBuilder;
		final Map<Object, Collection<Object>> factTemplates = cacheBuilderImpl.getFactTemplateCache();

		checkFactTemplates(factTemplates);
		factTemplateCache.put(getKieModuleCacheKeyGenerator().apply(cacheBuilderImpl.getKieModule()), factTemplates);
	}

	@Override
	public Collection<Object> getCachedFacts(final DroolsKIEBaseModel kieBase)
	{
		final Collection<Object> factTemplates = getFactTemplateCacheForKieBase(kieBase);
		final Collection<Object> facts = new HashSet<>();
		factTemplates.forEach(ft -> facts.addAll(getRaoProvider(ft).orElse(identityRAOProvider).expandFactModel(ft)));
		return facts;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		// only if deprecated RRDs are supposed to be used, we leave the specific RRD cache creators and RAO providers in place
		if (!isUseDeprecatedRRDsInCache())
		{
			// remove RAO providers for RRDs
			setRaoCacheCreators(getRaoCacheCreators().stream().filter(
					ro -> !(ro instanceof DefaultRuleConfigurationRRDProvider || ro instanceof DefaultRuleGroupExecutionRRDProvider))
					.collect(Collectors.toList()));
			getRaoProviders().remove(RuleConfigurationRRD.class);
			getRaoProviders().remove(RuleGroupExecutionRRD.class);
		}
	}

	/**
	 * checks that for each given fact template class there is an RAOProvider registered.
	 *
	 * @param factTemplates
	 *           the collection of fact templates to check
	 */
	protected void checkFactTemplates(final Map<Object, Collection<Object>> factTemplates)
	{
		// by default check if for each given fact template type there is a rao provider registered
		final boolean doSanityCheck = getConfigurationService().getConfiguration()
				.getBoolean("defaultCommerceRuleEngineCache.checkRAOProvidersForCache", true);

		if (doSanityCheck && isNotEmpty(factTemplates))
		{
			factTemplates.entrySet().stream().map(Map.Entry::getValue).flatMap(Collection::stream)
					.forEach(ft -> getRaoProvider(ft).orElseThrow(() -> new IllegalArgumentException(
							"Cannot create cache. No RAOProvider registered in DefaultCommerceRuleEngineCacheService for facts of class: "
									+ ft.getClass().getName()
									+ ". Please register an RAOProvider for this class. Otherise this fact template cannot be cloned into a fact at rule evaluation time. "
									+ "You can disable this check by setting the system variable 'defaultCommerceRuleEngineCache.checkRAOProvidersForCache' to false. "
									+ "If you do that, the fact templates will be inserted directly without creating a copy of them (make sure these facts are not modified by the rule evaluation though!)")));
		}
	}

	/**
	 * returns the fact templates for the given kie base.
	 */
	protected Collection<Object> getFactTemplateCacheForKieBase(final DroolsKIEBaseModel kieBase)
	{
		final Object key = getKieBaseCacheKeyGenerator().apply(kieBase);
		return getFactTemplateCacheForKIEModule(kieBase.getKieModule()).orElseGet(() -> emptyMap()).getOrDefault(key, emptyList());
	}

	/**
	 * returns the optional fact templates for the given kie module
	 */
	protected Optional<Map<Object, Collection<Object>>> getFactTemplateCacheForKIEModule(final DroolsKIEModuleModel kieModule)
	{
		final Object key = getKieModuleCacheKeyGenerator().apply(kieModule);
		return ofNullable(factTemplateCache.get(key));
	}

	/**
	 * returns the optional rao provider based on the given fact template's class
	 */
	protected Optional<RAOProvider> getRaoProvider(final Object factTemplate)
	{
		return ofNullable(getRaoProviders().get(factTemplate.getClass()));
	}

	protected Map<Class, RAOProvider> getRaoProviders()
	{
		return raoProviders;
	}

	@Required
	public void setRaoProviders(final Map<Class, RAOProvider> raoProviders)
	{
		this.raoProviders = raoProviders;
	}

	protected List<RAOProvider> getRaoCacheCreators()
	{
		return raoCacheCreators;
	}

	@Required
	public void setRaoCacheCreators(final List<RAOProvider> raoCacheCreators)
	{
		this.raoCacheCreators = raoCacheCreators;
	}

	/**
	 * @deprecated since 18.11 flag is present only to enable deprecated RRD usage (backwards compatibility)
	 */
	@Deprecated
	public void setUseDeprecatedRRDsInCache(final boolean useDeprecatedRRDsInCache)
	{
		this.useDeprecatedRRDsInCache = useDeprecatedRRDsInCache;
	}

	/**
	 * @deprecated since 18.11 flag is present only to enable deprecated RRD usage (backwards compatibility)
	 */
	@Deprecated
	protected boolean isUseDeprecatedRRDsInCache()
	{
		return useDeprecatedRRDsInCache;
	}

}
