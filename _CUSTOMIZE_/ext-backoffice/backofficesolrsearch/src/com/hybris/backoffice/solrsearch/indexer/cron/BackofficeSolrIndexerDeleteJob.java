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
package com.hybris.backoffice.solrsearch.indexer.cron;

import de.hybris.platform.core.PK;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexOperation;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.SolrConfig;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContextFactory;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.indexer.spi.Exporter;
import de.hybris.platform.solrfacetsearch.indexer.strategies.IndexOperationIdGenerator;
import de.hybris.platform.solrfacetsearch.model.SolrIndexModel;
import de.hybris.platform.solrfacetsearch.solr.Index;
import de.hybris.platform.solrfacetsearch.solr.SolrIndexService;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProvider;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProviderFactory;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Lists;
import com.hybris.backoffice.solrsearch.enums.SolrItemModificationType;
import com.hybris.backoffice.solrsearch.model.SolrModifiedItemModel;
import com.hybris.backoffice.solrsearch.utils.SolrPlatformUtils;

/**
 * @deprecated since 1808 Custom backoffice indexer jobs are deprecated. Standard indexing jobs are being used instead - {@link de.hybris.platform.solrfacetsearch.indexer.cron.SolrIndexerJob}
 */
@Deprecated
public class BackofficeSolrIndexerDeleteJob extends AbstractBackofficeSolrIndexerJob implements BeanFactoryAware
{
	private final Logger LOGGER = LoggerFactory.getLogger(BackofficeSolrIndexerDeleteJob.class);

	protected BeanFactory beanFactory;
	protected IndexerBatchContextFactory indexerBatchContextFactory;
	protected IndexOperationIdGenerator indexOperationIdGenerator;
	protected SolrSearchProviderFactory solrSearchProviderFactory;
	protected SolrIndexService solrIndexService;

	@Override
	protected void synchronizeIndexForType(final FacetSearchConfig facetSearchConfig, final IndexedType type, final Collection<PK> pks)
			throws IndexerException, SolrServiceException {
		IndexerBatchContext context = null;

		try
		{
			final SolrIndexModel activeIndex = solrIndexService.getActiveIndex(facetSearchConfig.getName(), type.getIdentifier());

			final SolrSearchProvider searchProvider = solrSearchProviderFactory.getSearchProvider(facetSearchConfig, type);

			final Index index = searchProvider.resolveIndex(facetSearchConfig, type, activeIndex.getQualifier());

			final long indexOperationId = indexOperationIdGenerator.generate(facetSearchConfig, type, index);

			context = indexerBatchContextFactory.createContext(
					indexOperationId,
					IndexOperation.DELETE,
					true,
					facetSearchConfig,
					type,
					Collections.emptyList());

			context.setIndex(index);

			final SolrConfig solrConfig = facetSearchConfig.getSolrConfig();

			final Exporter exporter = beanFactory.getBean(
					SolrPlatformUtils.createSolrExporterBeanName(solrConfig.getMode()), Exporter.class);

			final List<String> pksString = pks.stream().map(PK::getLongValueAsString).collect(Collectors.toList());

			indexerBatchContextFactory.prepareContext();
			context.setItems(Lists.newArrayList());
			indexerBatchContextFactory.initializeContext();

			exporter.exportToDeleteFromIndex(pksString, facetSearchConfig, type);

			indexerBatchContextFactory.destroyContext();
		}
		catch (final BeansException e)
		{
			LOGGER.warn("Solr exporter bean not found " + facetSearchConfig.getName(), e);

			if (context != null)
			{
				indexerBatchContextFactory.destroyContext(e);
			}
		}
		catch (final SolrServiceException|IndexerException|RuntimeException e)
		{
			if (context != null)
			{
				indexerBatchContextFactory.destroyContext(e);
			}

			throw e;
		}
	}

	@Override
	protected Collection<SolrModifiedItemModel> findModifiedItems()
	{
		return solrModifiedItemDAO.findByModificationType(SolrItemModificationType.DELETE);
	}

	@Override
	public void setBeanFactory(final BeanFactory beanFactory) throws BeansException
	{
		this.beanFactory = beanFactory;
	}

	@Required
	public void setIndexerBatchContextFactory(final IndexerBatchContextFactory indexerBatchContextFactory)
	{
		this.indexerBatchContextFactory = indexerBatchContextFactory;
	}

	@Required
	public void setSolrSearchProviderFactory(final SolrSearchProviderFactory solrSearchProviderFactory)
	{
		this.solrSearchProviderFactory = solrSearchProviderFactory;
	}

	@Required
	public void setIndexOperationIdGenerator(final IndexOperationIdGenerator indexOperationIdGenerator)
	{
		this.indexOperationIdGenerator = indexOperationIdGenerator;
	}

	@Required
	public void setSolrIndexService(final SolrIndexService solrIndexService)
	{
		this.solrIndexService = solrIndexService;
	}
}
