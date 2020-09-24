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
package com.hybris.backoffice.excel.data;

import java.io.Serializable;


/**
 * Represents simple impex value which consists of value and header attribute.
 */
public class ImpexValue implements Serializable
{

	/**
	 * Impex value.
	 */
	private final transient Object value;

	/**
	 * Impex header {@link ImpexHeaderValue}.
	 */
	private final ImpexHeaderValue headerValue;

	public ImpexValue(final Object value, final ImpexHeaderValue headerValue)
	{
		this.value = value;
		this.headerValue = headerValue;
	}

	public Object getValue()
	{
		return value;
	}

	public ImpexHeaderValue getHeaderValue()
	{
		return headerValue;
	}
}
