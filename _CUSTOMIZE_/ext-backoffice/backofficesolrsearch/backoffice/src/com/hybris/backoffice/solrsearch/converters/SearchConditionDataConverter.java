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
package com.hybris.backoffice.solrsearch.converters;

import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import java.util.List;

import com.hybris.backoffice.solrsearch.dataaccess.SearchConditionData;
import com.hybris.backoffice.solrsearch.dataaccess.SolrSearchCondition;


/**
 * Converts conditions into {@link SearchConditionData}
 */
public interface SearchConditionDataConverter
{
	/**
	 * @param conditions conditions to be converted
	 * @param globalOperator operator to connect the conditions
	 * @return SearchConditionData with appropriate assignments
	 */
	SearchConditionData convertConditions(final List<SolrSearchCondition> conditions,
			final SearchQuery.Operator globalOperator);
}
