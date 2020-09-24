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

import org.apache.solr.client.solrj.SolrQuery;


public interface FacetSearchQueryOperatorTranslator
{
	/**
	 * Translate {@link SolrQuery} into solr query expression using the supported operators.
	 *
	 * @param value
	 * 		- Field value
	 * @param queryOperator
	 * 		- Query operator
	 *
	 * @return String query expression
	 *
	 * @throws IllegalArgumentException
	 * 		when operator is not supported
	 */
	String translate(final String value, final SearchQuery.QueryOperator queryOperator);
}
