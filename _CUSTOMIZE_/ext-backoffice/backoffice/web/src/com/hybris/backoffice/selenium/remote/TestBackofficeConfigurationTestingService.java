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
package com.hybris.backoffice.selenium.remote;

import de.hybris.platform.servicelayer.impex.ImportResult;
import de.hybris.platform.servicelayer.impex.ImportService;
import de.hybris.platform.servicelayer.impex.impl.StreamBasedImpExResource;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.CSVConstants;
import de.hybris.platform.validation.services.ValidationService;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.config.impl.TestingBackofficeCockpitConfigurationService;
import com.hybris.backoffice.config.impl.TestingBackofficeWidgetPersistenceService;
import com.hybris.cockpitng.admin.CockpitAdminService;
import com.hybris.cockpitng.core.config.CockpitConfigurationException;
import com.hybris.cockpitng.util.WidgetUtils;


public class TestBackofficeConfigurationTestingService implements BackofficeConfigurationTestingService
{

	private static final Logger LOG = LogManager.getLogger(TestBackofficeConfigurationTestingService.class);

	protected TestingBackofficeWidgetPersistenceService widgetPersistenceService;
	protected TestingBackofficeCockpitConfigurationService cockpitConfigurationService;
	protected WidgetUtils widgetUtils;
	protected SessionService sessionService;
	protected UserService userService;
	protected CockpitAdminService cockpitAdminService;
	protected ImportService importService;
	protected ValidationService validationService;

	@Override
	public void resetCockpitConfig()
	{

		sessionService.executeInLocalView(new SessionExecutionBody()
		{
			@Override
			public Object execute()
			{
				cockpitConfigurationService.resetToDefaults();
				return super.execute();
			}
		}, userService.getAdminUser());
	}

	@Override
	public void resetConfigurationCache()
	{
		sessionService.executeInLocalView(new SessionExecutionBody()
		{
			@Override
			public Object execute()
			{
				cockpitConfigurationService.clearCustomConfiguration();
				return super.execute();
			}
		}, userService.getAdminUser());
	}

	@Override
	public void applyTestConfigurationToConfigurationCache(final String configXML, final String moduleName) throws IOException
	{
		sessionService.executeInLocalView(new SessionExecutionBody()
		{
			@Override
			public Object execute()
			{
				try
				{
					cockpitConfigurationService.setCustomConfiguration(configXML, moduleName);
				}
				catch (final CockpitConfigurationException e)
				{
					LOG.error(e.getLocalizedMessage(), e);
				}
				return super.execute();
			}
		}, userService.getAdminUser());
	}

	@Override
	public void applyTestWidgetConfig(final String configXML) throws IOException
	{
		sessionService.executeInLocalView(new SessionExecutionBody()
		{
			@Override
			public Object execute()
			{
				widgetPersistenceService.setAdditionalWidgetConfig(configXML);
				return super.execute();
			}
		}, userService.getAdminUser());
	}

	@Override
	public void resetWidgetConfig()
	{
		sessionService.executeInLocalView(new SessionExecutionBody()
		{
			@Override
			public Object execute()
			{
				widgetPersistenceService.clearAdditionalWidgetConfig();
				return super.execute();
			}
		}, userService.getAdminUser());
	}


	@Override
	public void importImpex(final String content)
	{
		sessionService.executeInLocalView(new SessionExecutionBody()
		{
			@Override
			public Object execute()
			{
				final StreamBasedImpExResource importFile = new StreamBasedImpExResource(IOUtils.toInputStream(content),
						CSVConstants.HYBRIS_ENCODING);
				final ImportResult importResult = importService.importData(importFile);
				if (importResult.hasUnresolvedLines())
				{
					LOG.warn(importResult.getUnresolvedLines().getPreview());
				}

				if (importResult.getCronJob() != null)
				{
					switch (importResult.getCronJob().getResult())
					{
						case FAILURE:
						case ERROR:
						case UNKNOWN:
						{
							LOG.error("Failed to import impex. For more details see impExImportCronJob object with code "
									+ importResult.getCronJob().getCode());
						}
					}
				}
				return super.execute();
			}
		}, userService.getAdminUser());
	}

	@Override
	public void reloadValidationEngine()
	{
		validationService.reloadValidationEngine();
	}

	@Required
	public void setWidgetPersistenceService(final TestingBackofficeWidgetPersistenceService widgetPersistenceService)
	{
		this.widgetPersistenceService = widgetPersistenceService;
	}

	@Required
	public void setCockpitConfigurationService(final TestingBackofficeCockpitConfigurationService cockpitConfigurationService)
	{
		this.cockpitConfigurationService = cockpitConfigurationService;
	}

	@Required
	public void setWidgetUtils(final WidgetUtils widgetUtils)
	{
		this.widgetUtils = widgetUtils;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	@Required
	public void setCockpitAdminService(final CockpitAdminService cockpitAdminService)
	{
		this.cockpitAdminService = cockpitAdminService;
	}

	@Required
	public void setImportService(final ImportService importService)
	{
		this.importService = importService;
	}

	@Required
	public void setValidationService(final ValidationService validationService)
	{
		this.validationService = validationService;
	}

}
