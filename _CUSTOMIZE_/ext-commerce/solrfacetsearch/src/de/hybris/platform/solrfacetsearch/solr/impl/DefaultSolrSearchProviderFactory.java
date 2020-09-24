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

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.SolrServerMode;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProvider;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProviderFactory;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of the {@link SolrSearchProviderFactory}.
 */
public class DefaultSolrSearchProviderFactory implements SolrSearchProviderFactory
{
	private SolrStandaloneSearchProvider solrStandaloneSearchProvider;
	private SolrCloudSearchProvider solrCloudSearchProvider;
	private XmlExportSearchProvider xmlExportSearchProvider;

	@Override
	public SolrSearchProvider getSearchProvider(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType)
			throws SolrServiceException
	{
		final SolrServerMode mode = facetSearchConfig.getSolrConfig().getMode();

		switch (mode)
		{
			case STANDALONE:
				return solrStandaloneSearchProvider;
			case CLOUD:
				return solrCloudSearchProvider;
			case XML_EXPORT:
				return xmlExportSearchProvider;
			default:
				throw new SolrServiceException(
						"A Solr Server mode of: " + mode + " is not supported. Please specify standalone in the solr configuration.");
		}
	}

	public SolrCloudSearchProvider getSolrCloudSearchProvider()
	{
		return solrCloudSearchProvider;
	}

	@Required
	public void setSolrCloudSearchProvider(final SolrCloudSearchProvider solrCloudSearchProvider)
	{
		this.solrCloudSearchProvider = solrCloudSearchProvider;
	}

	public SolrStandaloneSearchProvider getSolrStandaloneSearchProvider()
	{
		return solrStandaloneSearchProvider;
	}

	@Required
	public void setSolrStandaloneSearchProvider(final SolrStandaloneSearchProvider solrStandaloneSearchProvider)
	{
		this.solrStandaloneSearchProvider = solrStandaloneSearchProvider;
	}

	public XmlExportSearchProvider getXmlExportSearchProvider()
	{
		return xmlExportSearchProvider;
	}

	@Required
	public void setXmlExportSearchProvider(final XmlExportSearchProvider xmlExportSearchProvider)
	{
		this.xmlExportSearchProvider = xmlExportSearchProvider;
	}

}
