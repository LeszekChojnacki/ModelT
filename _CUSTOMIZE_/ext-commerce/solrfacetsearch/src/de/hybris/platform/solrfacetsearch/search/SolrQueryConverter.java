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


/**
 * This is the API of the converter that converts hybris {@link SearchQuery} into {@link SolrQuery}, which can be used
 * to query solr instance. It is possible to configure a conversion post-processors {@link SolrQueryPostProcessor} in
 * spring context.
 * <p>
 * You can specify a join operator (AND/OR) for the different facet fields. To do it, define <code>fieldOperator</code>
 * property in spring configuration.
 * <p>
 *
 * @deprecated Since 5.7 there is a new way of converting the query. This will only be used if the the legacy mode is
 *             enabled in the search configuration.
 */
@Deprecated
public interface SolrQueryConverter
{

	/**
	 * Converts {@link SearchQuery} instance into valid {@link SolrQuery}.
	 */
	SolrQuery convertSolrQuery(SearchQuery searchQuery) throws FacetSearchException;
}
