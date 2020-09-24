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

import de.hybris.platform.persistence.hjmp.HJMPUtils;
import de.hybris.platform.servicelayer.exceptions.ModelRemovalException;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.solrfacetsearch.daos.SolrFacetSearchConfigDao;
import de.hybris.platform.solrfacetsearch.daos.SolrIndexDao;
import de.hybris.platform.solrfacetsearch.model.SolrIndexModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedTypeModel;
import de.hybris.platform.solrfacetsearch.solr.SolrIndexService;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrIndexNotFoundException;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;
import de.hybris.platform.tx.Transaction;
import de.hybris.platform.tx.TransactionBody;

import java.util.List;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link SolrIndexService}.
 */
public class DefaultSolrIndexService implements SolrIndexService
{
	protected static final String FACET_SEARCH_CONFIG_PARAM = "facetSearchConfig";
	protected static final String INDEXED_TYPE_PARAM = "indexedType";
	protected static final String QUALIFIER_PARAM = "qualifier";
	protected static final String INDEX_PARAM = "index";

	private SolrIndexDao solrIndexDao;
	private SolrFacetSearchConfigDao solrFacetSearchConfigDao;
	private ModelService modelService;
	private TimeService timeService;

	@Override
	public SolrIndexModel createIndex(final String facetSearchConfig, final String indexedType, final String qualifier)
			throws SolrServiceException
	{
		ServicesUtil.validateParameterNotNullStandardMessage(FACET_SEARCH_CONFIG_PARAM, facetSearchConfig);
		ServicesUtil.validateParameterNotNullStandardMessage(INDEXED_TYPE_PARAM, indexedType);
		ServicesUtil.validateParameterNotNullStandardMessage(QUALIFIER_PARAM, qualifier);

		return doInTxWithOptimisticLocking(() -> {
			final SolrFacetSearchConfigModel facetSearchConfigModel = findFacetSearchConfig(facetSearchConfig);
			final SolrIndexedTypeModel indexedTypeModel = findIndexedType(facetSearchConfigModel, indexedType);

			return createIndex(facetSearchConfigModel, indexedTypeModel, qualifier);
		});
	}

	@Override
	public List<SolrIndexModel> getAllIndexes() throws SolrServiceException
	{
		return solrIndexDao.findAllIndexes();
	}

	@Override
	public List<SolrIndexModel> getIndexesForConfigAndType(final String facetSearchConfig, final String indexedType)
			throws SolrServiceException
	{
		ServicesUtil.validateParameterNotNullStandardMessage(FACET_SEARCH_CONFIG_PARAM, facetSearchConfig);
		ServicesUtil.validateParameterNotNullStandardMessage(INDEXED_TYPE_PARAM, indexedType);

		final SolrFacetSearchConfigModel facetSearchConfigModel = findFacetSearchConfig(facetSearchConfig);
		final SolrIndexedTypeModel indexedTypeModel = findIndexedType(facetSearchConfigModel, indexedType);

		return solrIndexDao.findIndexesByConfigAndType(facetSearchConfigModel, indexedTypeModel);
	}

	@Override
	public SolrIndexModel getIndex(final String facetSearchConfig, final String indexedType, final String qualifier)
			throws SolrServiceException
	{
		ServicesUtil.validateParameterNotNullStandardMessage(FACET_SEARCH_CONFIG_PARAM, facetSearchConfig);
		ServicesUtil.validateParameterNotNullStandardMessage(INDEXED_TYPE_PARAM, indexedType);
		ServicesUtil.validateParameterNotNullStandardMessage(QUALIFIER_PARAM, qualifier);

		final SolrFacetSearchConfigModel facetSearchConfigModel = findFacetSearchConfig(facetSearchConfig);
		final SolrIndexedTypeModel indexedTypeModel = findIndexedType(facetSearchConfigModel, indexedType);

		return findIndex(facetSearchConfigModel, indexedTypeModel, qualifier);
	}

	@Override
	public SolrIndexModel getOrCreateIndex(final String facetSearchConfig, final String indexedType, final String qualifier)
			throws SolrServiceException
	{
		ServicesUtil.validateParameterNotNullStandardMessage(FACET_SEARCH_CONFIG_PARAM, facetSearchConfig);
		ServicesUtil.validateParameterNotNullStandardMessage(INDEXED_TYPE_PARAM, indexedType);
		ServicesUtil.validateParameterNotNullStandardMessage(QUALIFIER_PARAM, qualifier);

		return doInTxWithOptimisticLocking(() -> {
			final SolrFacetSearchConfigModel facetSearchConfigModel = findFacetSearchConfig(facetSearchConfig);
			final SolrIndexedTypeModel indexedTypeModel = findIndexedType(facetSearchConfigModel, indexedType);

			SolrIndexModel indexModel = null;

			try
			{
				indexModel = solrIndexDao.findIndexByConfigAndTypeAndQualifier(facetSearchConfigModel, indexedTypeModel, qualifier);
			}
			catch (final UnknownIdentifierException e)
			{
				indexModel = createIndex(facetSearchConfigModel, indexedTypeModel, qualifier);
			}

			return indexModel;
		});
	}

	@Override
	public void deleteIndex(final String facetSearchConfig, final String indexedType, final String qualifier)
			throws SolrServiceException
	{
		ServicesUtil.validateParameterNotNullStandardMessage(FACET_SEARCH_CONFIG_PARAM, facetSearchConfig);
		ServicesUtil.validateParameterNotNullStandardMessage(INDEXED_TYPE_PARAM, indexedType);
		ServicesUtil.validateParameterNotNullStandardMessage(QUALIFIER_PARAM, qualifier);

		doInTxWithOptimisticLocking(() -> {
			final SolrFacetSearchConfigModel facetSearchConfigModel = findFacetSearchConfig(facetSearchConfig);
			final SolrIndexedTypeModel indexedTypeModel = findIndexedType(facetSearchConfigModel, indexedType);

			final SolrIndexModel index = findIndex(facetSearchConfigModel, indexedTypeModel, qualifier);

			try
			{
				modelService.remove(index);
			}
			catch (final ModelRemovalException e)
			{
				throw new SolrServiceException(e);
			}

			return null;
		});
	}

	@Override
	public SolrIndexModel activateIndex(final String facetSearchConfig, final String indexedType, final String qualifier)
			throws SolrServiceException
	{
		ServicesUtil.validateParameterNotNullStandardMessage(FACET_SEARCH_CONFIG_PARAM, facetSearchConfig);
		ServicesUtil.validateParameterNotNullStandardMessage(INDEXED_TYPE_PARAM, indexedType);
		ServicesUtil.validateParameterNotNullStandardMessage(QUALIFIER_PARAM, qualifier);

		return doInTxWithOptimisticLocking(() -> doActivateIndex(facetSearchConfig, indexedType, qualifier));
	}

	protected SolrIndexModel doActivateIndex(final String facetSearchConfig, final String indexedType, final String qualifier)
			throws SolrServiceException
	{
		final SolrFacetSearchConfigModel facetSearchConfigModel = findFacetSearchConfig(facetSearchConfig);
		final SolrIndexedTypeModel indexedTypeModel = findIndexedType(facetSearchConfigModel, indexedType);

		SolrIndexModel activeIndex = null;
		final List<SolrIndexModel> indexes = solrIndexDao.findIndexesByConfigAndType(facetSearchConfigModel, indexedTypeModel);

		for (final SolrIndexModel index : indexes)
		{
			if (Objects.equals(index.getQualifier(), qualifier))
			{
				index.setActive(true);
				activeIndex = index;
			}
			else
			{
				index.setActive(false);
			}
		}

		if (activeIndex == null)
		{
			throw new SolrIndexNotFoundException("index not found: facetSearchConfig=" + facetSearchConfig + ", indexedType="
					+ indexedType + ", qualifier=" + qualifier);
		}

		try
		{
			modelService.saveAll(indexes);
		}
		catch (final ModelSavingException e)
		{
			throw new SolrServiceException(e);
		}

		return activeIndex;
	}

	@Override
	public SolrIndexModel getActiveIndex(final String facetSearchConfig, final String indexedType) throws SolrServiceException
	{
		ServicesUtil.validateParameterNotNullStandardMessage(FACET_SEARCH_CONFIG_PARAM, facetSearchConfig);
		ServicesUtil.validateParameterNotNullStandardMessage(INDEXED_TYPE_PARAM, indexedType);

		final SolrFacetSearchConfigModel facetSearchConfigModel = findFacetSearchConfig(facetSearchConfig);
		final SolrIndexedTypeModel indexedTypeModel = findIndexedType(facetSearchConfigModel, indexedType);

		try
		{
			return solrIndexDao.findActiveIndexByConfigAndType(facetSearchConfigModel, indexedTypeModel);
		}
		catch (final UnknownIdentifierException e)
		{
			throw new SolrIndexNotFoundException(e);
		}
	}

	protected SolrFacetSearchConfigModel findFacetSearchConfig(final String facetSearchConfig) throws SolrServiceException
	{
		try
		{
			return solrFacetSearchConfigDao.findFacetSearchConfigByName(facetSearchConfig);
		}
		catch (final UnknownIdentifierException e)
		{
			throw new SolrServiceException("Facet search config not found: facetSearchConfig=" + facetSearchConfig, e);
		}
	}

	protected SolrIndexedTypeModel findIndexedType(final SolrFacetSearchConfigModel facetSearchConfigModel,
			final String indexedType) throws SolrServiceException
	{
		final List<SolrIndexedTypeModel> indexedTypeModels = facetSearchConfigModel.getSolrIndexedTypes();
		if (CollectionUtils.isNotEmpty(indexedTypeModels))
		{
			for (final SolrIndexedTypeModel indexedTypeModel : indexedTypeModels)
			{
				if (Objects.equals(indexedTypeModel.getIdentifier(), indexedType))
				{
					return indexedTypeModel;
				}
			}
		}

		throw new SolrServiceException(
				"Indexed type not found: facetSearchConfig=" + facetSearchConfigModel.getName() + ", indexedType=" + indexedType);
	}

	protected SolrIndexModel findIndex(final SolrFacetSearchConfigModel facetSearchConfig, final SolrIndexedTypeModel indexedType,
			final String qualifier) throws SolrServiceException
	{
		try
		{
			return solrIndexDao.findIndexByConfigAndTypeAndQualifier(facetSearchConfig, indexedType, qualifier);
		}
		catch (final UnknownIdentifierException e)
		{
			throw new SolrIndexNotFoundException("index not found: facetSearchConfig=" + facetSearchConfig + ", indexedType="
					+ indexedType + ", qualifier=" + qualifier, e);
		}
	}

	protected SolrIndexModel createIndex(final SolrFacetSearchConfigModel facetSearchConfig,
			final SolrIndexedTypeModel indexedType, final String qualifier) throws SolrServiceException
	{
		final SolrIndexModel indexModel = modelService.create(SolrIndexModel.class);
		indexModel.setFacetSearchConfig(facetSearchConfig);
		indexModel.setIndexedType(indexedType);
		indexModel.setQualifier(qualifier);

		try
		{
			modelService.save(indexModel);
		}
		catch (final ModelSavingException e)
		{
			throw new SolrServiceException(e);
		}

		return indexModel;
	}

	protected <T> T doInTxWithOptimisticLocking(final ExecutionBody<T, SolrServiceException> action) throws SolrServiceException
	{
		Objects.requireNonNull(action, "action must not be null");

		try
		{
			return (T) Transaction.current().execute(new TransactionBody()
			{
				@Override
				public T execute() throws Exception
				{
					if (HJMPUtils.isOptimisticLockingEnabled())
					{
						return action.execute();
					}

					try
					{
						HJMPUtils.enableOptimisticLocking();
						return action.execute();
					}
					finally
					{
						HJMPUtils.clearOptimisticLockingSetting();
					}
				}
			});
		}
		catch (final SolrServiceException | RuntimeException e)
		{
			throw e;
		}
		catch (final Exception e)
		{
			throw new SolrServiceException(e);
		}
	}

	@FunctionalInterface
	protected interface ExecutionBody<T, E extends Exception>
	{
		/**
		 * Executes a code fragment and returns a result.
		 *
		 * @return the result
		 */
		T execute() throws E;
	}

	public SolrIndexDao getSolrIndexDao()
	{
		return solrIndexDao;
	}

	@Required
	public void setSolrIndexDao(final SolrIndexDao solrIndexDao)
	{
		this.solrIndexDao = solrIndexDao;
	}

	public SolrFacetSearchConfigDao getSolrFacetSearchConfigDao()
	{
		return solrFacetSearchConfigDao;
	}

	@Required
	public void setSolrFacetSearchConfigDao(final SolrFacetSearchConfigDao solrFacetSearchConfigDao)
	{
		this.solrFacetSearchConfigDao = solrFacetSearchConfigDao;
	}

	public TimeService getTimeService()
	{
		return timeService;
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

	@Required
	public void setTimeService(final TimeService timeService)
	{
		this.timeService = timeService;
	}
}

