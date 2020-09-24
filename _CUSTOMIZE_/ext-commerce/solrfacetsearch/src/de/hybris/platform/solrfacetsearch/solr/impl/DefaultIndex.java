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
import de.hybris.platform.solrfacetsearch.solr.Index;


/**
 * Default implementation of {@link Index}.
 */
public class DefaultIndex implements Index
{
	private static final long serialVersionUID = 1L;

	private String name;

	private FacetSearchConfig facetSearchConfig;

	private IndexedType indexedType;

	private String qualifier;

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
	public FacetSearchConfig getFacetSearchConfig()
	{
		return facetSearchConfig;
	}

	public void setFacetSearchConfig(final FacetSearchConfig facetSearchConfig)
	{
		this.facetSearchConfig = facetSearchConfig;
	}

	@Override
	public IndexedType getIndexedType()
	{
		return indexedType;
	}

	public void setIndexedType(final IndexedType indexedType)
	{
		this.indexedType = indexedType;
	}

	@Override
	public String getQualifier()
	{
		return qualifier;
	}

	public void setQualifier(final String qualifier)
	{
		this.qualifier = qualifier;
	}
}
