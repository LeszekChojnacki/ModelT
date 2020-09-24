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
package de.hybris.platform.solrfacetsearch.search;

public class FacetSearchException extends Exception
{

	public FacetSearchException()
	{
		super();
	}

	public FacetSearchException(final String message)
	{
		super(message);
	}

	public FacetSearchException(final Throwable cause)
	{
		super(cause);
	}

	public FacetSearchException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

}
