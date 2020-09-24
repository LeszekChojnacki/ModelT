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

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * The Class represents a Facet Query Filed Constraint. Each field can have one or more values, but only one relation,
 * namely either "AND" or "OR". Example: {categories:HW1000, AND}, or {manufacture:[intel, sony], OR}.
 */
public class QueryField implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final String ALL_FIELD = "fulltext";

	private String field;
	private Set<String> values;
	private SearchQuery.Operator operator;
	private SearchQuery.QueryOperator queryOperator;

	public QueryField(final String field, final String... values)
	{
		this(field, SearchQuery.Operator.AND, SearchQuery.QueryOperator.EQUAL_TO, values);
	}

	public QueryField(final String field, final SearchQuery.Operator operator, final String... values)
	{
		this(field, operator, SearchQuery.QueryOperator.EQUAL_TO, values);
	}

	public QueryField(final String field, final SearchQuery.Operator operator, final Set<String> values)
	{
		this(field, operator, SearchQuery.QueryOperator.EQUAL_TO, values);
	}

	public QueryField(final String field, final SearchQuery.Operator operator, final SearchQuery.QueryOperator queryOperator,
			final String... values)
	{
		this.field = field;
		this.operator = operator;
		this.queryOperator = queryOperator;
		this.values = new LinkedHashSet<>(Arrays.asList(values));
	}

	public QueryField(final String field, final SearchQuery.Operator operator, final SearchQuery.QueryOperator queryOperator,
			final Set<String> values)
	{
		this.field = field;
		this.operator = operator;
		this.queryOperator = queryOperator;
		this.values = values;
	}

	public String getField()
	{
		return field;
	}

	public void setField(final String field)
	{
		this.field = field;
	}

	public Set<String> getValues()
	{
		return values;
	}

	public void setValues(final Set<String> values)
	{
		this.values = new LinkedHashSet<String>(values);
	}

	public SearchQuery.Operator getOperator()
	{
		return operator;
	}

	public void setOperator(final SearchQuery.Operator operator)
	{
		this.operator = operator;
	}

	public SearchQuery.QueryOperator getQueryOperator()
	{
		return queryOperator;
	}

	public void setQueryOperator(final SearchQuery.QueryOperator queryOperator)
	{
		this.queryOperator = queryOperator;
	}
}
