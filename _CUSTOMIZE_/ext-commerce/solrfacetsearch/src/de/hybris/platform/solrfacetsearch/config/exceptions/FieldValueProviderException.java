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
package de.hybris.platform.solrfacetsearch.config.exceptions;

import de.hybris.platform.solrfacetsearch.provider.FieldValueProvider;



/**
 * Thrown by {@link FieldValueProvider} whenever the specialized field value provider cannot obtain the value
 */
public class FieldValueProviderException extends FacetConfigServiceException
{

	/**
	 * Initializes the exception
	 * 
	 * @param message
	 *           - error message
	 * @param nested
	 *           - nested exception
	 */
	public FieldValueProviderException(final String message, final Throwable nested)
	{
		super(message, nested);
	}

	/**
	 * Initializes the exception
	 * 
	 * @param message
	 *           - error message
	 */
	public FieldValueProviderException(final String message)
	{
		super(message);
	}


}
