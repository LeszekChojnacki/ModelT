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
package de.hybris.platform.adaptivesearch;

/**
 * Represents an adaptive search related runtime exception.
 */
public class AsRuntimeException extends RuntimeException
{
	/**
	 * Constructs a new exception with null as its detail message.
	 *
	 * @see RuntimeException#RuntimeException()
	 */
	public AsRuntimeException()
	{
		super();
	}

	/**
	 * Constructs a new exception with the specified detail message.
	 *
	 * @param message
	 *           - the message
	 *
	 * @see RuntimeException#RuntimeException(String)
	 */
	public AsRuntimeException(final String message)
	{
		super(message);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 *
	 * @param message
	 *           - the message
	 * @param cause
	 *           - the cause
	 *
	 * @see RuntimeException#RuntimeException(String, Throwable)
	 */
	public AsRuntimeException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Constructs a new exception with the specified cause.
	 *
	 * @param cause
	 *           - the cause
	 *
	 * @see RuntimeException#RuntimeException(Throwable)
	 */
	public AsRuntimeException(final Throwable cause)
	{
		super(cause);
	}
}
