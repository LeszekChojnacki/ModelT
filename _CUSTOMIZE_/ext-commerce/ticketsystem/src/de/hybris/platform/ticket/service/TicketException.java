/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.ticket.service;

/**
 * 
 */
public class TicketException extends Exception
{
	public TicketException()
	{
		super();
	}

	public TicketException(final String message)
	{
		super(message);
	}

	public TicketException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public TicketException(final Throwable cause)
	{
		super(cause);
	}
}
