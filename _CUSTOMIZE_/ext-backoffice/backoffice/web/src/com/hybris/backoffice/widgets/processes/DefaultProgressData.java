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
package com.hybris.backoffice.widgets.processes;

import de.hybris.platform.cronjob.model.CronJobHistoryModel;

import java.util.Date;

import com.hybris.cockpitng.core.util.Validate;


public class DefaultProgressData implements ProgressData
{
	protected static final long INITIAL_SPEED = 1000;
	protected static final int INITIAL_MAX_PERCENTAGE = 10;
	protected static final int MAX_PERCENTAGE = 99;

	private ProgressPick previousUpdate;

	public DefaultProgressData()
	{
		previousUpdate = new ProgressPick(0, 0, 0, 0, INITIAL_MAX_PERCENTAGE);
	}

	@Override
	public void updateProgress(final CronJobHistoryModel cronJobHistoryModel, final Date currentTime)
	{
		Validate.notNull("Time cannot be null", currentTime);
		Validate.notNull("CronJobHistory cannot be null", cronJobHistoryModel);

		final long elapsedTime = currentTime.getTime() - cronJobHistoryModel.getStartTime().getTime();
		final int realPercentage = cronJobHistoryModel.getProgress() == null ? 0 : cronJobHistoryModel.getProgress().intValue();

		final int estimatedCurrentPercentage = calculateEstimatedCurrentPercentage(realPercentage, elapsedTime);

		final long timeToIncreaseOnePercent;
		final int maxPercentage;
		if (progressShouldBeMocked(realPercentage, estimatedCurrentPercentage))
		{
			maxPercentage = INITIAL_MAX_PERCENTAGE;
			timeToIncreaseOnePercent = calculateInitialSpeed(estimatedCurrentPercentage, maxPercentage);
		}
		else
		{
			timeToIncreaseOnePercent = calculateTimeToIncreaseOnePercent(elapsedTime, realPercentage, estimatedCurrentPercentage);
			maxPercentage = MAX_PERCENTAGE;
		}

		replacePreviousUpdateWithNewOne(
				new ProgressPick(realPercentage, estimatedCurrentPercentage, timeToIncreaseOnePercent, elapsedTime, maxPercentage));
	}

	private static long calculateInitialSpeed(final int estimatedCurrentPercentage, final int maxPercentage)
	{
		if (estimatedCurrentPercentage >= maxPercentage)
		{
			return 0;
		}
		return INITIAL_SPEED;
	}

	protected int calculateEstimatedCurrentPercentage(final int realPercentage, final long elapsedTime)
	{
		final int estimatedCurrentPercentage;
		if (previousUpdate.getTimeToIncreaseOnePercent() > 0)
		{
			estimatedCurrentPercentage = (Math
					.round(((float) (elapsedTime - previousUpdate.getElapsedTime())) / previousUpdate.getTimeToIncreaseOnePercent()))
					+ previousUpdate.getEstimatedPercentage();
		}
		else
		{
			estimatedCurrentPercentage = previousUpdate.getEstimatedPercentage();
		}

		return getOptimizedEstimatedCurrentPercentage(realPercentage, estimatedCurrentPercentage);
	}

	protected int getOptimizedEstimatedCurrentPercentage(final int realPercentage, final int estimatedCurrentPercentage)
	{
		if (realPercentage - estimatedCurrentPercentage >= 1) //prevent situations when increase time is longer than heartbeat
		{
			return estimatedCurrentPercentage + 1;
		}
		if (estimatedCurrentPercentage >= previousUpdate.getMaxPercentage()) //prevent to reach 100% before job is done
		{
			return previousUpdate.getMaxPercentage();
		}

		return estimatedCurrentPercentage;
	}

	protected long calculateTimeToIncreaseOnePercent(final long elapsedTime, final int realPercentage,
			final int estimatedCurrentPercentage)
	{
		long timeToIncreaseOnePercent = 0;
		final int estimatedPercentageInNextUpdate = 2 * realPercentage - previousUpdate.getRealPercentage();
		if (realPercentage > 0 && estimatedCurrentPercentage < MAX_PERCENTAGE && elapsedTime > 0
				&& estimatedPercentageInNextUpdate > estimatedCurrentPercentage)
		{
			timeToIncreaseOnePercent = (elapsedTime - previousUpdate.getElapsedTime())
					/ (estimatedPercentageInNextUpdate - estimatedCurrentPercentage);
		}
		return timeToIncreaseOnePercent;
	}

	protected boolean progressShouldBeMocked(final int realPercentage, final int estimatedCurrentPercentage)
	{
		return realPercentage <= 0 && estimatedCurrentPercentage <= INITIAL_MAX_PERCENTAGE;
	}

	protected void replacePreviousUpdateWithNewOne(final ProgressPick progressPick)
	{
		previousUpdate = progressPick;
	}

	@Override
	public int getEstimatedCurrentPercentage()
	{
		return previousUpdate.getEstimatedPercentage();
	}

	@Override
	public int getMaxPercentage()
	{
		return previousUpdate.getMaxPercentage();
	}

	@Override
	public long getTimeToIncreaseOnePercent()
	{
		return previousUpdate.getTimeToIncreaseOnePercent();
	}
}

class ProgressPick
{
	private final int realPercentage;
	private final int estimatedPercentage;
	private final long timeToIncreaseOnePercent;
	private final long elapsedTime;
	private final int maxPercentage;

	public ProgressPick(final int realPercentage, final int estimatedPercentage, final long timeToIncreaseOnePercent,
			final long elapsedTime, final int maxPercentage)
	{
		this.realPercentage = realPercentage;
		this.estimatedPercentage = estimatedPercentage;
		this.timeToIncreaseOnePercent = timeToIncreaseOnePercent;
		this.elapsedTime = elapsedTime;
		this.maxPercentage = maxPercentage;
	}

	public int getRealPercentage()
	{
		return realPercentage;
	}

	public int getEstimatedPercentage()
	{
		return estimatedPercentage;
	}

	public long getTimeToIncreaseOnePercent()
	{
		return timeToIncreaseOnePercent;
	}

	public long getElapsedTime()
	{
		return elapsedTime;
	}

	public int getMaxPercentage()
	{
		return maxPercentage;
	}
}
