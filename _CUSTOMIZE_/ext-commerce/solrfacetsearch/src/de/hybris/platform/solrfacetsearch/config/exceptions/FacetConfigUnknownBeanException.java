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
 * Thrown by {@link DefaultFacetSearchConfigService} whenever either identity provider of an indexed type or value
 * provider of an indexed property cannot be resolved
 */
public class FacetConfigUnknownBeanException extends FacetConfigServiceException
{
	/**
	 * @param message
	 * @param nested
	 */
	public FacetConfigUnknownBeanException(final String message, final Throwable nested)
	{
		super(message, nested);
	}

	/**
	 * Initializes exception
	 * 
	 * @param configName
	 *           - name of SOLR configuration item
	 * @param message
	 *           - parameterized error message
	 * @param nested
	 *           - nested exception
	 * @param params
	 *           - elements of parameterized error message
	 */
	public FacetConfigUnknownBeanException(final String configName, final String message, final Throwable nested,
			final Object... params)
	{
		super(configName, message, nested);

	}




}
