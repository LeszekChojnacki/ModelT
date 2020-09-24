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
package de.hybris.platform.solrfacetsearch.search.impl;

import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import org.apache.solr.common.SolrDocument;


/**
 * Parameter class used to pass SolrDocument and SearchQuery to appropriate converter.
 * 
 * @see de.hybris.platform.solrfacetsearch.search.SearchQuery
 * 
 *
 */
public class SolrResult
{

	private final SolrDocument document;
	private final SearchQuery query;

	public SolrResult(final SolrDocument document, final SearchQuery query)
	{
		this.document = document;
		this.query = query;
	}

	public SolrDocument getDocument()
	{
		return document;
	}

	public SearchQuery getQuery()
	{
		return query;
	}

}
