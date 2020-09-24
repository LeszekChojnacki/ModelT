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

import de.hybris.platform.storelocator.GPS;



/**
 * Exception used when argument validation is not succeed due to missing or invalid arguments. Used while in {@link GPS}
 * creation and different metrics conversions.
 */
public class GeoLocatorException extends RuntimeException
{

	/**
	 * @param message
	 * @param cause
	 */
	public GeoLocatorException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public GeoLocatorException(final String message)
	{
		super(message);
	}

	/**
	 * @param cause
	 */
	public GeoLocatorException(final Throwable cause)
	{
		super(cause);
	}

	@Override
	public String toString()
	{
		return super.toString() + "\n" + getStackTrace();

	}

}
