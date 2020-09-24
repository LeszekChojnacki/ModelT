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

/**
 * Exception thrown if any error occurs while creating google KML document.
 */
public class KmlDocumentException extends RuntimeException
{

	/**
	 * @param message
	 * @param cause
	 */
	public KmlDocumentException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public KmlDocumentException(final String message)
	{
		super(message);
	}

	/**
	 * @param cause
	 */
	public KmlDocumentException(final Throwable cause)
	{
		super(cause);
	}


}
