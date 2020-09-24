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

import de.hybris.platform.storelocator.map.Map;
import de.hybris.platform.storelocator.map.impl.DefaultMap;



/**
 * Exception thrown during creation of the {@link DefaultMap} which is a default implementation of the Google MAPs
 * oriented {@link Map} interface.
 */
public class GoogleMapException extends RuntimeException
{

	/**
	 * 
	 */
	public GoogleMapException()
	{
		super();
	}

	/**
	 * @param message
	 * @param nested
	 *           exception
	 */
	public GoogleMapException(final String message, final Throwable nested)
	{
		super(message, nested);
	}

	/**
	 * @param message
	 */
	public GoogleMapException(final String message)
	{
		super(message);
	}

	/**
	 * @param nested
	 */
	public GoogleMapException(final Throwable nested)
	{
		super(nested);
	}

}
