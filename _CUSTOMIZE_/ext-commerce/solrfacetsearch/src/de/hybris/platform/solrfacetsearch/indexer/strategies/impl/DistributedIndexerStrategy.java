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
package de.hybris.platform.solrfacetsearch.indexer.strategies.impl;

import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.suspend.SystemIsSuspendedException;
import de.hybris.platform.core.threadregistry.OperationInfo;
import de.hybris.platform.core.threadregistry.RevertibleUpdate;
import de.hybris.platform.processing.distributed.DistributedProcessService;
import de.hybris.platform.processing.distributed.defaultimpl.DistributedProcessHelper;
import de.hybris.platform.processing.distributed.simple.data.CollectionBasedCreationData;
import de.hybris.platform.processing.distributed.simple.data.CollectionBasedCreationData.Builder;
import de.hybris.platform.processing.enums.DistributedProcessState;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.enums.IndexerOperationValues;
import de.hybris.platform.solrfacetsearch.indexer.IndexerContext;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.indexer.strategies.IndexerStrategy;
import de.hybris.platform.solrfacetsearch.model.SolrIndexerDistributedProcessModel;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * Implementation of {@link IndexerStrategy} that distributes work across cluster nodes.
 */
public class DistributedIndexerStrategy extends AbstractIndexerStrategy
{
	private static final Logger LOG = Logger.getLogger(DistributedIndexerStrategy.class);

	private static final String DISTRIBUTED_PROCESS_HANDLER = "indexerDistributedProcessHandler";
	private DistributedProcessService distributedProcessService;
	private ModelService modelService;

	@Override
	protected void doExecute(final IndexerContext indexerContext) throws IndexerException
	{
		try (RevertibleUpdate revertibleUpdate = markThreadAsSuspendable())
		{
			final CollectionBasedCreationData indexerProcessData = buildIndexerCreationData(indexerContext);
			final SolrIndexerDistributedProcessModel distributedIndexerProcess = createDistributedIndexerProcess(indexerProcessData,
					indexerContext);

			distributedProcessService.start(distributedIndexerProcess.getCode());

			waitForDistributedIndexer(distributedIndexerProcess.getCode());

		}
		catch (final Exception e)
		{
			throw new IndexerException(e);
		}
	}

	protected RevertibleUpdate markThreadAsSuspendable()
	{
		return OperationInfo.updateThread(OperationInfo.builder().withTenant(resolveTenantId())
				.withStatusInfo("Starting distributed indexing process as suspendable thread...").asSuspendableOperation().build());
	}

	protected CollectionBasedCreationData buildIndexerCreationData(final IndexerContext indexerContext)
	{
		final IndexConfig indexConfig = indexerContext.getFacetSearchConfig().getIndexConfig();

		final int batchSize = indexConfig.getBatchSize();
		final int maxRetries = indexConfig.getMaxRetries();

		final Builder indexerCreationDataBuilder = CollectionBasedCreationData.builder().withElements(indexerContext.getPks())
				.withProcessId(String.valueOf(indexerContext.getIndexOperationId())).withHandlerId(DISTRIBUTED_PROCESS_HANDLER)
				.withBatchSize(batchSize).withNumOfRetries(maxRetries)
				.withProcessModelClass(SolrIndexerDistributedProcessModel.class);
		if (StringUtils.isNotEmpty(indexConfig.getNodeGroup()))
		{
			indexerCreationDataBuilder.withNodeGroup(indexConfig.getNodeGroup());
		}

		return indexerCreationDataBuilder.build();
	}

	protected SolrIndexerDistributedProcessModel createDistributedIndexerProcess(
			final CollectionBasedCreationData indexerProcessData, final IndexerContext indexerContext)
	{
		final UserModel sessionUser = resolveSessionUser();
		final LanguageModel sessionLanguage = resolveSessionLanguage();
		final CurrencyModel sessionCurrency = resolveSessionCurrency();

		final Collection<String> indexedProperties = new ArrayList<>();
		for (final IndexedProperty indexedProperty : indexerContext.getIndexedProperties())
		{
			indexedProperties.add(indexedProperty.getName());
		}

		final SolrIndexerDistributedProcessModel distributedIndexerProcess = distributedProcessService.create(indexerProcessData);
		distributedIndexerProcess.setIndexOperationId(indexerContext.getIndexOperationId());
		distributedIndexerProcess.setIndexOperation(IndexerOperationValues.valueOf(indexerContext.getIndexOperation().name()));
		distributedIndexerProcess.setExternalIndexOperation(indexerContext.isExternalIndexOperation());
		distributedIndexerProcess.setFacetSearchConfig(indexerContext.getFacetSearchConfig().getName());
		distributedIndexerProcess.setIndexedType(indexerContext.getIndexedType().getUniqueIndexedTypeCode());
		distributedIndexerProcess.setIndexedProperties(indexedProperties);
		distributedIndexerProcess.setIndexerHints(indexerContext.getIndexerHints());

		// pass only the qualifier to avoid an additional query on the database
		distributedIndexerProcess.setIndex(indexerContext.getIndex().getQualifier());

		// session related parameters
		distributedIndexerProcess.setSessionUser(sessionUser.getUid());
		distributedIndexerProcess.setSessionCurrency(sessionCurrency == null ? null : sessionCurrency.getIsocode());
		distributedIndexerProcess.setSessionLanguage(sessionLanguage == null ? null : sessionLanguage.getIsocode());

		modelService.save(distributedIndexerProcess);

		return distributedIndexerProcess;
	}

	protected void waitForDistributedIndexer(final String processCode) throws IndexerException, InterruptedException
	{
		do
		{
			try
			{
				// await 'finished' state of process first
				final SolrIndexerDistributedProcessModel process = distributedProcessService.wait(processCode, 5);

				if (DistributedProcessHelper.isFinished(process) && DistributedProcessState.FAILED.equals(process.getState()))
				{
					throw new IndexerException("Indexing process has failed");
				}
				if (DistributedProcessHelper.isFinished(process))
				{
					return;
				}
			}
			catch (final SystemIsSuspendedException e)
			{
				if (LOG.isDebugEnabled())
				{
					LOG.debug("The system has been suspended. Retrying in 5 seconds.", e);
				}

				Thread.sleep(5000);
			}
		}
		while (true);
	}

	public DistributedProcessService getDistributedProcessService()
	{
		return distributedProcessService;
	}

	@Required
	public void setDistributedProcessService(final DistributedProcessService distributedProcessService)
	{
		this.distributedProcessService = distributedProcessService;
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
}
