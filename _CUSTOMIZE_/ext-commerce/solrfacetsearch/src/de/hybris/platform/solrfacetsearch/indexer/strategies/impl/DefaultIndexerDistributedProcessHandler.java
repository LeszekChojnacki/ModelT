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

import de.hybris.platform.processing.distributed.DistributedProcessService;
import de.hybris.platform.processing.distributed.defaultimpl.DistributedProcessHandler;
import de.hybris.platform.processing.distributed.simple.SimpleBatchProcessor;
import de.hybris.platform.processing.distributed.simple.SimpleDistributedProcessHandler;
import de.hybris.platform.processing.distributed.simple.context.SimpleProcessCreationContext;
import de.hybris.platform.processing.distributed.simple.data.SimpleAbstractDistributedProcessCreationData;
import de.hybris.platform.processing.distributed.simple.id.SimpleBatchID;
import de.hybris.platform.processing.enums.BatchType;
import de.hybris.platform.processing.model.BatchModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.solrfacetsearch.model.SolrIndexerBatchModel;


/**
 * Implementation of {@link DistributedProcessHandler} for distributed indexing.
 */
public class DefaultIndexerDistributedProcessHandler extends SimpleDistributedProcessHandler
{
	public DefaultIndexerDistributedProcessHandler(final ModelService modelService,
			final FlexibleSearchService flexibleSearchService, final DistributedProcessService distributedProcessService,
			final SimpleBatchProcessor simpleBatchProcessor)
	{
		super(modelService, flexibleSearchService, distributedProcessService, simpleBatchProcessor);
	}

	@Override
	protected SimpleProcessCreationContext prepareProcessCreationContext(
			final SimpleAbstractDistributedProcessCreationData processData)
	{
		return new DefaultIndexerProcessCreationContext(modelService, processData);
	}

	@Override
	protected BatchModel prepareResultBatch()
	{
		final SolrIndexerBatchModel resultBatch = modelService.create(SolrIndexerBatchModel.class);
		resultBatch.setId(SimpleBatchID.asResultBatchID().toString());
		resultBatch.setType(BatchType.RESULT);
		resultBatch.setRemainingWorkLoad(WORK_DONE);

		return resultBatch;
	}
}
