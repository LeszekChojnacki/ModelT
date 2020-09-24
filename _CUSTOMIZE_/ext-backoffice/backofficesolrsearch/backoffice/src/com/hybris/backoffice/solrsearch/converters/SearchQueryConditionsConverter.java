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


import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import java.util.List;

import com.hybris.backoffice.solrsearch.dataaccess.SolrSearchCondition;
import com.hybris.cockpitng.search.data.SearchQueryCondition;


/**
 * Converts {@link SearchQueryCondition} list into list of {@link SolrSearchCondition}
 */
public interface SearchQueryConditionsConverter
{
	/**
	 * Converts a list of {@link SearchQueryCondition} to a list of {@link SolrSearchCondition}. Converted
	 * condition list contains only one element for a specific attribute combined with a language (if attribute is
	 * localized) on the same level of a query
	 * {@link SolrSearchCondition#SolrSearchCondition(List, SearchQuery.Operator)} as opposed
	 * to SearchQueryCondition list which can have multiple conditions for the same attribute.
	 *
	 * @param conditions conditions from simple search.
	 * @param globalOperator operator used to join conditions.
	 * @param indexedType solr configuration for indexed type.
	 * @return list of converted {@link SolrSearchCondition}.
	 */
	List<SolrSearchCondition> convert(final List<? extends SearchQueryCondition> conditions,
									  final SearchQuery.Operator globalOperator, final IndexedType indexedType);
}
