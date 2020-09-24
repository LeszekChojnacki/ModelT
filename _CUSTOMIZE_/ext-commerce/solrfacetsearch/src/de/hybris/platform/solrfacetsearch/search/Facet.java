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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;


/**
 * This class defines the root facet. For example, <b>hardware</b>(Facet) contains [cpu, monitor, memory](FacetValue).
 */
public class Facet implements Serializable, Comparable<Facet>
{
	private static final long serialVersionUID = 1L;

	private static final String NAME_MUST_NOT_BE_NULL = "name must not be null";

	private final String name;
	private final String displayName;
	private List<FacetValue> facetValues;
	private List<FacetValue> topFacetValues;
	private List<FacetValue> selectedFacetValues;
	private List<FacetValue> allFacetValues;
	private FacetType facetType;
	private int priority;
	private boolean multiselect;
	private final Set<String> tags = new HashSet<String>();

	public Facet(final String name, final List<FacetValue> facetValues)
	{
		if (name == null)
		{
			throw new IllegalArgumentException();
		}
		if (facetValues == null)
		{
			throw new IllegalArgumentException("facetValues must not be null");
		}
		this.name = name;
		this.displayName = name;
		this.facetValues = facetValues;
		this.topFacetValues = new ArrayList<>();
		this.selectedFacetValues = new ArrayList<>();
	}

	public Facet(final String name, final String displayName, final List<FacetValue> facetValues,
			final List<FacetValue> topFacetValues, final FacetType facetType, final int priority)
	{
		if (name == null)
		{
			throw new IllegalArgumentException(NAME_MUST_NOT_BE_NULL);
		}
		if (facetValues == null)
		{
			throw new IllegalArgumentException("facetValues must not be null");
		}
		this.name = name;
		this.displayName = StringUtils.isBlank(displayName) ? name : displayName;
		this.facetValues = facetValues;
		this.topFacetValues = topFacetValues;
		this.selectedFacetValues = new ArrayList<>();
		this.facetType = facetType;
		this.priority = priority;
	}

	public Facet(final String name, final String displayName, final List<FacetValue> facetValues,
			final List<FacetValue> topFacetValues, final List<FacetValue> selectedFacetValues, final FacetType facetType,
			final int priority)
	{
		if (name == null)
		{
			throw new IllegalArgumentException(NAME_MUST_NOT_BE_NULL);
		}
		if (facetValues == null)
		{
			throw new IllegalArgumentException("facetValues must not be null");
		}
		this.name = name;
		this.displayName = StringUtils.isBlank(displayName) ? name : displayName;
		this.facetValues = facetValues;
		this.topFacetValues = topFacetValues;
		this.selectedFacetValues = selectedFacetValues;
		this.facetType = facetType;
		this.priority = priority;
	}

	public void setFacetValues(final List<FacetValue> facetValues)
	{
		this.facetValues = facetValues;
	}

	public List<FacetValue> getFacetValues()
	{
		return facetValues;
	}

	public String getName()
	{
		return name;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public FacetType getFacetType()
	{
		return facetType;
	}

	public void setFacetType(final FacetType facetType)
	{
		this.facetType = facetType;
	}

	public int getPriority()
	{
		return priority;
	}

	public void setPriority(final int priority)
	{
		this.priority = priority;
	}

	public boolean isMultiselect()
	{
		return multiselect;
	}

	public void setMultiselect(final boolean multiselect)
	{
		this.multiselect = multiselect;
	}

	public List<FacetValue> getTopFacetValues()
	{
		return topFacetValues;
	}

	public void setTopFacetValues(final List<FacetValue> topFacetValues)
	{
		this.topFacetValues = topFacetValues;
	}

	public List<FacetValue> getSelectedFacetValues()
	{
		return selectedFacetValues;
	}

	public void setSelectedFacetValues(final List<FacetValue> selectedFacetValues)
	{
		this.selectedFacetValues = selectedFacetValues;
	}

	public List<FacetValue> getAllFacetValues()
	{
		return allFacetValues;
	}

	public void setAllFacetValues(final List<FacetValue> allFacetValues)
	{
		this.allFacetValues = allFacetValues;
	}

	public Set<String> getTags()
	{
		return tags;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (name.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (obj == null || this.getClass() != obj.getClass())
		{
			return false;
		}

		final Facet that = (Facet) obj;
		return new EqualsBuilder().append(this.name, that.name).isEquals();
	}

	@Override
	public int compareTo(@Nonnull final Facet other)
	{
		return name.compareTo(other.name);
	}

}
