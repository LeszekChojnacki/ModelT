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
package com.hybris.backoffice.solrsearch.indexer.impl;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.impl.DefaultSolrDocumentFactory;

import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.solrsearch.constants.BackofficesolrsearchConstants;
import com.hybris.backoffice.solrsearch.services.BackofficeFacetSearchConfigService;



/**
 * @deprecated since 1808, not used anymore
 */
@Deprecated
public class BackofficeSolrDocumentFactory extends DefaultSolrDocumentFactory
{
	private BackofficeFacetSearchConfigService backofficeFacetSearchConfigService;

	@Override
	protected void addCommonFields(final SolrInputDocument document, final IndexerBatchContext batchContext, final ItemModel model)
	{
		super.addCommonFields(document, batchContext, model);

		final boolean isBackofficeIndex = backofficeFacetSearchConfigService.isBackofficeSolrSearchConfiguredForName(batchContext.getFacetSearchConfig().getName());

		if (isBackofficeIndex)
		{
			final boolean configuredForType = backofficeFacetSearchConfigService.isSolrSearchConfiguredForType(model.getItemtype());

			if (configuredForType)
			{
				document.addField(BackofficesolrsearchConstants.TYPE_CODE_FIELD, model.getItemtype());
			}
		}
	}

	@Required
	public void setBackofficeFacetSearchConfigService(final BackofficeFacetSearchConfigService backofficeFacetSearchConfigService)
	{
		this.backofficeFacetSearchConfigService = backofficeFacetSearchConfigService;
	}

	public BackofficeFacetSearchConfigService getBackofficeFacetSearchConfigService()
	{
		return backofficeFacetSearchConfigService;
	}
}
