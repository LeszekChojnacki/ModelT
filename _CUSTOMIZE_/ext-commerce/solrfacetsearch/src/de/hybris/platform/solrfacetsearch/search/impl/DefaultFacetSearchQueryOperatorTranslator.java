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
package de.hybris.platform.solrfacetsearch.search.impl;


import de.hybris.platform.solrfacetsearch.search.FacetSearchQueryOperatorTranslator;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import java.util.Map;

import org.springframework.beans.factory.annotation.Required;


public class DefaultFacetSearchQueryOperatorTranslator implements FacetSearchQueryOperatorTranslator
{
	private Map<SearchQuery.QueryOperator, String> queryOperatorStringMap;

	@Override
	public String translate(final String value, final SearchQuery.QueryOperator queryOperator)
	{
		if (!queryOperatorStringMap.containsKey(queryOperator))
		{
			throw new IllegalArgumentException(queryOperator + " has no solr query template defined");
		}

		return String.format(queryOperatorStringMap.get(queryOperator), value);
	}

	public Map<SearchQuery.QueryOperator, String> getQueryOperatorStringMap()
	{
		return queryOperatorStringMap;
	}

	@Required
	public void setQueryOperatorStringMap(final Map<SearchQuery.QueryOperator, String> queryOperatorStringMap)
	{
		this.queryOperatorStringMap = queryOperatorStringMap;
	}
}
