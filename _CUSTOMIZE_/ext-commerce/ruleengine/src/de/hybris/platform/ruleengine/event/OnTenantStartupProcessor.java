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
package de.hybris.platform.ruleengine.event;

import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.RULE_ENGINE_ACTIVE;
import static de.hybris.platform.ruleengine.constants.RuleEngineConstants.RULE_ENGINE_INIT_MODE;

import de.hybris.platform.core.Registry;
import de.hybris.platform.core.Tenant;
import de.hybris.platform.core.TenantListener;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.ruleengine.MessageLevel;
import de.hybris.platform.ruleengine.RuleEngineActionResult;
import de.hybris.platform.ruleengine.RuleEngineService;
import de.hybris.platform.ruleengine.constants.RuleEngineConstants.RuleEngineInitMode;
import de.hybris.platform.ruleengine.dao.RulesModuleDao;
import de.hybris.platform.ruleengine.init.InitializationFuture;
import de.hybris.platform.ruleengine.init.RuleEngineBootstrap;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.util.RedeployUtilities;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;

import com.google.common.collect.Lists;


/**
 * The OnTenantStartupProcessor is responsible for handling tenant activation of rules modules after the current tenant
 * has been (re)started. It is possible to disable this mechanism for specific tenants by providing a set of tenant ids
 * to be excluded (see {@link #getExcludedTenants()}.
 */
public class OnTenantStartupProcessor
{

	private static final Logger LOG = LoggerFactory.getLogger(OnTenantStartupProcessor.class);

	private RulesModuleDao rulesModuleDao;
	private RuleEngineBootstrap ruleEngineBootstrap;
	private RuleEngineService ruleEngineService;
	private ConfigurationService configurationService;

	private Set<String> excludedTenants = Collections.emptySet();
	private Tenant currentTenant;
	private RetryTemplate ruleEngineInitRetryTemplate;


	/**
	 * The method is called on Tenant startup.
	 */
	protected void processOnTenantStartup()
	{
		if (ignoreTenant())
		{
			return;
		}
		if (isSystemInitialized())
		{
			activateRulesModules();
		}
		else
		{
			LOG.info("Not initializing rule engine as system is not initialized or currently initializing");
		}
	}

	protected boolean activateRulesModules()
	{
		if (getConfigurationService().getConfiguration().getBoolean(RULE_ENGINE_ACTIVE, true))
		{
			refreshCurrentSessionWithRetry(() -> {
				JaloSession.getCurrentSession();
				return null;
			});
			final List<RuleEngineActionResult> bootstrapResults = Lists.newArrayList();
			try
			{
				final List<AbstractRulesModuleModel> modules = getRulesModuleDao().findAll();
				final List<String> moduleNames = modules.stream().map(AbstractRulesModuleModel::getName)
						.collect(Collectors.toList());
				LOG.info("[{}]: Initializing rule engine modules after tenant [{}] startup. Modules to be initialized: {}",
						Thread.currentThread().getName(),
						(currentTenant == null ? "null" : currentTenant.getTenantID()), //NOSONAR
						moduleNames.stream().collect(Collectors.joining(", ")));

				final RuleEngineInitMode INITMODE = RuleEngineInitMode                                       // NOSONAR
						.valueOf(getConfigurationService().getConfiguration().getString(RULE_ENGINE_INIT_MODE,
								RuleEngineInitMode.SYNC.toString()));
				if (RuleEngineInitMode.SYNC.equals(INITMODE))
				{
					moduleNames.forEach(moduleName -> bootstrapResults.add(getRuleEngineBootstrap().startup(moduleName)));
				}
				else if (RuleEngineInitMode.ASYNC.equals(INITMODE))
				{
					final InitializationFuture initializationFuture = getRuleEngineService().initialize(modules, false, false)
							.waitForInitializationToFinish();
					bootstrapResults.addAll(initializationFuture.getResults());
				}
				else
				{
					LOG.error("Unsupported Rule Engine initialisation mode [{}], skipping initialization.", INITMODE);
				}
			}
			catch (final Exception ex)
			{
				LOG.error("[{}]: Exception caught in rule module initialization during tenant startup or after initialization: {}",
						Thread.currentThread().getName(), ex);
			}
			finally
			{
				for (final RuleEngineActionResult bootstrapResult : bootstrapResults)
				{
					if (bootstrapResult.isActionFailed())
					{
						LOG.error(
								"[{}]: Rules module initialization for [{}] failed during tenant startup or after initialization. Error details: {}",
								Thread.currentThread().getName(), bootstrapResult.getModuleName(),
								bootstrapResult.getMessagesAsString(MessageLevel.ERROR));
					}
				}
			}
		}
		return true;
	}

	protected void refreshCurrentSessionWithRetry(final Supplier<Void> failingMethodSupplier)
	{
		final RetryTemplate retryTemplate = getRuleEngineInitRetryTemplate();
		retryTemplate.execute((RetryCallback<Void, IllegalStateException>) retryContext -> failingMethodSupplier.get());
	}

	@PostConstruct
	protected void init()
	{
		this.currentTenant = Registry.getCurrentTenantNoFallback();
		Registry.registerTenantListener(new DefaultRuleEngineTenantListener());
	}

	protected boolean isSystemInitialized()
	{
		if (currentTenant == null)
		{
			return false;
		}

		if (!currentTenant.getJaloConnection().isSystemInitialized())
		{
			return false;
		}

		final boolean result = !RedeployUtilities.isShutdownInProgress();
		if (result)
		{
			LOG.info("System is initialised, tenantId=[{}] ", currentTenant.getTenantID());
		}
		return result;
	}

	/**
	 * checks if the current tenantID is part of the ignored tenants as configured via {@link #getExcludedTenants()}.
	 *
	 * @return true if the tenant should be ignored, otherwise false
	 */
	protected boolean ignoreTenant()
	{
		final Tenant tenant = Registry.getCurrentTenant();
		// ignore excluded tenants
		if (getExcludedTenants() != null && getExcludedTenants().contains(tenant.getTenantID()))
		{
			LOG.info("ignoring rule module activation on tenant:" + tenant.getTenantID()
					+ " as it is part of the excludedTenants set.");
			return true;
		}
		return false;
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

	/**
	 * returns a set of tenant IDs for which rules modules will not be activated.
	 */
	protected Set<String> getExcludedTenants()
	{
		return excludedTenants;
	}

	public void setExcludedTenants(final Set<String> excludedTenants)
	{
		this.excludedTenants = excludedTenants;
	}

	protected RulesModuleDao getRulesModuleDao()
	{
		return rulesModuleDao;
	}

	@Required
	public void setRulesModuleDao(final RulesModuleDao rulesModuleDao)
	{
		this.rulesModuleDao = rulesModuleDao;
	}

	protected RuleEngineBootstrap getRuleEngineBootstrap()
	{
		return ruleEngineBootstrap;
	}

	@Required
	public void setRuleEngineBootstrap(final RuleEngineBootstrap ruleEngineBootstrap)
	{
		this.ruleEngineBootstrap = ruleEngineBootstrap;
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

	protected RetryTemplate getRuleEngineInitRetryTemplate()
	{
		return ruleEngineInitRetryTemplate;
	}

	@Required
	public void setRuleEngineInitRetryTemplate(final RetryTemplate ruleEngineInitRetryTemplate)
	{
		this.ruleEngineInitRetryTemplate = ruleEngineInitRetryTemplate;
	}

	private final class DefaultRuleEngineTenantListener implements TenantListener
	{

		@Override
		public void afterTenantStartUp(final Tenant paramTenant)
		{
			if (currentTenant.equals(paramTenant))
			{
				processOnTenantStartup();
			}
		}

		@Override
		public void beforeUnsetActivateSession(final Tenant paramTenant)
		{
			// do nothing
		}

		@Override
		public void beforeTenantShutDown(final Tenant paramTenant)
		{
			// do nothing
		}

		@Override
		public void afterSetActivateSession(final Tenant paramTenant)
		{
			// do nothing
		}
	}

}
