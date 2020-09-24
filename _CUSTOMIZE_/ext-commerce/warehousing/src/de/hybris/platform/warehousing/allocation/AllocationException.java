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
package de.hybris.platform.warehousing.allocation;

/**
 * Exception to be thrown when something goes wrong during allocation/reallocation.
 * 
 * @see AllocationService
 */
public class AllocationException extends RuntimeException
{

	private static final long serialVersionUID = -6302207170322132302L;

	public AllocationException(final String message)
	{
		super(message);
	}

	public AllocationException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
}
