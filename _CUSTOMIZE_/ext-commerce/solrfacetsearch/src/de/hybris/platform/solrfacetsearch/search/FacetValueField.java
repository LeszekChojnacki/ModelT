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


public class FacetValueField implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String field;
	private Set<String> values;

	public FacetValueField(final String field, final String... values)
	{
		this.field = field;
		this.values = new LinkedHashSet<String>(Arrays.asList(values));
	}

	public FacetValueField(final String field, final Set<String> values)
	{
		this.field = field;
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
		this.values = values;
	}
}
