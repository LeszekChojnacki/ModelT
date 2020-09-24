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
package de.hybris.platform.ordercancel.exceptions;

/**
 * 
 */
public class OrderCancelDaoException extends RuntimeException
{
	private final String orderCode;

	/**
	 * Exception constructor
	 * 
	 * @param orderCode
	 * @param message
	 * @param cause
	 */
	public OrderCancelDaoException(final String orderCode, final String message, final Throwable cause)
	{
		super(message, cause);
		this.orderCode = orderCode;
	}

	/**
	 * Exception constructor
	 * 
	 * @param orderCode
	 * @param message
	 */
	public OrderCancelDaoException(final String orderCode, final String message)
	{
		super(message);
		this.orderCode = orderCode;
	}

	/**
	 * @return the orderCode
	 */
	public String getOrderCode()
	{
		return orderCode;
	}
}
