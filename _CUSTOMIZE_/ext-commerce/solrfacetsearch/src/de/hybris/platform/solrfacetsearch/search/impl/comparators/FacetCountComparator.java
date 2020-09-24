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
 *
 */
public class FacetCountComparator implements Comparator<FacetValue>
{

	@Override
	public int compare(final FacetValue value1, final FacetValue value2)
	{
		if (value1 == null || value2 == null)
		{
			return 0;
		}
		final long long1 = value1.getCount();
		final long long2 = value2.getCount();
		if (long1 == long2)
		{
			return 0;
		}
		return long1 < long2 ? -1 : 1;
	}
}
