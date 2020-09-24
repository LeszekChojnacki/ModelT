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


public class Breadcrumb implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final String fieldName;
	private final String value;
	private final String displayValue;

	protected Breadcrumb(final String fieldName, final String value)
	{
		this(fieldName, value, null);
	}

	protected Breadcrumb(final String fieldName, final String value, final String displayValue)
	{
		this.fieldName = fieldName;
		this.value = value;
		this.displayValue = displayValue;
	}


	public String getFieldName()
	{
		return this.fieldName;
	}

	public String getValue()
	{
		return this.value;
	}

	public String getDisplayValue()
	{
		return (this.displayValue == null) ? this.value : this.displayValue;
	}

}
