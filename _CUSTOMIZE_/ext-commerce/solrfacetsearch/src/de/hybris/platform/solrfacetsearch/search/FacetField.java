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
package de.hybris.platform.solrfacetsearch.search;

import de.hybris.platform.solrfacetsearch.config.FacetType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class FacetField implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String field;
	private Integer priority;
	private FacetType facetType;
	private String displayNameProvider;
	private String sortProvider;
	private String topValuesProvider;
	private List<String> promotedValues = new ArrayList<>();
	private List<String> excludedValues = new ArrayList<>();

	public FacetField(final String field)
	{
		this.field = field;
	}

	public FacetField(final String field, final FacetType facetType)
	{
		this.field = field;
		this.facetType = facetType;
	}

	public String getField()
	{
		return field;
	}

	public void setField(final String field)
	{
		this.field = field;
	}

	public Integer getPriority()
	{
		return priority;
	}

	public void setPriority(final Integer priority)
	{
		this.priority = priority;
	}

	public FacetType getFacetType()
	{
		return facetType;
	}

	public void setFacetType(final FacetType facetType)
	{
		this.facetType = facetType;
	}

	public String getDisplayNameProvider()
	{
		return displayNameProvider;
	}

	public void setDisplayNameProvider(final String displayNameProvider)
	{
		this.displayNameProvider = displayNameProvider;
	}

	public String getSortProvider()
	{
		return sortProvider;
	}

	public void setSortProvider(final String sortProvider)
	{
		this.sortProvider = sortProvider;
	}

	public String getTopValuesProvider()
	{
		return topValuesProvider;
	}

	public void setTopValuesProvider(final String topValuesProvider)
	{
		this.topValuesProvider = topValuesProvider;
	}

	public List<String> getPromotedValues()
	{
		return promotedValues;
	}

	public void setPromotedValues(final List<String> promotedValues)
	{
		this.promotedValues = promotedValues;
	}

	public List<String> getExcludedValues()
	{
		return excludedValues;
	}

	public void setExcludedValues(final List<String> excludedValues)
	{
		this.excludedValues = excludedValues;
	}
}
