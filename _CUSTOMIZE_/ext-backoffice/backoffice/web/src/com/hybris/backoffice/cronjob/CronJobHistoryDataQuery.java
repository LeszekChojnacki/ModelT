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


import de.hybris.platform.cronjob.model.CronJobHistoryModel;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import com.hybris.cockpitng.core.context.impl.DefaultCockpitContext;


/**
 * A pojo which represents a query to retrieve {@link CronJobHistoryModel}s.
 */
public class CronJobHistoryDataQuery extends DefaultCockpitContext
{
	private final Duration timeRange;
	private final boolean showExecutedByOtherUsers;
	private final boolean showFinishedJobs;
	/**
	 * @deprecated since 6.6 - not used anymore
	 */
	@Deprecated
	private Set<String> jobTypeCodes;

	/**
	 * @param timeRange
	 *           time range since current date.
	 * @param showExecutedByOtherUsers
	 *           defines if cronJobs executed by other users than current session's user should be included.
	 * @param showFinishedJobs
	 *           defines if finished cronJobs should be included.
	 */
	public CronJobHistoryDataQuery(final Duration timeRange, final boolean showExecutedByOtherUsers,
			final boolean showFinishedJobs)
	{
		this(timeRange, showExecutedByOtherUsers, showFinishedJobs, null);
	}

	/**
	 * @param timeRange
	 *           time range since current date.
	 * @param showExecutedByOtherUsers
	 *           defines if cronjobs executed by other users than current session's user should be included.
	 * @param showFinishedJobs
	 *           defines if finished cronJobs should be included.
	 * @param jobTypeCodes
	 *           type codes of jobs which should be found.
	 */
	public CronJobHistoryDataQuery(final Duration timeRange, final boolean showExecutedByOtherUsers,
			final boolean showFinishedJobs, final Set<String> jobTypeCodes)
	{
		this.timeRange = timeRange;
		this.showExecutedByOtherUsers = showExecutedByOtherUsers;
		this.showFinishedJobs = showFinishedJobs;
	}

	/**
	 * Copying constructor.
	 *
	 * @param query
	 *           query being copied.
	 */
	public CronJobHistoryDataQuery(final CronJobHistoryDataQuery query)
	{
		timeRange = query.timeRange;
		showFinishedJobs = query.showFinishedJobs;
		showExecutedByOtherUsers = query.showExecutedByOtherUsers;
		CronJobHistoryDataQuery.this.addAllParameters(query);
	}

	public Duration getTimeRange()
	{
		return timeRange;
	}

	public boolean isShowExecutedByOtherUsers()
	{
		return showExecutedByOtherUsers;
	}

	public boolean isShowFinishedJobs()
	{
		return showFinishedJobs;
	}

	/**
	 * adds to list of type codes given type code
	 *
	 * @param typeCode
	 *           job type code.
	 *
	 * @deprecated since 6.6 - not used anymore
	 */
	@Deprecated
	public void addJobTypeCode(final String typeCode)
	{
		if (jobTypeCodes == null)
		{
			jobTypeCodes = new HashSet<>();
		}
		jobTypeCodes.add(typeCode);
	}

	/**
	 * @deprecated since 6.6 - not used anymore
	 */
	@Deprecated
	public Set<String> getJobTypeCodes()
	{
		return jobTypeCodes;
	}

	/**
	 * @deprecated since 6.6 - not used anymore
	 */
	@Deprecated
	public void setJobTypeCodes(final Set<String> jobTypeCodes)
	{
		this.jobTypeCodes = jobTypeCodes;
	}
}
