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

/**
 * Thrown whenever SOLR configuration contains mode definition that is not supported
 */
public class UnsuportedServerModeException extends FacetConfigServiceException
{

	/**
	 * Initializes the exception
	 * 
	 * @param configName
	 *           - name of search configuration
	 * @param message
	 *           - error message
	 * @param nested
	 *           - nested exception
	 */
	public UnsuportedServerModeException(final String configName, final String message, final Throwable nested)
	{
		super(configName, message, nested);
	}

	/**
	 * Initializes the exception
	 * 
	 * @param configName
	 *           - name of search configuration
	 * @param message
	 *           - error message
	 */
	public UnsuportedServerModeException(final String configName, final String message)
	{
		super(configName, message);
	}

	/**
	 * Initializes the exception
	 * 
	 * @param message
	 *           - error message
	 * @param nested
	 *           - nested exception
	 */
	public UnsuportedServerModeException(final String message, final Throwable nested)
	{
		super(message, nested);
	}

}
