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

import de.hybris.platform.solrfacetsearch.search.Document;
import de.hybris.platform.solrfacetsearch.search.SearchResultGroup;

import java.util.List;


public class SolrSearchResultGroup implements SearchResultGroup
{
	private static final long serialVersionUID = 1L;

	private long numberOfResults;
	private String groupValue;
	private List<Document> documents;

	@Override
	public long getNumberOfResults()
	{
		return numberOfResults;
	}

	public void setNumberOfResults(final long numberOfResults)
	{
		this.numberOfResults = numberOfResults;
	}

	@Override
	public String getGroupValue()
	{
		return groupValue;
	}

	public void setGroupValue(final String groupValue)
	{
		this.groupValue = groupValue;
	}

	@Override
	public List<Document> getDocuments()
	{
		return documents;
	}

	public void setDocuments(final List<Document> documents)
	{
		this.documents = documents;
	}
}
