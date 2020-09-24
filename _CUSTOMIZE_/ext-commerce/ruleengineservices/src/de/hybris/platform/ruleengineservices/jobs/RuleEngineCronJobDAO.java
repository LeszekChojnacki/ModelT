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

import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.ruleengineservices.model.RuleEngineJobModel;


/**
 * DAO object to access to common repository related Job information
 */
public interface RuleEngineCronJobDAO
{

	/**
	 * Get number of rule engine cron jobs for a given job code and specified statuses
	 *
	 * @param jobCode
	 * 		code of the related job
	 * @param statuses
	 * 		array of statuses to filter the cron jobs
	 * @return a number of cron jobs satisfying the search criteria
	 */
	int countCronJobsByJob(String jobCode, CronJobStatus... statuses);

	/**
	 * Try to get the registered rule engine Job 
	 *
	 * @param jobCode
	 * 		the code of job
	 * @return the instance of {@link RuleEngineJobModel}
	 */
	RuleEngineJobModel findRuleEngineJobByCode(String jobCode);

}
