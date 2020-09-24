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
package com.hybris.backoffice.solrsearch.utils;

import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.SearchQuery.Operator;

import com.hybris.cockpitng.search.data.ValueComparisonOperator;


public final class BackofficeSolrUtil
{
	private BackofficeSolrUtil()
	{
		// Utility class
	}

	/**
	 * Converts {@link ValueComparisonOperator} to {@link SearchQuery}.{@link Operator} which is used to combine search
	 * conditions. Parameter with a value equal to {@link ValueComparisonOperator#AND} is converted to
	 * {@link SearchQuery}.{@link Operator#AND}. All other values of {@link ValueComparisonOperator} are converted to
	 * {@link SearchQuery}.{@link Operator#OR}.
	 *
	 * @param operator
	 *           operator which is converted to {@link Operator}.
	 * @return converted operator.
	 */
	public static Operator convertToSolrOperator(final ValueComparisonOperator operator)
	{
		return ValueComparisonOperator.AND.equals(operator) ? Operator.AND : Operator.OR;
	}
}
