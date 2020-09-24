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
package com.hybris.backoffice.sync.decorators;

import de.hybris.platform.catalog.model.SyncItemJobModel;

import com.hybris.backoffice.cronjob.CronJobHistoryDataQuery;
import com.hybris.backoffice.widgets.processes.ProcessesQueryDecorator;


/**
 * Process data query decorator which adds {@link SyncItemJobModel} type code to query's
 * {@link CronJobHistoryDataQuery#getJobTypeCodes()}.
 *
 * @deprecated since 6.6 - no longer used
 */
@Deprecated
public class SyncJobProcessQueryDecorator implements ProcessesQueryDecorator
{
	@Override
	public CronJobHistoryDataQuery decorateQuery(final CronJobHistoryDataQuery cronJobHistoryDataQuery)
	{
		final CronJobHistoryDataQuery decorated = new CronJobHistoryDataQuery(cronJobHistoryDataQuery);
		decorated.addJobTypeCode(SyncItemJobModel._TYPECODE);
		return decorated;
	}
}
