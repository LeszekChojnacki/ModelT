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

import de.hybris.platform.ordercancel.OrderCancelException;


/**
 * 
 */
public class OrderCancelRecordsHandlerException extends OrderCancelException
{
	/**
	 * 
	 * @param orderCode
	 * @param message
	 * @param nested
	 */
	public OrderCancelRecordsHandlerException(final String orderCode, final String message, final Throwable nested)
	{
		super("Order Cancel(" + orderCode + ") :" + message, nested);
	}

	/**
	 * 
	 * @param orderCode
	 * @param message
	 */
	public OrderCancelRecordsHandlerException(final String orderCode, final String message)
	{
		super(orderCode, "Order Cancel(" + orderCode + ") :" + message);
	}

	/**
	 * 
	 * @param orderCode
	 * @param nested
	 */
	public OrderCancelRecordsHandlerException(final String orderCode, final Throwable nested)
	{
		super("orderCode: " + orderCode, nested);
	}
}
