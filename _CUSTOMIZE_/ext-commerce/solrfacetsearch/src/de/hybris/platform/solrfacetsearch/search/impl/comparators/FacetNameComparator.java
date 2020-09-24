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
package de.hybris.platform.solrfacetsearch.search.impl.comparators;

import de.hybris.platform.solrfacetsearch.search.FacetValue;

import java.util.Comparator;


/**
 *Compares facet values by name {@link FacetValue#getName()}.
 */
public class FacetNameComparator implements Comparator<FacetValue>
{

	@Override
	public int compare(final FacetValue value1, final FacetValue value2)
	{
		if (value1 == null || value2 == null)
		{
			return 0;
		}
		return value1.getName().compareTo(value2.getName());
	}
}
