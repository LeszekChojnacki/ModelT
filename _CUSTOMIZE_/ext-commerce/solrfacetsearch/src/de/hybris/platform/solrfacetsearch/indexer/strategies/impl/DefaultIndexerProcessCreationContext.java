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

import de.hybris.platform.processing.distributed.defaultimpl.DistributedProcessHandler;
import de.hybris.platform.processing.distributed.simple.SimpleDistributedProcessHandler;
import de.hybris.platform.processing.distributed.simple.context.SimpleProcessCreationContext;
import de.hybris.platform.processing.distributed.simple.data.SimpleAbstractDistributedProcessCreationData;
import de.hybris.platform.processing.distributed.simple.data.SimpleBatchCreationData;
import de.hybris.platform.processing.distributed.simple.id.SimpleBatchID;
import de.hybris.platform.processing.enums.BatchType;
import de.hybris.platform.processing.model.BatchModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.solrfacetsearch.model.SolrIndexerBatchModel;


public class DefaultIndexerProcessCreationContext extends SimpleProcessCreationContext
{
	public DefaultIndexerProcessCreationContext(final ModelService modelService,
			final SimpleAbstractDistributedProcessCreationData creationData)
	{
		super(modelService, creationData);
	}

	@Override
	protected DistributedProcessHandler.ModelWithDependencies<BatchModel> prepareBatch(final SimpleBatchCreationData data)
	{
		final SolrIndexerBatchModel initialBatch = modelService.create(SolrIndexerBatchModel.class);
		initialBatch.setId(SimpleBatchID.asInitialBatch().toString());
		initialBatch.setType(BatchType.INITIAL);
		initialBatch.setContext(data.getContext());
		initialBatch.setRemainingWorkLoad(SimpleDistributedProcessHandler.REMAINING_WORKLOAD);
		initialBatch.setRetries(creationData.getNumOfRetries());
		initialBatch.setScriptCode(creationData.getScriptCode());

		return DistributedProcessHandler.ModelWithDependencies.singleModel(initialBatch);
	}
}
