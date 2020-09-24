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
package com.hybris.backoffice.solrsearch.dataaccess;

import de.hybris.platform.solrfacetsearch.search.QueryField;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import java.util.Locale;
import java.util.Set;


public class BackofficeQueryField extends QueryField
{
	private final Locale locale;

	public BackofficeQueryField(final String field, final Locale locale, final String... values)
	{
		super(field, values);
		this.locale = locale;
	}

	public BackofficeQueryField(final String field, final SearchQuery.Operator operator, final Locale locale,
			final String... values)
	{
		super(field, operator, values);
		this.locale = locale;
	}

	public BackofficeQueryField(final String field, final SearchQuery.Operator operator, final Locale locale,
			final Set<String> values)
	{
		super(field, operator, values);
		this.locale = locale;
	}

	public BackofficeQueryField(final String field, final SearchQuery.Operator operator,
			final SearchQuery.QueryOperator queryOperator, final Locale locale, final String... values)
	{
		super(field, operator, queryOperator, values);
		this.locale = locale;
	}

	public BackofficeQueryField(final String field, final SearchQuery.Operator operator,
			final SearchQuery.QueryOperator queryOperator, final Locale locale, final Set<String> values)
	{
		super(field, operator, queryOperator, values);
		this.locale = locale;
	}

	public Locale getLocale()
	{
		return locale;
	}
}
