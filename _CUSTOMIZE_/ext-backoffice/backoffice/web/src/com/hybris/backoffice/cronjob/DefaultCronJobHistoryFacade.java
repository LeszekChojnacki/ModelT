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

import de.hybris.platform.catalog.model.synchronization.CatalogVersionSyncCronJobModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobHistoryModel;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.cronjob.model.JobLogModel;
import de.hybris.platform.cronjob.model.JobModel;
import de.hybris.platform.cronjob.model.LogFileModel;
import de.hybris.platform.servicelayer.cronjob.CronJobHistoryInclude;
import de.hybris.platform.servicelayer.cronjob.CronJobHistoryService;
import de.hybris.platform.servicelayer.cronjob.CronJobService;
import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Sets;
import com.hybris.backoffice.sync.facades.SynchronizationFacade;
import com.hybris.cockpitng.core.user.CockpitUserService;
import com.hybris.cockpitng.core.util.Validate;
import com.hybris.cockpitng.labels.LabelService;


/**
 * Default implementation of {@link CronJobHistoryFacade}
 */
public class DefaultCronJobHistoryFacade implements CronJobHistoryFacade
{
	private CronJobHistoryService cronJobHistoryService;
	private CockpitUserService cockpitUserService;
	private UserService userService;
	private TimeService timeService;
	private LabelService labelService;
	private CronJobService cronJobService;
	private SynchronizationFacade synchronizationFacade;
	private Map<String, CronJobHistoryInclude> cronJobHistoryIncludes;

	@Override
	public List<CronJobHistoryModel> getCronJobHistory(final CronJobHistoryDataQuery query)
	{
		final Date startDate = calculateStartDate(query);
		final Date endDate = null;
		final UserModel currentUser = query.isShowExecutedByOtherUsers() ? null : getCurrentUser();
		final CronJobStatus cronJobStatus = query.isShowFinishedJobs() ? null : CronJobStatus.RUNNING;

		return cronJobHistoryService.getCronJobHistoryBy(Sets.newHashSet(cronJobHistoryIncludes.values()),currentUser,
				startDate, endDate, cronJobStatus);
	}

	protected Date calculateStartDate(final CronJobHistoryDataQuery cronJobHistoryDataQuery)
	{
		final Date currentTime = timeService.getCurrentTime();
		return Date.from(currentTime.toInstant().minus(cronJobHistoryDataQuery.getTimeRange()));
	}

	@Override
	public List<CronJobHistoryModel> getCronJobHistory(final String cronJobCode)
	{
		return cronJobHistoryService.getCronJobHistoryBy(cronJobCode);
	}

	@Override
	public List<CronJobHistoryModel> getCronJobHistory(final List<String> cronJobCodes)
	{
		return CollectionUtils.isNotEmpty(cronJobCodes) ? cronJobHistoryService.getCronJobHistoryBy(cronJobCodes)
				: Collections.emptyList();
	}


	@Override
	public String getJobName(final CronJobHistoryModel cronJobHistoryModel)
	{
		final JobModel job = cronJobService.getJob(cronJobHistoryModel.getJobCode());
		return labelService.getObjectLabel(job);
	}

	protected UserModel getCurrentUser()
	{
		final String currentUser = cockpitUserService.getCurrentUser();
		return userService.getUserForUID(currentUser);
	}

	@Override
	public Optional<? extends ItemModel> findLog(final CronJobHistoryModel cronJobHistoryModel)
	{
		Validate.notNull("cronJobHistoryModel cannot be null", cronJobHistoryModel);
		final CronJobModel cronJob = cronJobHistoryModel.getCronJob();
		final Date execStartTime = cronJobHistoryModel.getStartTime();
		if (cronJob != null && execStartTime != null)
		{
			final Optional<JobLogModel> recentDBLog = lookupDBLog(cronJob, execStartTime);
			if (recentDBLog.isPresent())
			{
				return recentDBLog;
			}
			else
			{
				return lookupLogFile(cronJob, execStartTime);
			}
		}
		return Optional.empty();
	}

	protected Optional<LogFileModel> lookupLogFile(final CronJobModel cronJob, final Date cronJobExecutionStartTime)
	{
		Validate.notNull("cronJob cannot be null", cronJob);
		Validate.notNull("cronJobExecutionStartTime cannot be null", cronJobExecutionStartTime);
		if (cronJob.getLogToFile().booleanValue() && CollectionUtils.isNotEmpty(cronJob.getLogFiles()))
		{
			return cronJob.getLogFiles().stream().filter(log -> log.getCreationtime().after(cronJobExecutionStartTime))
					.sorted(Comparator.comparing(LogFileModel::getCreationtime)).findFirst();
		}
		return Optional.empty();
	}

	protected Optional<JobLogModel> lookupDBLog(final CronJobModel cronJob, final Date cronJobExecutionStartTime)
	{
		Validate.notNull("cronJob cannot be null", cronJob);
		Validate.notNull("cronJobExecutionStartTime cannot be null", cronJobExecutionStartTime);
		if (cronJob.getLogToDatabase().booleanValue() && CollectionUtils.isNotEmpty(cronJob.getLogs()))
		{
			return cronJob.getLogs().stream().filter(log -> log.getCreationtime().after(cronJobExecutionStartTime))
					.sorted(Comparator.comparing(JobLogModel::getCreationtime)).findFirst();
		}
		return Optional.empty();
	}

	@Override
	public void reRunCronJob(final CronJobHistoryModel cronJobHistory)
	{
		if (cronJobHistory == null)
		{
			return;
		}

		final CronJobModel cronJob = cronJobHistory.getCronJob();
		if (cronJob instanceof CatalogVersionSyncCronJobModel)
		{
			synchronizationFacade.reRunCronJob((CatalogVersionSyncCronJobModel) cronJobHistory.getCronJob());
		}
		else
		{
			cronJobService.performCronJob(cronJobHistory.getCronJob());
		}
	}

	@Required
	public void setCockpitUserService(final CockpitUserService cockpitUserService)
	{
		this.cockpitUserService = cockpitUserService;
	}

	public CockpitUserService getCockpitUserService()
	{
		return cockpitUserService;
	}

	@Required
	public void setCronJobHistoryService(final CronJobHistoryService cronJobHistoryService)
	{
		this.cronJobHistoryService = cronJobHistoryService;
	}

	public CronJobHistoryService getCronJobHistoryService()
	{
		return cronJobHistoryService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	public UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setTimeService(final TimeService timeService)
	{
		this.timeService = timeService;
	}

	public TimeService getTimeService()
	{
		return timeService;
	}

	@Required
	public LabelService getLabelService()
	{
		return labelService;
	}

	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}

	public CronJobService getCronJobService()
	{
		return cronJobService;
	}

	@Required
	public void setCronJobService(final CronJobService cronJobService)
	{
		this.cronJobService = cronJobService;
	}

	@Required
	public void setSynchronizationFacade(final SynchronizationFacade synchronizationFacade)
	{
		this.synchronizationFacade = synchronizationFacade;
	}

	public SynchronizationFacade getSynchronizationFacade()
	{
		return synchronizationFacade;
	}

	public Map<String, CronJobHistoryInclude> getCronJobHistoryIncludes()
	{
		return cronJobHistoryIncludes;
	}

	@Required
	public void setCronJobHistoryIncludes(final Map<String, CronJobHistoryInclude> cronJobHistoryIncludes)
	{
		this.cronJobHistoryIncludes = cronJobHistoryIncludes;
	}
}
