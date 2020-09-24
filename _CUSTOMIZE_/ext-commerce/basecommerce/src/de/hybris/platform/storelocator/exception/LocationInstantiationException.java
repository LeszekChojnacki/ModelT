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
package de.hybris.platform.storelocator.exception;

import de.hybris.platform.storelocator.location.Location;


/**
 * Exception thrown while instantiating of the {@link Location} implementations.
 */
public class LocationInstantiationException extends RuntimeException
{

	/**
	 * 
	 */
	public LocationInstantiationException()
	{
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public LocationInstantiationException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public LocationInstantiationException(final String message)
	{
		super(message);
	}

	/**
	 * @param cause
	 */
	public LocationInstantiationException(final Throwable cause)
	{
		super(cause);
	}

}
