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
package de.hybris.platform.solrfacetsearch.solr.exceptions;

/**
 * Represents a Solr server REST call related exception.
 */
public class SolrRestException extends SolrServiceException
{
	private static final long serialVersionUID = 1L;

	private final int statusCode;

	/**
	 * Constructs a new exception with null as its detail message and status code.
	 *
	 * @param statusCode
	 *           - the status code
	 *
	 * @see Exception#Exception()
	 */
	public SolrRestException(final int statusCode)
	{
		super();
		this.statusCode = statusCode;
	}

	/**
	 * Constructs a new exception with the specified detail message and status code.
	 *
	 * @param message
	 *           - the message
	 * @param statusCode
	 *           - the status code
	 *
	 * @see Exception#Exception(String)
	 */
	public SolrRestException(final String message, final int statusCode)
	{
		super(message);
		this.statusCode = statusCode;
	}

	/**
	 * Constructs a new exception with the specified cause and status code.
	 *
	 * @param cause
	 *           - the cause
	 * @param statusCode
	 *           - the status code
	 *
	 * @see Exception#Exception(Throwable)
	 */
	public SolrRestException(final Throwable cause, final int statusCode)
	{
		super(cause);
		this.statusCode = statusCode;
	}

	/**
	 * Constructs a new exception with the specified detail message, cause and status code.
	 *
	 * @param message
	 *           - the message
	 * @param cause
	 *           - the cause
	 * @param statusCode
	 *           - the status code
	 *
	 * @see Exception#Exception(String, Throwable)
	 */

	public SolrRestException(final String message, final Throwable cause, final int statusCode)
	{
		super(message, cause);
		this.statusCode = statusCode;
	}

	public int getStatusCode()
	{
		return statusCode;
	}
}
