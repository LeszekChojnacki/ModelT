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
package de.hybris.platform.ruleengineservices.setup.tasks.impl;

import de.hybris.platform.core.initialization.SystemSetupContext;
import de.hybris.platform.ruleengineservices.setup.tasks.MigrationTask;
import de.hybris.platform.servicelayer.interceptor.impl.InterceptorExecutionPolicy;
import de.hybris.platform.servicelayer.interceptor.impl.InterceptorExecutionPolicy.InterceptorType;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;


/**
 * Decorator which disables prepare and validate interceptor during MigrationTask executions. Please notice that it can
 * be applied only for such a task that are executed in the same thread.
 */
public class InterceptorUnawareMigrationTaskDecorator implements MigrationTask
{
	private final MigrationTask migrationTask;
	private SessionService sessionService;

	public InterceptorUnawareMigrationTaskDecorator(final MigrationTask migrationTask)
	{
		this.migrationTask = migrationTask;
	}

	@Override
	public void execute(final SystemSetupContext systemSetupContext)
	{
		final Map<String, Object> params = ImmutableMap.of(
				InterceptorExecutionPolicy.DISABLED_INTERCEPTOR_TYPES,
				ImmutableSet.of(InterceptorType.VALIDATE, InterceptorType.PREPARE));

		getSessionService().executeInLocalViewWithParams(params, new SessionExecutionBody()
		{
			@Override
			public void executeWithoutResult()
			{
				getMigrationTask().execute(systemSetupContext);
			}
		});
	}

	protected MigrationTask getMigrationTask()
	{
		return migrationTask;
	}

	protected SessionService getSessionService()
	{
		return sessionService;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}
}
