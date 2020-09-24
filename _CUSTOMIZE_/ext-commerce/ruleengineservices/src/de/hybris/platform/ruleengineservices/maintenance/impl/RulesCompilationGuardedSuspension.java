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
package de.hybris.platform.ruleengineservices.maintenance.impl;

import de.hybris.platform.ruleengine.concurrency.GuardStatus;
import de.hybris.platform.ruleengine.concurrency.GuardedSuspension;
import de.hybris.platform.ruleengineservices.maintenance.RulesCompilationInProgressQueryEvent;
import de.hybris.platform.ruleengineservices.maintenance.RulesCompilationInProgressResponseEvent;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.event.EventService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationListener;


/**
 * Implements the {@link GuardedSuspension} interface for rules compilation
 */
public class RulesCompilationGuardedSuspension implements GuardedSuspension<String>
{

	private static final Logger LOGGER = LoggerFactory.getLogger(RulesCompilationGuardedSuspension.class);

	protected static final String VERIFICATION_LATCH_TIMEOUT = "ruleengineservices.compiler.guarded.suspension.timeout";

	private EventService eventService;
	private ConfigurationService configurationService;

	@Override
	public GuardStatus checkPreconditions(final String moduleName)
	{
		final CountDownLatch verificationLatch = new CountDownLatch(1);
		final GuardStatus guardStatus = GuardStatus.of(GuardStatus.Type.GO);
		final ApplicationListener<RulesCompilationInProgressResponseEvent> responseEventApplicationListener = new ApplicationListener<RulesCompilationInProgressResponseEvent>()
		{
			@Override
			public void onApplicationEvent(final RulesCompilationInProgressResponseEvent compilationInProgressResponse)
			{
				if (moduleName.equals(compilationInProgressResponse.getModuleName()))
				{
					guardStatus.setType(GuardStatus.Type.NO_GO);
					guardStatus
							.setMessage("Another process running the rules compilation for the module [" + moduleName + "] is detected");
					verificationLatch.countDown();
				}
			}
		};
		try
		{
			final Long latchTimeout = getConfigurationService().getConfiguration().getLong(VERIFICATION_LATCH_TIMEOUT, Long.valueOf(2));
			LOGGER.debug("Registering the application listener on RulesCompilationInProgressResponseEvent");
			getEventService().registerEventListener(responseEventApplicationListener);
			getEventService().publishEvent(new RulesCompilationInProgressQueryEvent(moduleName));
			LOGGER.debug("Waiting for the verificationLatch for [{}] seconds", latchTimeout);
			verificationLatch.await(latchTimeout, TimeUnit.SECONDS);
		}
		catch (final InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
		finally
		{
			LOGGER.debug("Unregistering the application listener on RulesCompilationInProgressResponseEvent");
			getEventService().unregisterEventListener(responseEventApplicationListener);
		}
		return guardStatus;
	}

	protected EventService getEventService()
	{
		return eventService;
	}

	@Required
	public void setEventService(final EventService eventService)
	{
		this.eventService = eventService;
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
}
