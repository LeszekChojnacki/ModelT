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
package com.hybris.backoffice.sync;

/**
 * Pojo which holds scheduled sync task and sync cron job code which runs the sync.
 */
public class SyncTaskExecutionInfo
{
	private final SyncTask syncTask;
	private final String syncCronJobCode;

	public SyncTaskExecutionInfo(final SyncTask syncTask, final String syncCronJobCode)
	{

		this.syncTask = syncTask;
		this.syncCronJobCode = syncCronJobCode;
	}

	public SyncTask getSyncTask()
	{
		return syncTask;
	}

	public String getSyncCronJobCode()
	{
		return syncCronJobCode;
	}
}
