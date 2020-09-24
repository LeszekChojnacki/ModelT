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

import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.JobPerformable;


/**
 * Extension to the {@link JobPerformable} contract that provides additional rule engine job specific methods
 * @param <T> entity of @{@link CronJobModel} type
 */
public interface RuleEngineJobPerformable<T extends CronJobModel> extends JobPerformable<T>
{
	/**
	 * Identifies whether provided cron job can be performed
	 *
	 * @param cronJob
	 * 					- cron job instance
	 * @return <code>true</code> in case cron job can be performed, <code>false</code> - otherwise
	 */
	boolean isPerformable(T cronJob);
}
