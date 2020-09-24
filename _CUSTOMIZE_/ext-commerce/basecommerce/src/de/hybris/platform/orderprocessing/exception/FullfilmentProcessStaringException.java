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
package de.hybris.platform.orderprocessing.exception;

/**
 * 
 */
public class FullfilmentProcessStaringException extends RuntimeException
{

	private final String orderCode;
	private final String processDefinition;

	/**
	 * 
	 * @param orderCode
	 * @param processDefinition
	 * @param message
	 * @param nested
	 */
	public FullfilmentProcessStaringException(final String orderCode, final String processDefinition, final String message,
			final Throwable nested)
	{
		super("Could not start process [" + processDefinition + "] for order [" + orderCode + "] due to : " + message, nested);
		this.orderCode = orderCode;
		this.processDefinition = processDefinition;
	}

	public FullfilmentProcessStaringException(final String orderCode, final String processDefinition, final String message)
	{
		super("Could not start process [" + processDefinition + "] for order [" + orderCode + "] due to : " + message);
		this.orderCode = orderCode;
		this.processDefinition = processDefinition;
	}

	/**
	 * @return the orderCode
	 */
	public String getOrderCode()
	{
		return orderCode;
	}

	/**
	 * @return the processDefinition
	 */
	public String getProcessDefinition()
	{
		return processDefinition;
	}



}
