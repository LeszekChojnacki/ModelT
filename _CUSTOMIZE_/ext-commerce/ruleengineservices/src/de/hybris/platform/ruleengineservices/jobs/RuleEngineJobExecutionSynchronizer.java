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
package de.hybris.platform.ruleengineservices.jobs;

import de.hybris.platform.ruleengineservices.model.RuleEngineCronJobModel;


/**
 * This contract is meant to perform synchronization of the rule engine jobs execution through the means of
 * acquiring and and releasing locks on the respective resources that are essential for the job's execution.
 **/
public interface RuleEngineJobExecutionSynchronizer
{
	/**
	 * Performs lock acquisition of all the required resources for the given job
	 *
	 * @param cronJob
	 * 				- cron job instance
	 * @return <code>true</code> in case locks have been successfully acquired, <code>false</code> - otherwise
	 */
	boolean acquireLock(RuleEngineCronJobModel cronJob);

	/**
	 * Releases locks of all of the previously acquired resources for the given job
	 *
	 * @param cronJob
	 * 				- cron job instance
	 */
	void releaseLock(RuleEngineCronJobModel cronJob);
}
