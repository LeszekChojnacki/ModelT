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

import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.core.threadregistry.OperationInfo;
import de.hybris.platform.core.threadregistry.RevertibleUpdate;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.indexer.IndexerContext;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.indexer.strategies.IndexerStrategy;
import de.hybris.platform.solrfacetsearch.indexer.workers.IndexerWorker;
import de.hybris.platform.solrfacetsearch.indexer.workers.IndexerWorkerFactory;
import de.hybris.platform.solrfacetsearch.indexer.workers.IndexerWorkerParameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Default implementation of {@link IndexerStrategy}.
 */
public class DefaultIndexerStrategy extends AbstractIndexerStrategy
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultIndexerStrategy.class);

	// dependencies
	private IndexerWorkerFactory indexerWorkerFactory;

	@Override
	protected void doExecute(final IndexerContext indexerContext) throws IndexerException
	{
		final IndexConfig indexConfig = indexerContext.getFacetSearchConfig().getIndexConfig();
		final List<PK> pks = indexerContext.getPks();
		final List<IndexerWorkerWrapper> workers = new ArrayList<>();

		final int batchSize = indexConfig.getBatchSize();
		final int numberOfThreads = indexConfig.getNumberOfThreads();
		final int numberOfWorkers = (int) Math.ceil((double) pks.size() / batchSize);

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Batch size: {}", batchSize);
			LOG.debug("Number of threads: {}", numberOfThreads);
			LOG.debug("Number of workers: {}", numberOfWorkers);
		}

		final ExecutorService executorService = createIndexerWorkersPool(numberOfThreads);
		final ExecutorCompletionService<Integer> completionService = new ExecutorCompletionService<>(executorService);
		final int maxRetries = Math.max(0, indexConfig.getMaxRetries());
		final int maxBatchRetries = Math.max(0, indexConfig.getMaxBatchRetries());

		try (RevertibleUpdate revertibleUpdate = markThreadAsSuspendable())
		{
			// create workers
			for (int index = 0, start = 0; index < numberOfWorkers; index++, start += batchSize)
			{
				final int end = Math.min(start + batchSize, pks.size());

				final int workerNumber = index;
				final List<PK> workerPks = pks.subList(start, end);

				final IndexerWorker indexerWorker = createIndexerWorker(indexerContext, workerNumber, workerPks);
				final IndexerWorkerWrapper indexerWorkerWrapper = new IndexerWorkerWrapper(indexerWorker, workerNumber,
						maxBatchRetries, workerPks);
				workers.add(indexerWorkerWrapper);
			}

			// run workers
			runWorkers(indexerContext, completionService, workers, maxRetries);
		}
		catch (final Exception exception)
		{
			throw new IndexerException(exception);
		}
		finally
		{
			executorService.shutdownNow();
		}
	}

	protected RevertibleUpdate markThreadAsSuspendable()
	{
		return OperationInfo.updateThread(OperationInfo.builder().withTenant(resolveTenantId())
				.withStatusInfo("Starting default indexing process as suspendable thread...").asSuspendableOperation().build());
	}

	protected void runWorkers(final IndexerContext indexerContext, final ExecutorCompletionService<Integer> completionService,
			final List<IndexerWorkerWrapper> workers, final int retriesLeft) throws IndexerException
	{
		int currentRetriesLeft = retriesLeft;
		final Map<Integer, IndexerWorkerWrapper> failedWorkers = new HashMap<>();

		LOG.debug("Submitting indexer workers (retries left: {})", Integer.valueOf(retriesLeft));

		for (final IndexerWorkerWrapper worker : workers)
		{
			completionService.submit(worker.getIndexerWorker(), worker.getWorkerNumber());
			failedWorkers.put(worker.getWorkerNumber(), worker);

			if (LOG.isDebugEnabled())
			{
				LOG.debug("Worker {} has been submitted (retries left: {}",
						worker.getWorkerNumber(), worker.getRetriesLeft());
			}
		}

		for (int i = 0; i < workers.size(); i++)
		{

			try
			{
				final Future<Integer> future = completionService.take();
				final Integer workerNumber = future.get();

				if (LOG.isDebugEnabled())
				{
					LOG.debug("Worker {} finished", workerNumber);
				}

				failedWorkers.remove(workerNumber);
			}
			catch (final ExecutionException e)
			{
				if (currentRetriesLeft <= 0)
				{
					throw new IndexerException("Indexer worker failed. Max number of retries in total has been reached", e);
				}

				currentRetriesLeft--;
			}
			catch (final InterruptedException e)
			{
				if (currentRetriesLeft <= 0)
				{
					throw new IndexerException("Indexer worker was interrupted. Max number of retries in total has been reached", e);
				}

				currentRetriesLeft--;
			}
		}

		if (!failedWorkers.isEmpty())
		{
			final List<IndexerWorkerWrapper> rerunWorkers = new ArrayList<>();

			for (final IndexerWorkerWrapper indexerWorkerWrapper : failedWorkers.values())
			{
				if (indexerWorkerWrapper.getRetriesLeft() <= 0)
				{
					throw new IndexerException("Indexer worker " + indexerWorkerWrapper.getWorkerNumber()
							+ " failed. Max number of retries per worker has been reached");
				}

				// recreate failed workers
				final IndexerWorker indexerWorker = createIndexerWorker(indexerContext, indexerWorkerWrapper.getWorkerNumber(),
						indexerWorkerWrapper.getWorkerPks());
				indexerWorkerWrapper.setIndexerWorker(indexerWorker);
				indexerWorkerWrapper.setRetriesLeft(indexerWorkerWrapper.getRetriesLeft() - 1);
				rerunWorkers.add(indexerWorkerWrapper);
			}

			runWorkers(indexerContext, completionService, rerunWorkers, currentRetriesLeft);
		}
	}

	protected ExecutorService createIndexerWorkersPool(final int numberOfThreads)
	{
		final ThreadFactory threadFactory = new ThreadFactory()
		{
			private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();

			@Override
			public Thread newThread(final Runnable runnable)
			{
				final Thread thread = defaultFactory.newThread(runnable);
				thread.setName("solr indexer thread");
				return thread;
			}
		};

		return Executors.newFixedThreadPool(numberOfThreads, threadFactory);
	}

	protected IndexerWorker createIndexerWorker(final IndexerContext indexerContext, final long workerNumber,
			final List<PK> workerPks) throws IndexerException
	{
		final Collection<String> indexedProperties = new ArrayList<>();
		for (final IndexedProperty indexedProperty : indexerContext.getIndexedProperties())
		{
			indexedProperties.add(indexedProperty.getName());
		}

		final IndexerWorkerParameters workerParameters = new IndexerWorkerParameters();
		workerParameters.setWorkerNumber(workerNumber);
		workerParameters.setIndexOperationId(indexerContext.getIndexOperationId());
		workerParameters.setIndexOperation(indexerContext.getIndexOperation());
		workerParameters.setExternalIndexOperation(indexerContext.isExternalIndexOperation());
		workerParameters.setFacetSearchConfig(indexerContext.getFacetSearchConfig().getName());
		workerParameters.setIndexedType(indexerContext.getIndexedType().getUniqueIndexedTypeCode());
		workerParameters.setIndexedProperties(indexedProperties);
		workerParameters.setPks(workerPks);
		workerParameters.setIndexerHints(indexerContext.getIndexerHints());

		// pass only the qualifier to avoid an additional query on the database
		workerParameters.setIndex(indexerContext.getIndex().getQualifier());

		// session related parameters
		final String tenantId = resolveTenantId();
		final UserModel sessionUser = resolveSessionUser();
		final LanguageModel sessionLanguage = resolveSessionLanguage();
		final CurrencyModel sessionCurrency = resolveSessionCurrency();
		workerParameters.setTenant(tenantId);
		workerParameters.setSessionUser(sessionUser.getUid());
		workerParameters.setSessionLanguage(sessionLanguage == null ? null : sessionLanguage.getIsocode());
		workerParameters.setSessionCurrency(sessionCurrency == null ? null : sessionCurrency.getIsocode());

		final IndexerWorker indexerWorker = indexerWorkerFactory.createIndexerWorker(getFacetSearchConfig());
		indexerWorker.initialize(workerParameters);
		return indexerWorker;
	}

	protected static class IndexerWorkerWrapper
	{
		private IndexerWorker indexerWorker;
		private int retriesLeft;
		private final Integer workerNumber;
		private final List<PK> workerPks;

		public IndexerWorkerWrapper(final IndexerWorker indexerWorker, final Integer workerNumber, final int retriesLeft,
				final List<PK> workerPks)
		{
			this.indexerWorker = indexerWorker;
			this.workerNumber = workerNumber;
			this.retriesLeft = retriesLeft;
			this.workerPks = workerPks;
		}

		public IndexerWorker getIndexerWorker()
		{
			return indexerWorker;
		}

		public void setIndexerWorker(final IndexerWorker indexerWorker)
		{
			this.indexerWorker = indexerWorker;
		}

		public int getRetriesLeft()
		{
			return retriesLeft;
		}

		public void setRetriesLeft(final int retriesLeft)
		{
			this.retriesLeft = retriesLeft;
		}

		public Integer getWorkerNumber()
		{
			return workerNumber;
		}

		public List<PK> getWorkerPks()
		{
			return workerPks;
		}
	}

	//dependencies
	public IndexerWorkerFactory getIndexerWorkerFactory()
	{
		return indexerWorkerFactory;
	}

	public void setIndexerWorkerFactory(final IndexerWorkerFactory indexerWorkerFactory)
	{
		this.indexerWorkerFactory = indexerWorkerFactory;
	}
}
