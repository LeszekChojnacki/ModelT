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

import com.hybris.backoffice.cronjob.CronJobHistoryDataQuery;


/**
 * @deprecated since 6.6 - not used anymore
 */
@Deprecated
public interface ProcessesQueryDecorator
{
	/**
	 * Decorates given query
	 * 
	 * @param cronJobHistoryDataQuery
	 *           query to decorate
	 * @return decorated query
	 */
	CronJobHistoryDataQuery decorateQuery(CronJobHistoryDataQuery cronJobHistoryDataQuery);
}
