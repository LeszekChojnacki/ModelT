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
package de.hybris.platform.solrfacetsearch.search.context.listeners;

import de.hybris.platform.solrfacetsearch.search.FacetSearchException;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchContext;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchListener;

import org.apache.log4j.Logger;
import org.apache.solr.common.util.SimpleOrderedMap;


/**
 * Listener to show the parsed solr query
 */
public class SolrQueryDebuggingListener implements FacetSearchListener
{
	private static final Logger LOG = Logger.getLogger(SolrQueryDebuggingListener.class);

	@Override
	public void beforeSearch(final FacetSearchContext facetSearchContext) throws FacetSearchException
	{
		facetSearchContext.getSearchQuery().addRawParam("debugQuery", "true");
	}

	@Override
	public void afterSearch(final FacetSearchContext facetSearchContext) throws FacetSearchException
	{
		LOG.info("Raw Query: " + ((SimpleOrderedMap) facetSearchContext.getSearchResult().getSolrObject().getResponse().get("debug")).get("rawquerystring"));
		LOG.info("Parsed Solr Query: " + ((SimpleOrderedMap) facetSearchContext.getSearchResult().getSolrObject().getResponse().get("debug")).get("parsedquery"));
		LOG.info("Filter Queries: " + ((SimpleOrderedMap) facetSearchContext.getSearchResult().getSolrObject().getResponse().get("debug")).get("filter_queries"));
		LOG.info("Solr Query explanation: " + ((SimpleOrderedMap) facetSearchContext.getSearchResult().getSolrObject().getResponse().get("debug")).get("explain"));
	}

	@Override
	public void afterSearchError(final FacetSearchContext facetSearchContext) throws FacetSearchException
	{
		//Do Nothing
	}
}
