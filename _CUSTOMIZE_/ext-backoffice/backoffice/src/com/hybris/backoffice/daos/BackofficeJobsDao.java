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
package com.hybris.backoffice.daos;

import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.cronjob.model.JobModel;
import de.hybris.platform.servicelayer.internal.dao.Dao;

import java.util.Collection;

public interface BackofficeJobsDao extends Dao
{
	/**
	 * Finds all JobModels for given codes
	 * @param codes
	 * @return {@link JobModel} for given codes
     */
	Collection<CronJobModel> findAllJobs(Collection<String> codes);
}
