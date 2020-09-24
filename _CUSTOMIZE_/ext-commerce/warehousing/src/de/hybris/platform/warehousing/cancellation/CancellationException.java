/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousing.cancellation;

/**
 * Exception to be thrown when something goes wrong during cancellation.
 * 
 * @see de.hybris.platform.ordercancel.OrderCancelService
 * @see ConsignmentCancellationService
 */
public class CancellationException extends RuntimeException
{

	private static final long serialVersionUID = 755896214498434805L;

	public CancellationException(final String message)
	{
		super(message);
	}

	public CancellationException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
}
