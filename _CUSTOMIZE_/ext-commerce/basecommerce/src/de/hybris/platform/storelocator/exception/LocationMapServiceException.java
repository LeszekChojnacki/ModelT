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

import de.hybris.platform.storelocator.location.LocationMapService;


/**
 * High-level exception for {@link LocationMapService}.
 */
public class LocationMapServiceException extends RuntimeException
{
	/**
	 * 
	 */
	public LocationMapServiceException()
	{
		super();
	}

	/**
    * 
    */
	public LocationMapServiceException(final String message)
	{
		super(message);
	}

	/**
    * 
    */
	public LocationMapServiceException(final Throwable cause)
	{
		super(cause);
	}

	/**
    * 
    */
	public LocationMapServiceException(final String message, final Throwable cause)
	{
		super(message, cause);
	}
}
