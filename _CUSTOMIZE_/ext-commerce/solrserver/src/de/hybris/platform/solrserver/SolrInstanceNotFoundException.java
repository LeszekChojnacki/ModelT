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
package de.hybris.platform.solrserver;

/**
 * Exception thrown when a Solr instance is not found.
 *
 * @deprecated Since 18.08, no longer used.
 */
@Deprecated
public class SolrInstanceNotFoundException extends SolrServerException
{
	/**
	 * Constructs a new exception with null as its detail message.
	 *
	 * @see Exception#Exception()
	 */
	public SolrInstanceNotFoundException()
	{
		super();
	}

	/**
	 * Constructs a new exception with the specified detail message.
	 *
	 * @param message
	 *           - the message
	 *
	 * @see Exception#Exception(String)
	 */
	public SolrInstanceNotFoundException(final String message)
	{
		super(message);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 *
	 * @param message
	 *           - the message
	 * @param cause
	 *           - the cause
	 *
	 * @see Exception#Exception(String, Throwable)
	 */
	public SolrInstanceNotFoundException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Constructs a new exception with the specified cause.
	 *
	 * @param cause
	 *           - the cause
	 *
	 * @see Exception#Exception(Throwable)
	 */
	public SolrInstanceNotFoundException(final Throwable cause)
	{
		super(cause);
	}
}
