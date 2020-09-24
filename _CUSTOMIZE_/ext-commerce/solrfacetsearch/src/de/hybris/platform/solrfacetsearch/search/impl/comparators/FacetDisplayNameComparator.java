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

import org.apache.commons.lang.StringUtils;


/**
 * Compares facet values by display name {@link FacetValue#getDisplayName()}.
 */
public class FacetDisplayNameComparator implements Comparator<FacetValue>
{

	@Override
	public int compare(final FacetValue value1, final FacetValue value2)
	{
		if (value1 == null || value2 == null)
		{
			return 0;
		}
		if (StringUtils.isEmpty(value1.getDisplayName()) || StringUtils.isEmpty(value2.getDisplayName()))
		{
			return value1.getName().compareTo(value2.getName());
		}
		else
		{
			return value1.getDisplayName().compareTo(value2.getDisplayName());
		}
	}

}
