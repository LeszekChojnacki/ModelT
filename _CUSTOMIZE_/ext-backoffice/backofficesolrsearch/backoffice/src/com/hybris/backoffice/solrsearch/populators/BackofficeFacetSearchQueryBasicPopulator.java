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
package com.hybris.backoffice.solrsearch.populators;

import de.hybris.platform.solrfacetsearch.search.QueryField;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.impl.populators.FacetSearchQueryBasicPopulator;

import org.apache.commons.lang.StringUtils;

import com.hybris.backoffice.solrsearch.dataaccess.BackofficeQueryField;
import com.hybris.backoffice.solrsearch.dataaccess.BackofficeSearchQuery;


public class BackofficeFacetSearchQueryBasicPopulator extends FacetSearchQueryBasicPopulator
{
	private FieldNamePostProcessor fieldNamePostProcessor;

	@Override
	protected String convertQueryField(final SearchQuery searchQuery, final QueryField queryField)
	{
		if (searchQuery instanceof BackofficeSearchQuery && queryField instanceof BackofficeQueryField)
		{
			final BackofficeQueryField backofficeQueryField = (BackofficeQueryField) queryField;
			final String convertedQuery = super.convertQueryField(searchQuery, queryField);
			return getFieldNamePostProcessor().process(searchQuery, backofficeQueryField.getLocale(), convertedQuery);
		}

		return StringUtils.EMPTY;
	}

	public FieldNamePostProcessor getFieldNamePostProcessor()
	{
		return fieldNamePostProcessor;
	}

	public void setFieldNamePostProcessor(final FieldNamePostProcessor fieldNamePostProcessor)
	{
		this.fieldNamePostProcessor = fieldNamePostProcessor;
	}
}
