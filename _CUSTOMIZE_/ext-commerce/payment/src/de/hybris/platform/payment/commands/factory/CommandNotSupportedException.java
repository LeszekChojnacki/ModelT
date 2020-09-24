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
package de.hybris.platform.payment.commands.factory;

/**
 * exception indicate that command is not supported
 */
public class CommandNotSupportedException extends Exception
{
	public CommandNotSupportedException()
	{
		super();
	}

	/**
	 * @param message
	 * @param exception
	 */
	public CommandNotSupportedException(final String message, final Throwable exception)
	{
		super(message, exception);
	}

	/**
	 * @param message
	 */
	public CommandNotSupportedException(final String message)
	{
		super(message);
	}

	/**
	 * @param exception
	 */
	public CommandNotSupportedException(final Throwable exception)
	{
		super(exception);
	}
}
