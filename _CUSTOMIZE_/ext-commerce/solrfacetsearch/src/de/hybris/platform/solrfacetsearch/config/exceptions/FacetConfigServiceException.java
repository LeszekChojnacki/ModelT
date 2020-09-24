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

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfigService;



/**
 * Main exception of the SOLR configuration service.
 * All exceptions thrown by {@link FacetSearchConfigService} extend from this exception.
 */
public class FacetConfigServiceException extends Exception
{


	private final String configName;
	private final String message;


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
	public FacetConfigServiceException(final String configName, final String message, final Throwable nested)
	{
		super(message, nested);
		this.message = "Solr configuration:" + configName + " ," + message;
		this.configName = configName;
	}

	/**
	 * Initializes the exception
	 * 
	 * @param configName
	 *           - name of search configuration
	 * @param message
	 *           - error message
	 */
	public FacetConfigServiceException(final String configName, final String message)
	{
		super(message);
		this.configName = configName;
		this.message = "Solr configuration:" + configName + " ," + message;
	}

	/**
	 * Initializes the exception
	 * 
	 * @param message
	 *           - error message
	 * @param nested
	 *           - nested exception
	 */
	public FacetConfigServiceException(final String message, final Throwable nested)
	{
		super(message, nested);
		configName = "";
		this.message = message;
	}

	/**
	 * Initializes the exception
	 *
	 * @param message
	 *           - error message
	 */
	public FacetConfigServiceException(final String message)
	{
		super(message);
		configName = "";
		this.message = message;

	}

	/**
	 * Get configuration name
	 * 
	 * @return the configName
	 */
	public String getConfigName()
	{
		return this.configName;
	}

	/**
	 * Get error message
	 * 
	 * @return the message
	 */
	@Override
	public String getMessage()
	{
		return this.message;
	}


}
