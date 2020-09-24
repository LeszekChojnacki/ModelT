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
package de.hybris.platform.ordercancel;

/**
 * Default implementation for Cancel Order Service denial reasons.
 */
public class DefaultOrderCancelDenialReason implements OrderCancelDenialReason
{
	private int code;
	private String description;

	/**
	 * @param code
	 * @param description
	 */
	public DefaultOrderCancelDenialReason(final int code, final String description)
	{
		super();
		this.code = code;
		this.description = description;
	}

	public DefaultOrderCancelDenialReason()
	{
		super();
	}

	/**
	 * @return the code
	 */
	public int getCode()
	{
		return code;
	}

	/**
	 * @param code
	 *           the code to set
	 */
	public void setCode(final int code)
	{
		this.code = code;
	}

	/**
	 * @return the description
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * @param description
	 *           the description to set
	 */
	public void setDescription(final String description)
	{
		this.description = description;
	}
}
