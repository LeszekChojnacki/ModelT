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
 * Thrown when Geo Document ResponseParser fails with error.
 */
public class GeoDocumentParsingException extends GeoServiceWrapperException
{

	public GeoDocumentParsingException(final String message, final Throwable nested)
	{
		super(message, nested);
	}

}
