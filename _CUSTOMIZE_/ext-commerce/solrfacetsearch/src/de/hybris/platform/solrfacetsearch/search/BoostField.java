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


public class BoostField implements Serializable
{
	private static final long serialVersionUID = 1L;

	public enum BoostType
	{
		MULTIPLICATIVE, ADDITIVE
	}

	private String field;
	private SearchQuery.QueryOperator queryOperator;
	private Object value;
	private Float boostValue;
	private BoostType boostType;

	public BoostField(final String field, final SearchQuery.QueryOperator queryOperator, final Object value,
			final Float boostValue, final BoostType boostType)
	{
		this.field = field;
		this.queryOperator = queryOperator;
		this.value = value;
		this.boostValue = boostValue;
		this.boostType = boostType;
	}

	public String getField()
	{
		return field;
	}

	public void setField(final String field)
	{
		this.field = field;
	}

	public SearchQuery.QueryOperator getQueryOperator()
	{
		return queryOperator;
	}

	public void setQueryOperator(final SearchQuery.QueryOperator queryOperator)
	{
		this.queryOperator = queryOperator;
	}

	public Object getValue()
	{
		return value;
	}

	public void setValue(final Object value)
	{
		this.value = value;
	}

	public Float getBoostValue()
	{
		return boostValue;
	}

	public void setBoostValue(final Float boostValue)
	{
		this.boostValue = boostValue;
	}

	public BoostType getBoostType()
	{
		return boostType;
	}

	public void setBoostType(final BoostType boostType)
	{
		this.boostType = boostType;
	}
}
