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
package com.hybris.backoffice.cronjob;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.cronjob.model.CronJobHistoryModel;

import java.util.List;
import java.util.Optional;


/**
 * Facade used to fetch {@link CronJobHistoryModel}s.
 */
public interface CronJobHistoryFacade
{
	/**
	 * Gets a list of {@link CronJobHistoryModel}s.
	 *
	 * @param cronJobHistoryDataQuery
	 *           pojo which defines conditions for {@link CronJobHistoryModel}s.
	 * @return list of {@link CronJobHistoryModel}s.
	 */
	List<CronJobHistoryModel> getCronJobHistory(CronJobHistoryDataQuery cronJobHistoryDataQuery);

	/**
	 * Gets a list of {@link CronJobHistoryModel}s for given cronJob code.
	 *
	 * @param cronJobCode
	 *           code of a cronJob.
	 * @return a {@link CronJobHistoryModel} representing given cronJob.
	 */
	List<CronJobHistoryModel> getCronJobHistory(String cronJobCode);

	/**
	 * Gets a list of {@link CronJobHistoryModel}s for given cronJob codes.
	 *
	 * @param cronJobCodes
	 *           codes of a cronJobs.
	 * @return a {@link CronJobHistoryModel} representing given cronJob.
	 */
	List<CronJobHistoryModel> getCronJobHistory(List<String> cronJobCodes);

	/**
	 * Gets localized job name.
	 *
	 * @param cronJobHistoryModel
	 *           defines cron job to rerun.
	 * @return localized job name.
	 */
	String getJobName(CronJobHistoryModel cronJobHistoryModel);


	/**
	 * Lookup cronJob's log which is related to given cron job history. Depending on cronJob settings it returns db log
	 * or file log.
	 *
	 * @param cronJobHistoryModel
	 *           defines cron job to rerun.
	 * @return db log {@link de.hybris.platform.cronjob.model.JobLogModel} if exists or file log
	 *         {@link de.hybris.platform.cronjob.model.LogFileModel} otherwise.
	 */
	Optional<? extends ItemModel> findLog(final CronJobHistoryModel cronJobHistoryModel);


	/**
	 * Runs cron job related to given cron job history.
	 *
	 * @param cronJobHistory
	 *           defines cron job to rerun.
	 */
	void reRunCronJob(final CronJobHistoryModel cronJobHistory);
}
