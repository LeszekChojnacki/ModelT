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
package de.hybris.platform.solrfacetsearch.suggester.exceptions;


/**
 *
 */
public class SolrAutoSuggestException extends Exception
{

	public SolrAutoSuggestException(final Throwable nested)
	{
		super(nested);
	}


	public SolrAutoSuggestException(final String msg, final Throwable nested)
	{
		super(msg, nested);
	}

}
