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
package de.hybris.platform.solrfacetsearch.solr.impl;

import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.solrfacetsearch.config.IndexOperation;
import de.hybris.platform.solrfacetsearch.daos.SolrIndexOperationDao;
import de.hybris.platform.solrfacetsearch.enums.IndexerOperationStatus;
import de.hybris.platform.solrfacetsearch.enums.IndexerOperationValues;
import de.hybris.platform.solrfacetsearch.model.SolrIndexModel;
import de.hybris.platform.solrfacetsearch.model.SolrIndexOperationModel;
import de.hybris.platform.solrfacetsearch.solr.SolrIndexOperationService;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrIndexOperationNotFoundException;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link SolrIndexOperationService}.
 */
public class DefaultSolrIndexOperationService implements SolrIndexOperationService
{
	private SolrIndexOperationDao solrIndexOperationDao;
	private ModelService modelService;
	private TimeService timeService;

	@Override
	public SolrIndexOperationModel getOperationForId(final long id) throws SolrServiceException
	{
		try
		{
			return solrIndexOperationDao.findIndexOperationById(id);
		}
		catch (final UnknownIdentifierException e)
		{
			throw new SolrIndexOperationNotFoundException("index not found: id=" + id, e);
		}
	}

	@Override
	public SolrIndexOperationModel startOperation(final SolrIndexModel index, final long id, final IndexOperation operation,
			final boolean external) throws SolrServiceException
	{
		ServicesUtil.validateParameterNotNullStandardMessage("index", index);
		ServicesUtil.validateParameterNotNullStandardMessage("operation", operation);

		final SolrIndexOperationModel indexOperationModel = modelService.create(SolrIndexOperationModel.class);
		indexOperationModel.setId(id);
		indexOperationModel.setIndex(index);
		indexOperationModel.setOperation(IndexerOperationValues.valueOf(operation.toString()));
		indexOperationModel.setStatus(IndexerOperationStatus.RUNNING);
		indexOperationModel.setStartTime(timeService.getCurrentTime());
		indexOperationModel.setExternal(external);

		try
		{
			modelService.save(indexOperationModel);
		}
		catch (final ModelSavingException e)
		{
			throw new SolrServiceException(e);
		}

		return indexOperationModel;
	}

	@Override
	public SolrIndexOperationModel endOperation(final long id, final boolean indexError) throws SolrServiceException
	{
		final SolrIndexOperationModel indexOperation = getOperationForId(id);

		// YTODO: check if running?

		indexOperation.setStatus(indexError ? IndexerOperationStatus.FAILED : IndexerOperationStatus.SUCCESS);
		indexOperation.setEndTime(timeService.getCurrentTime());

		try
		{
			modelService.save(indexOperation);
		}
		catch (final ModelSavingException e)
		{
			throw new SolrServiceException(e);
		}

		return indexOperation;
	}

	@Override
	public SolrIndexOperationModel cancelOperation(final long id) throws SolrServiceException
	{
		final SolrIndexOperationModel indexOperation = getOperationForId(id);

		// YTODO: check if running?

		indexOperation.setStatus(IndexerOperationStatus.ABORTED);
		indexOperation.setEndTime(timeService.getCurrentTime());

		try
		{
			modelService.save(indexOperation);
		}
		catch (final ModelSavingException e)
		{
			throw new SolrServiceException(e);
		}

		return indexOperation;
	}

	@Override
	public Date getLastIndexOperationTime(final SolrIndexModel index) throws SolrServiceException
	{
		ServicesUtil.validateParameterNotNullStandardMessage("index", index);

		final Optional<SolrIndexOperationModel> operation = solrIndexOperationDao.findLastSuccesfulIndexOperation(index);

		if (operation.isPresent())
		{
			return operation.get().getStartTime();
		}
		else
		{
			return new Date(0L);
		}
	}

	public SolrIndexOperationDao getSolrIndexOperationDao()
	{
		return solrIndexOperationDao;
	}

	@Required
	public void setSolrIndexOperationDao(final SolrIndexOperationDao solrIndexOperationDao)
	{
		this.solrIndexOperationDao = solrIndexOperationDao;
	}

	public ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	public TimeService getTimeService()
	{
		return timeService;
	}

	@Required
	public void setTimeService(final TimeService timeService)
	{
		this.timeService = timeService;
	}
}
