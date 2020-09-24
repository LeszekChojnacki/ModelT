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
package com.hybris.backoffice.config;

import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.event.events.AfterInitializationEndEvent;
import de.hybris.platform.tx.Transaction;
import de.hybris.platform.util.JspContext;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.config.impl.BackofficeCockpitConfigurationService;
import com.hybris.backoffice.events.AfterInitializationEndBackofficeListener;
import com.hybris.cockpitng.core.persistence.WidgetPersistenceService;
import com.hybris.cockpitng.core.util.Validate;
import com.hybris.cockpitng.modules.core.impl.CockpitModuleComponentDefinitionService;
import com.hybris.cockpitng.util.WidgetUtils;


/**
 * Class responsible for handling
 * {@link com.hybris.backoffice.events.ExternalEventCallback}&lt;{@link AfterInitializationEndEvent}&gt; in Backoffice
 * application. It calls {@link #resetBackofficeWidgetsConfiguration(AfterInitializationEndEvent)} if property
 * {@value #RESET_EVERYTHING_ENABLED_PROPERTY} is set to true (default).
 */
public class DefaultBackofficeStartupHandler implements InitializingBean
{

	public static final String RESET_EVERYTHING_ENABLED_PROPERTY = "backoffice.cockpitng.reseteverything.enabled";
	public static final String BACKOFFICE_PROJECT_DATA_CHECKBOX = "backoffice_sample";

	private static final Logger LOG = LoggerFactory.getLogger(DefaultBackofficeStartupHandler.class);

	private AfterInitializationEndBackofficeListener afterInitializationEndBackofficeListener;

	private WidgetPersistenceService widgetPersistenceService;
	private ConfigurationService configurationService;
	private BackofficeCockpitConfigurationService cockpitConfigurationService;
	private WidgetUtils widgetUtils;
	private CockpitModuleComponentDefinitionService cockpitComponentDefinitionService;

	protected void registerAfterInitializationEndCallback(
			final AfterInitializationEndBackofficeListener afterInitializationEndBackofficeListener)
	{
		afterInitializationEndBackofficeListener.registerCallback(this::resetBackofficeWidgetsConfiguration);
	}

	protected void resetBackofficeWidgetsConfiguration(final AfterInitializationEndEvent event)
	{
		if (isBackofficeProjectDataUpdate(event) && isResetEverythingEnabled())
		{
			final Transaction tx = getCurrentTransaction();

			boolean success = false;
			try
			{
				tx.begin();

				getWidgetUtils().refreshWidgetLibrary();
				getCockpitComponentDefinitionService().reloadDefinitions();

				getWidgetPersistenceService().resetToDefaults();
				getCockpitConfigurationService().resetToDefaults();
				success = true;
			}
			finally
			{
				getWidgetUtils().clearWidgetLibrary();

				if (success)
				{
					tx.commit();
				}
				else
				{
					tx.rollback();
				}
			}

		}
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Consuming {}. BackofficeWidgets configuration has been reset to defaults:{}.",
					AfterInitializationEndEvent.class, Boolean.valueOf(isResetEverythingEnabled()));
		}
	}

	protected Transaction getCurrentTransaction()
	{
		return Transaction.current();
	}

	protected boolean isBackofficeProjectDataUpdate(final AfterInitializationEndEvent event)
	{
		Validate.notNull("Init event may not be null", event);
		final JspContext ctx = event.getCtx();
		if (ctx != null)
		{
			final HttpServletRequest request = ctx.getServletRequest();
			if (request != null)
			{
				return BooleanUtils.toBoolean(request.getParameter(BACKOFFICE_PROJECT_DATA_CHECKBOX));
			}
		}
		LOG.warn("Could not read request parameters. Using fallback.");
		return false;
	}

	protected boolean isResetEverythingEnabled()
	{
		boolean resetEverythingEnabled = true;
		final Configuration configuration = getConfigurationService().getConfiguration();

		if (configuration.containsKey(RESET_EVERYTHING_ENABLED_PROPERTY))
		{
			resetEverythingEnabled = configuration.getBoolean(RESET_EVERYTHING_ENABLED_PROPERTY);
		}

		return resetEverythingEnabled;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		registerAfterInitializationEndCallback(getAfterInitializationEndBackofficeListener());
	}

	public AfterInitializationEndBackofficeListener getAfterInitializationEndBackofficeListener()
	{
		return afterInitializationEndBackofficeListener;
	}

	@Required
	public void setAfterInitializationEndBackofficeListener(
			final AfterInitializationEndBackofficeListener afterInitializationEndBackofficeListener)
	{
		this.afterInitializationEndBackofficeListener = afterInitializationEndBackofficeListener;
	}

	public WidgetPersistenceService getWidgetPersistenceService()
	{
		return widgetPersistenceService;
	}

	@Required
	public void setWidgetPersistenceService(final WidgetPersistenceService widgetPersistenceService)
	{
		this.widgetPersistenceService = widgetPersistenceService;
	}

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	public BackofficeCockpitConfigurationService getCockpitConfigurationService()
	{
		return cockpitConfigurationService;
	}

	@Required
	public void setCockpitConfigurationService(final BackofficeCockpitConfigurationService cockpitConfigurationService)
	{
		this.cockpitConfigurationService = cockpitConfigurationService;
	}

	public WidgetUtils getWidgetUtils()
	{
		return widgetUtils;
	}

	@Required
	public void setWidgetUtils(final WidgetUtils widgetUtils)
	{
		this.widgetUtils = widgetUtils;
	}

	public CockpitModuleComponentDefinitionService getCockpitComponentDefinitionService()
	{
		return cockpitComponentDefinitionService;
	}

	@Required
	public void setCockpitComponentDefinitionService(
			final CockpitModuleComponentDefinitionService cockpitComponentDefinitionService)
	{
		this.cockpitComponentDefinitionService = cockpitComponentDefinitionService;
	}
}
