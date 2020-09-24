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
import de.hybris.platform.servicelayer.exceptions.ModelLoadingException;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hybris.backoffice.solrsearch.enums.SolrItemModificationType;
import com.hybris.backoffice.solrsearch.model.SolrModifiedItemModel;

/**
 * @deprecated since 6.6 Updating has been replaced by SolrIndexerCronJob (code='update-backofficeIndex-CronJob')
 */
@Deprecated
public class BackofficeSolrIndexerUpdateJob extends AbstractBackofficeSolrIndexerJob
{

	private static final Logger LOG = LoggerFactory.getLogger(BackofficeSolrIndexerUpdateJob.class);


	@Override
	protected Collection<SolrModifiedItemModel> findModifiedItems()
	{

		final Collection<SolrModifiedItemModel> byModificationType = solrModifiedItemDAO
				.findByModificationType(SolrItemModificationType.UPDATE);
		return byModificationType.stream().filter(this::isItemExisting).collect(Collectors.toList());
	}

	protected boolean isItemExisting(final SolrModifiedItemModel modifiedItem)
	{
		try
		{
			final Object model = modelService.get(PK.fromLong(modifiedItem.getModifiedPk().longValue()));
			if (model != null)
			{
				return true;
			}
			else
			{
				modelService.remove(modifiedItem);
				return false;
			}
		}
		catch (final ModelLoadingException e)
		{
			if (LOG.isDebugEnabled())
			{
				LOG.debug(e.getLocalizedMessage(), e);
			}
			modelService.remove(modifiedItem);
			return false;
		}
	}

	@Override
	protected void synchronizeIndexForType(final FacetSearchConfig facetSearchConfig, final IndexedType type,
			final Collection<PK> pks) throws IndexerException
	{
		indexerService.updateTypeIndex(facetSearchConfig, type, new ArrayList<>(pks));
	}

}
