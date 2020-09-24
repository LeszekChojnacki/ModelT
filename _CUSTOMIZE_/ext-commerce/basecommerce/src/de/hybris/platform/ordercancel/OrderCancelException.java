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

public class OrderCancelException extends Exception
{
	private final String orderCode;

	/**
	 * @param orderCode
	 */
	public OrderCancelException(final String orderCode)
	{
		super();
		this.orderCode = orderCode;
	}

	/**
	 * 
	 * @param orderCode
	 * @param message
	 * @param nested
	 */
	public OrderCancelException(final String orderCode, final String message, final Throwable nested)
	{
		super("Order Cancel(" + orderCode + ") :" + message, nested);
		this.orderCode = orderCode;
	}

	/**
	 * 
	 * @param orderCode
	 * @param message
	 */
	public OrderCancelException(final String orderCode, final String message)
	{
		super("Order Cancel(" + orderCode + ") :" + message);
		this.orderCode = orderCode;
	}

	/**
	 * 
	 * @param orderCode
	 * @param nested
	 */
	public OrderCancelException(final String orderCode, final Throwable nested)
	{
		super("orderCode: " + orderCode, nested);
		this.orderCode = orderCode;
	}

	public String getOrderCode()
	{
		return this.orderCode;
	}
}
