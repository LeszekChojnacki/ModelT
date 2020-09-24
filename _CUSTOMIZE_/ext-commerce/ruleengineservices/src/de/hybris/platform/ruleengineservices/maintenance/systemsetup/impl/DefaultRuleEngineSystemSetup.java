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
package de.hybris.platform.ruleengineservices.maintenance.systemsetup.impl;

import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.regioncache.ConcurrentHashSet;
import de.hybris.platform.ruleengine.RuleEngineService;
import de.hybris.platform.ruleengine.jalo.AbstractRulesModule;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;
import de.hybris.platform.ruleengineservices.jalo.SourceRule;
import de.hybris.platform.ruleengineservices.maintenance.RuleMaintenanceService;
import de.hybris.platform.ruleengineservices.maintenance.systemsetup.RuleEngineSystemSetup;
import de.hybris.platform.ruleengineservices.model.SourceRuleModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.event.events.AfterInitializationEndEvent;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;



/**
 * The default implementation for RuleEngineSystemSetupService. This setup class allows to register source rules for
 * deployment after system initialization.
 */
public class DefaultRuleEngineSystemSetup extends AbstractEventListener<AfterInitializationEndEvent>
		implements RuleEngineSystemSetup
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRuleEngineSystemSetup.class);
	private final Map<String, Set<PK>> initializationMap = new ConcurrentHashMap<>();
	private RuleEngineService ruleEngineService;
	private RuleMaintenanceService ruleMaintenanceService;
	private ModelService modelService;
	private ConfigurationService configurationService;
	private Predicate<SourceRuleModel> sourceRuleModelValidator = sr -> {
		try
		{
			requireNonNull(sr);
			requireNonNull(sr.getPk());
		}
		catch (final Exception e)
		{
			if (failOnError())
			{
				LOGGER.error("SourceRuleModel is null or has no pk.", e);
				throw e;
			}
			LOGGER.info("SourceRuleModel is null or has no pk.", e);
			return false;
		}
		return true;
	};

	@Override
	public <T extends AbstractRulesModule> void initializeModule(final T module)
	{
		checkArgument(nonNull(module), "The provided module cannot be NULL");

		validateAndConvertFromJalo(module).ifPresent(m -> getRuleEngineService().initialize(newArrayList(m), true, true));
	}

	@Override
	public void registerSourceRuleForDeployment(final SourceRule sourceRule, final String... moduleNames)
	{
		validateParameterNotNull(sourceRule, "sourceRules must not be null!");
		validateParameterNotNull(moduleNames, "moduleNames must not be null!");
		validateAndConvertFromJalo(sourceRule)
				.ifPresent(sr -> registerSourceRulesForDeployment(singleton(sr), asList(moduleNames)));
	}

	@Override
	public void registerSourceRulesForDeployment(final Collection<SourceRuleModel> sourceRules,
			final Collection<String> moduleNames)
	{
		validateParameterNotNull(sourceRules, "sourceRules must not be null!");
		validateParameterNotNull(moduleNames, "moduleNames must not be null!");
		checkArgument(!moduleNames.contains(null), "moduleNames must not contain null values");
		moduleNames.forEach(name -> getInitializationMap().computeIfAbsent(name, s -> new ConcurrentHashSet<>()).addAll(
				sourceRules.stream().filter(getSourceRuleModelValidator()).map(SourceRuleModel::getPk).collect(Collectors.toSet())));
	}

	@Override
	protected void onEvent(final AfterInitializationEndEvent event)
	{
		LOGGER.info("Starting rule compilation and deployment after system initialization end event: {}",
				getInitializationEventType(event));
		final Map<String, List<SourceRuleModel>> moduleNameToRules = getInitializationMap().entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, e -> convert(e.getValue())));
		getInitializationMap().clear();
		moduleNameToRules.forEach(this::doRuleDeployment);
		LOGGER.info("Rule compilation and deployment after system initialization finished.");
	}

	protected void doRuleDeployment(final String moduleName, final List<SourceRuleModel> rules)
	{
		try
		{
			LOGGER.info("starting compilation and publication for module {}", moduleName);
			getRuleMaintenanceService().compileAndPublishRulesWithBlocking(rules, moduleName, false);
		}
		catch (final Exception e)
		{
			LOGGER.error("error during rule deployment! Module " + moduleName + " is not initialized properly. ", e);
		}
	}

	protected String getInitializationEventType(final AfterInitializationEndEvent event)
	{
		if (MapUtils.isNotEmpty(event.getParams()) && event.getParams().containsKey("initmethod"))
		{
			return event.getParams().get("initmethod");
		}
		return StringUtils.EMPTY;
	}

	protected <T extends SourceRuleModel> Optional<T> validateAndConvertFromJalo(final SourceRule sourceRule)
	{
		validateParameterNotNull(sourceRule, "sourceRule must not be null!");
		return validateAndConvertFromJalo(sourceRule.getPK());
	}

	protected <T extends AbstractRulesModuleModel> Optional<T> validateAndConvertFromJalo(final AbstractRulesModule rulesModule)
	{
		validateParameterNotNull(rulesModule, "rulesModule must not be null!");
		return validateAndConvertFromJalo(rulesModule.getPK());
	}

	protected <T extends ItemModel> Optional<T> validateAndConvertFromJalo(final PK pk)
	{
		try
		{
			return of(getModelService().get(pk));
		}
		catch (final Exception e)
		{
			LOGGER.warn("Couldn't get model for given pk: [{}]", pk);
			if (failOnError())
			{
				throw e;
			}
			return empty();
		}
	}

	/**
	 * whether to fail on any error or ignore them (set via <code>ruleengineservices.system.setup.failOnError</code>
	 * system property, defaults to true.
	 *
	 * @return whether to fail on any error or not
	 */
	protected boolean failOnError()
	{
		return getConfigurationService().getConfiguration().getBoolean("ruleengineservices.system.setup.failOnError", true);
	}

	/**
	 * converts the given set of PKs to source rule model objects.
	 *
	 * @param pks
	 *           the set of pks
	 * @return the list of source rule models
	 */
	protected <T extends ItemModel> List<T> convert(final Set<PK> pks)
	{
		return pks.stream().map(this::<T> validateAndConvertFromJalo).filter(Optional::isPresent).map(Optional::get)
				.collect(Collectors.toList());
	}

	protected Map<String, Set<PK>> getInitializationMap()
	{
		return initializationMap;
	}

	protected RuleMaintenanceService getRuleMaintenanceService()
	{
		return ruleMaintenanceService;
	}

	@Required
	public void setRuleMaintenanceService(final RuleMaintenanceService ruleMaintenanceService)
	{
		this.ruleMaintenanceService = ruleMaintenanceService;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
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

	protected Predicate<SourceRuleModel> getSourceRuleModelValidator()
	{
		return sourceRuleModelValidator;
	}

	public void setSourceRuleModelValidator(final Predicate<SourceRuleModel> sourceRuleModelValidator)
	{
		this.sourceRuleModelValidator = sourceRuleModelValidator;
	}

	protected RuleEngineService getRuleEngineService()
	{
		return ruleEngineService;
	}

	@Required
	public void setRuleEngineService(final RuleEngineService ruleEngineService)
	{
		this.ruleEngineService = ruleEngineService;
	}
}
