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
package de.hybris.platform.payment;


/**
 * AdapterException is thrown when payment related operations fail.
 */
public class AdapterException extends RuntimeException
{
	private Exception baseException; // NOSONAR

	public AdapterException()
	{
		super();
	}

	public AdapterException(final String message, final Throwable exception)
	{
		super(message, exception);
	}

	public AdapterException(final String message)
	{
		super(message);
	}

	public AdapterException(final Throwable exception)
	{
		super(exception);

	}

	public void setBaseException(final Exception baseException)
	{
		this.baseException = baseException;
	}

	public Exception getBaseException()
	{
		return baseException;
	}
}
