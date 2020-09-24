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
package de.hybris.platform.returns;



public class OrderReturnRecordsHandlerException extends OrderReturnException
{
	/**
	 * 
	 * @param orderCode
	 * @param message
	 * @param nested
	 */
	public OrderReturnRecordsHandlerException(final String orderCode, final String message, final Throwable nested)
	{
		super("Order Return(" + orderCode + ") :" + message, nested);
	}

	/**
	 * 
	 * @param orderCode
	 * @param message
	 */
	public OrderReturnRecordsHandlerException(final String orderCode, final String message)
	{
		super(orderCode, "Order Return(" + orderCode + ") :" + message);
	}

	/**
	 * 
	 * @param orderCode
	 * @param nested
	 */
	public OrderReturnRecordsHandlerException(final String orderCode, final Throwable nested)
	{
		super("orderCode: " + orderCode, nested);
	}
}
