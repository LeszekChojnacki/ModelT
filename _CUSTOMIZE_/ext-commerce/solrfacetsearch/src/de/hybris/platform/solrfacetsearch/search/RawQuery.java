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

import de.hybris.platform.solrfacetsearch.search.SearchQuery.Operator;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.io.Serializable;
import java.util.Objects;


/**
 * Class representing a raw query using lucene syntax.
 */
public class RawQuery implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String field;
	private String query;
	private Operator operator = Operator.OR;

	public RawQuery(final String query)
	{

		this.query = query;
	}

	/**
	 * @deprecated Since 5.7, see {@link SearchQuery#setDefaultOperator(Operator)}.
	 */
	@Deprecated
	public RawQuery(final String query, final Operator operator)
	{
		this.query = query;
		this.operator = operator;
	}

	/**
	 * @deprecated Since 5.7, should only be used for compatibility purposes.
	 */
	@Deprecated
	public RawQuery(final String field, final String query, final Operator operator)
	{
		this.field = field;
		this.query = query;
		this.operator = operator;
	}

	/**
	 * @deprecated Since 5.7, should only be used for compatibility purposes.
	 */
	@Deprecated
	public String getField()
	{
		return field;
	}

	/**
	 * @deprecated Since 5.7, should only be used for compatibility purposes.
	 */
	@Deprecated
	public void setField(final String field)
	{
		this.field = field;
	}

	public String getQuery()
	{
		return query;
	}

	public void setQuery(final String query)
	{
		this.query = query;
	}

	/**
	 * @deprecated Since 5.7, see {@link SearchQuery#getDefaultOperator()}.
	 */
	@Deprecated
	public SearchQuery.Operator getOperator()
	{
		return operator;
	}

	/**
	 * @deprecated Since 5.7, see {@link SearchQuery#getDefaultOperator()}.
	 */
	@Deprecated
	public void setOperator(final SearchQuery.Operator operator)
	{
		this.operator = operator;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}

		if (obj == null || this.getClass() != obj.getClass())
		{
			return false;
		}

		final RawQuery that = (RawQuery) obj;
		return new EqualsBuilder()
				.append(this.field, that.field)
				.append(this.query, that.query)
				.append(this.operator, that.operator)
				.isEquals();
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(field, query, operator);
	}
}
