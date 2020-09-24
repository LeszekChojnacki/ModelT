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
package com.hybris.backoffice.solrsearch.services.impl;

import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.daos.BackofficeJobsDao;
import com.hybris.backoffice.solrsearch.services.SolrIndexerJobsService;


/**
 * Default implementation for {@link SolrIndexerJobsService}
 */
public class DefaultSolrIndexerJobsService implements SolrIndexerJobsService
{

	private Set<String> jobNames;

	private BackofficeJobsDao backofficeJobsDao;
	private ModelService modelService;

	private static final Logger LOG = LoggerFactory.getLogger(DefaultSolrIndexerJobsService.class);


	@Override
	public void enableBackofficeSolrSearchIndexerJobs()
	{
		final Collection<CronJobModel> jobModels = backofficeJobsDao.findAllJobs(jobNames);
		for (final CronJobModel jobModel : jobModels)
		{
			jobModel.setActive(Boolean.TRUE);
			modelService.save(jobModel);
		}

		LOG.debug("BackofficeSolrSearchIndexer's Jobs have been enabled");
	}

	protected BackofficeJobsDao getBackofficeJobsDao()
	{
		return backofficeJobsDao;
	}

	@Required
	public void setBackofficeJobsDao(final BackofficeJobsDao backofficeJobsDao)
	{
		this.backofficeJobsDao = backofficeJobsDao;
	}

	protected ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	protected Collection<String> getJobNames()
	{
		return jobNames;
	}

	public void setJobNames(final Set<String> jobNames)
	{
		this.jobNames = jobNames;
	}

}
