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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;


/**
 * This class defines the contents of a facet. For example, <b>cpu</b> and <b>monitor</b> are FacetValue of the
 * <b>hardware</b> Facet. Each FacetValue contains concrete items???(either subFacetValue or items)
 *
 */
public class FacetValue implements Serializable, Comparable<FacetValue>
{
	private static final long serialVersionUID = 1L;

	private final String name;
	private final String displayName;
	private final long count;
	private final boolean selected;
	private Set<String> tags;

	public FacetValue(final String name, final long count, final boolean selected)
	{
		this(name, name, count, selected);
	}

	public FacetValue(final String name, final String displayName, final long count, final boolean selected)
	{
		this.name = name;
		this.displayName = displayName;
		this.count = count;
		this.selected = selected;
		this.tags = Collections.emptySet();
	}

	public String getName()
	{
		return name;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public long getCount()
	{
		return count;
	}

	public boolean isSelected()
	{
		return selected;
	}

	public void addTag(final String tag)
	{
		if (CollectionUtils.isEmpty(tags))
		{
			tags = new HashSet<>();
		}

		tags.add(tag);
	}

	public Set<String> getTags()
	{
		return tags;
	}

	@Override
	public int compareTo(final FacetValue anotherFacetValue)
	{
		return name.compareTo(anotherFacetValue.name);
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

		final FacetValue that = (FacetValue) obj;
		return new EqualsBuilder()
				.append(this.name, that.name)
				.append(this.displayName, that.displayName)
				.append(this.count, that.count)
				.append(this.selected, that.selected)
				.isEquals();
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.name, this.displayName, this.count);
	}


	@Override
	public String toString()
	{
		return getClass().getName() + " [" + name + " (" + count + ")]";
	}

}
