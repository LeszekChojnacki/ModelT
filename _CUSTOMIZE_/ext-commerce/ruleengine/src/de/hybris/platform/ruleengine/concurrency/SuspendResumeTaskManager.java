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
package de.hybris.platform.ruleengine.concurrency;

/**
 * Register the threads with platform suspend/resume framework
 */
public interface SuspendResumeTaskManager
{
	/**
	 * Try to register the task as NonSuspendable thread
	 *
	 * @param task
	 * 		instance of {@link Thread} to register
	 * @param statusInfo
	 * 		status info to expose to framework
	 */
	void registerAsNonSuspendableTask(Thread task, String statusInfo);

	/**
	 * Try to register the task as Suspendable thread
	 *
	 * @param task
	 * 		instance of {@link Thread} to register
	 * @param statusInfo
	 * 		status info to expose to framework
	 */
	void registerAsSuspendableTask(Thread task, String statusInfo);

	/**
	 * Query id the system is in the running state
	 *
	 * @return true if the system is running, false otherwise
	 */
	boolean isSystemRunning();
}
