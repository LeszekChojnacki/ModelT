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
import static org.apache.commons.collections4.MapUtils.isNotEmpty;

import de.hybris.platform.ruleengine.cache.KIEModuleCacheBuilder;
import de.hybris.platform.ruleengine.cache.RuleGlobalsBeanProvider;
import de.hybris.platform.ruleengine.impl.DefaultPlatformRuleEngineService;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.model.DroolsKIEBaseModel;
import de.hybris.platform.ruleengine.model.DroolsKIEModuleModel;
import de.hybris.platform.ruleengine.model.DroolsRuleModel;
import de.hybris.platform.ruleengine.util.EngineRulePreconditions;
import de.hybris.platform.ruleengine.util.RuleMappings;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * The default implementation for {@link KIEModuleCacheBuilder} caches the globals for each rule that has been
 * added via the {@link #processRule(AbstractRuleEngineRuleModel)} method.
 */
public class DefaultKIEModuleCacheBuilder implements KIEModuleCacheBuilder
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPlatformRuleEngineService.class);

	private final Map<Object, Map<String, Object>> globalsCache = new ConcurrentHashMap<>();

	// a map for checking inconsistencies among the added globals
	private final Map<Object, DroolsRuleModel> duplicateGlobalsCheckMap = new ConcurrentHashMap<>();

	private DroolsKIEModuleModel kieModule;
	private boolean failOnBeanMismatches;

	private Function<DroolsKIEBaseModel, Object> kieBaseCacheKeyGenerator;

	private RuleGlobalsBeanProvider ruleGlobalsBeanProvider;

	/**
	 * @param kieModule the kieModule for this cache builder
	 * @param kieBaseCacheKeyGenerator the cache key generator to be used
	 * @param failOnBeanMismatches throws {@link IllegalArgumentException} during {@link #processRule(AbstractRuleEngineRuleModel)} if the currently
	 * processed rule introduces a global with the same identifier but different type
	 */
	public DefaultKIEModuleCacheBuilder(final RuleGlobalsBeanProvider ruleGlobalsBeanProvider,
			final DroolsKIEModuleModel kieModule,
			final Function<DroolsKIEBaseModel, Object> kieBaseCacheKeyGenerator,
			final boolean failOnBeanMismatches)
	{
		validateParameterNotNull(kieModule, "kieModule must not be null");
		validateParameterNotNull(kieModule.getPk(), "kieModule must be persisted (i.e. have a non-null PK)");
		validateParameterNotNull(kieBaseCacheKeyGenerator, "kieBaseCacheKeyGenerator must not be null");

		this.ruleGlobalsBeanProvider = ruleGlobalsBeanProvider;
		this.kieModule = kieModule;
		this.failOnBeanMismatches = failOnBeanMismatches;
		this.kieBaseCacheKeyGenerator = kieBaseCacheKeyGenerator;
	}

	@Override
	public <T extends AbstractRuleEngineRuleModel> void processRule(final T rule)
	{
		EngineRulePreconditions.checkRuleHasKieModule(rule);
		final DroolsRuleModel droolsRule = (DroolsRuleModel)rule;
		checkArgument(kieModule.getName().equals(RuleMappings.moduleName(droolsRule)),
				"rule must have the same kie module as cache builder");

		final Map<String, Object> kieBaseGlobals = getCachedGlobalsForKieBase(droolsRule.getKieBase());
		if (isNotEmpty(droolsRule.getGlobals()))
		{
			for (final Entry<String, String> entry : droolsRule.getGlobals().entrySet())
			{
				final Object bean = getRuleGlobalsBeanProvider().getRuleGlobals(entry.getValue());
				final Object oldBean = kieBaseGlobals.put(entry.getKey(), bean);
				if (oldBean != null && !bean.equals(oldBean) && !(bean.getClass().isAssignableFrom(oldBean.getClass())))
				{
					// the oldRule has previously added a different bean type under the same global name.
					// This will probably lead to errors
					final DroolsRuleModel oldRule = duplicateGlobalsCheckMap.get(oldBean);
					final String errorMessage = MessageFormat.format(
							"Error when registering global of type {4} for rule {0}. Bean for global {1} was already defined by rule {2} "
									+ "which added bean of type {3}.\n Check your rules! Rule {2} might encounter runtime errors as it expects a global of type {3}",
							rule.getCode(), entry.getKey(),
							oldRule == null ? "" : oldRule.getCode(), oldBean.getClass().getName(), bean.getClass().getName());
					LOGGER.error(errorMessage);
					escalateOnBeanMismatchesIfNecessary(errorMessage);
				}
				duplicateGlobalsCheckMap.put(bean, droolsRule);
			}
		}
	}

	protected void escalateOnBeanMismatchesIfNecessary(final String message)
	{
		if (failOnBeanMismatches)
		{
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * returns the non-null cache segment for the given kie base.
	 */
	protected Map<String, Object> getCachedGlobalsForKieBase(final DroolsKIEBaseModel kieBase)
	{
		return globalsCache.computeIfAbsent(getKieBaseCacheKeyGenerator().apply(kieBase), k -> new ConcurrentHashMap<>());
	}

	public Map<Object, Map<String, Object>> getGlobalsCache()
	{
		return globalsCache;
	}

	public DroolsKIEModuleModel getKieModule()
	{
		return kieModule;
	}

	protected Function<DroolsKIEBaseModel, Object> getKieBaseCacheKeyGenerator()
	{
		return kieBaseCacheKeyGenerator;
	}

	protected RuleGlobalsBeanProvider getRuleGlobalsBeanProvider()
	{
		return ruleGlobalsBeanProvider;
	}
}
