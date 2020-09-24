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
package de.hybris.platform.ruleengine.concurrency.impl;

import static com.google.common.base.Preconditions.checkArgument;

import de.hybris.platform.core.Tenant;
import de.hybris.platform.core.suspend.SuspendResumeService;
import de.hybris.platform.core.suspend.SystemStatus;
import de.hybris.platform.core.threadregistry.OperationInfo;
import de.hybris.platform.core.threadregistry.RegistrableThread;
import de.hybris.platform.ruleengine.concurrency.SuspendResumeTaskManager;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link SuspendResumeTaskManager}.
 */
public class DefaultSuspendResumeTaskManager implements SuspendResumeTaskManager
{

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSuspendResumeTaskManager.class);

	private Tenant currentTenant;
	private SuspendResumeService suspendResumeService;

	@Override
	public void registerAsNonSuspendableTask(final Thread task, final String statusInfo)
	{
		checkArgument(Objects.nonNull(task), "Task instance should be provided");

		if (task instanceof RegistrableThread)
		{
			((RegistrableThread)task).withInitialInfo(OperationInfo.builder()
					.withStatusInfo("Rule engine module deployment is in progress")
					.asNotSuspendableOperation()
					.withTenant(getCurrentTenant().getTenantID())
					.withCategory(OperationInfo.Category.TASK).build());
		}
		else
		{
			LOGGER.warn(
					"The worker thread {} is not an instance of RegisterableThread: suspension/resume functionality is not working correctly.",
					task.getName());         // NOSONAR
		}
	}

	@Override
	public void registerAsSuspendableTask(final Thread task, final String statusInfo)
	{
		checkArgument(Objects.nonNull(task), "Task instance should be provided");

		if (task instanceof RegistrableThread)
		{
			((RegistrableThread)task).withInitialInfo(OperationInfo.builder()
					.withStatusInfo("Rule engine module deployment is in progress")
					.withTenant(getCurrentTenant().getTenantID())
					.withCategory(OperationInfo.Category.TASK).build());
		}
		else
		{
			LOGGER.warn(
					"The worker thread {} is not an instance of RegisterableThread: suspension/resume functionality is not working correctly.",
					task.getName());          // NOSONAR
		}
	}

	@Override
	public boolean isSystemRunning()
	{
		return getSuspendResumeService().getSystemStatus().equals(SystemStatus.RUNNING);
	}

	protected Tenant getCurrentTenant()
	{
		return currentTenant;
	}

	@Required
	public void setCurrentTenant(final Tenant currentTenant)
	{
		this.currentTenant = currentTenant;
	}

	protected SuspendResumeService getSuspendResumeService()
	{
		return suspendResumeService;
	}

	@Required
	public void setSuspendResumeService(final SuspendResumeService suspendResumeService)
	{
		this.suspendResumeService = suspendResumeService;
	}
}
