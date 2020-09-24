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
import de.hybris.platform.ruleengineservices.model.RuleEngineJobModel;

import java.util.function.Supplier;


/**
 * The interface to provide routine operations and checks to support the rule engine related jobs
 */
public interface RuleEngineJobService
{
	/**
	 * Retrieves (or creates one if absent) the RuleEngineJob, identified by given spring Bean name
	 *
	 * @param jobCode
	 * 		job code
	 * @param springBeanName
	 * 		spring bean name
	 * @return an instance of {@link RuleEngineJobModel}
	 */
	RuleEngineJobModel getRuleEngineJob(String jobCode, String springBeanName);

	/**
	 * Checks whether the RuleEngineJob has any active cron jobs
	 *
	 * @param ruleEngineJobCode
	 * 		code of the Job to check against
	 * @return boolean indicating whether this job has any active cron jobs
	 */
	boolean isRunning(String ruleEngineJobCode);

	/**
	 * Checks how many currently active cron jobs a RuleEngineJob has
	 *
	 * @param ruleEngineJobCode
	 * 		code of the Job to check against
	 * @return number of active cron jobs currently running
	 */
	int countRunningJobs(String ruleEngineJobCode);

	/**
	 * Given the rule engine Job, create a new instance of {@link RuleEngineCronJobModel} and run it if no other associated cron
	 * job is currently in execution
	 *
	 * @param ruleEngineJobCode
	 * 		the code of the Job to create the cron job with
	 * @param jobPerformableBeanName
	 * 		name of a bean of {@link de.hybris.platform.servicelayer.cronjob.JobPerformable} to run
	 * @param cronJobSupplier
	 * 		a supplier for the instance of {@link RuleEngineCronJobModel}
	 * @return instance of {@link RuleEngineCronJobModel}
	 */
	RuleEngineCronJobModel triggerCronJob(String ruleEngineJobCode, String jobPerformableBeanName,
			Supplier<RuleEngineCronJobModel> cronJobSupplier);
}
