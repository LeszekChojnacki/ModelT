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

import de.hybris.platform.solrfacetsearch.config.impl.DefaultFacetSearchConfigService;



/**
 * Thrown by {@link DefaultFacetSearchConfigService} whenever value range specified in the indexed property definition
 * cannot be found
 */
public class FacetConfigUnknownValueRangeException extends FacetConfigServiceException
{

	/**
	 * Initializes the exception
	 * 
	 * @param configName
	 *           - SOLR configuration item name
	 * @param nested
	 *           - nested exception
	 */
	public FacetConfigUnknownValueRangeException(final String configName, final Throwable nested)
	{
		super(configName, nested);
	}

	/**
	 * Initializes the exception
	 * 
	 * @param configName
	 *           - SOLR configuration item name
	 * @param message
	 *           - error message
	 * @param nested
	 *           - nested exception
	 */
	public FacetConfigUnknownValueRangeException(final String configName, final String message, final Throwable nested)
	{
		super(configName, message, nested);
	}

	/**
	 * Initializes the exception
	 * 
	 * @param configName
	 *           - SOLR configuration item name
	 * @param message
	 *           - error message
	 */
	public FacetConfigUnknownValueRangeException(final String configName, final String message)
	{
		super(configName, message);
	}

}
