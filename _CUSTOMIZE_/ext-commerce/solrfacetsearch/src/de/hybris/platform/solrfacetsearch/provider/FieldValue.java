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
package de.hybris.platform.solrfacetsearch.provider;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 *
 * 
 */
public class FieldValue
{

	private final String fieldName;
	private final Object value;

	/**
	 * @param fieldName
	 * @param value
	 */
	public FieldValue(final String fieldName, final Object value)
	{
		this.fieldName = fieldName;
		this.value = value;
	}


	/**
	 * @return the fieldName
	 */
	public String getFieldName()
	{
		return fieldName;
	}

	/**
	 * @return the value
	 */
	public Object getValue()
	{
		return value;
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this).append("field", fieldName).append("value", value).toString();
	}

}
