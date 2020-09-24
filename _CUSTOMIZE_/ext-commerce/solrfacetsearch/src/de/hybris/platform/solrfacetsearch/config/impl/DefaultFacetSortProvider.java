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
package de.hybris.platform.solrfacetsearch.config.impl;


import de.hybris.platform.solrfacetsearch.config.FacetSortProvider;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.search.FacetValue;

import java.util.Comparator;

import org.apache.commons.collections.ComparatorUtils;
import org.apache.log4j.Logger;


/**
 * Default sort provider for facets.
 */
public class DefaultFacetSortProvider implements FacetSortProvider
{
	private static final Logger LOG = Logger.getLogger(DefaultFacetSortProvider.class);
	private boolean descending;
	private Comparator<FacetValue> comparator;

	public boolean isDescending()
	{
		return descending;
	}

	// Optional spring inject
	public void setDescending(final boolean descending)
	{
		this.descending = descending;
	}

	@Override
	public Comparator<FacetValue> getComparatorForTypeAndProperty(final IndexedType indexedType,
			final IndexedProperty indexedProperty)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Resolved comparator for facet " + indexedProperty.getName() + " sorting : (reversed=" + isDescending() + ") "
					+ comparator.getClass());
		}
		return isDescending() ? (Comparator<FacetValue>) ComparatorUtils.reversedComparator(comparator) : comparator;
	}

	public void setComparator(final Comparator<FacetValue> comparator)
	{
		this.comparator = comparator;
	}
}
