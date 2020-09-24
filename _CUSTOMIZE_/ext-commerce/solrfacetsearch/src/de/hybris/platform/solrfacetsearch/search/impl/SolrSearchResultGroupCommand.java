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

import de.hybris.platform.solrfacetsearch.search.SearchResultGroup;
import de.hybris.platform.solrfacetsearch.search.SearchResultGroupCommand;

import java.util.List;


public class SolrSearchResultGroupCommand implements SearchResultGroupCommand
{
	private static final long serialVersionUID = 1L;

	private String name;
	private long numberOfMatches;
	private long numberOfGroups;
	private List<SearchResultGroup> groups;

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	@Override
	public long getNumberOfMatches()
	{
		return numberOfMatches;
	}

	public void setNumberOfMatches(final long numberOfMatches)
	{
		this.numberOfMatches = numberOfMatches;
	}

	@Override
	public long getNumberOfGroups()
	{
		return numberOfGroups;
	}

	public void setNumberOfGroups(final long numberOfGroups)
	{
		this.numberOfGroups = numberOfGroups;
	}

	@Override
	public List<SearchResultGroup> getGroups()
	{
		return groups;
	}

	public void setGroups(final List<SearchResultGroup> groups)
	{
		this.groups = groups;
	}
}
