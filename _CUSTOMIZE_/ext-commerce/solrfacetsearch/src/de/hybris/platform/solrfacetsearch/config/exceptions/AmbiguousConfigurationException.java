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
 * Thrown by {@link DefaultFacetSearchConfigService} whenever the user specified the configuration in both the xml file
 * and the related configuration items. The valid configuration cannot be resolved.
 */
public class AmbiguousConfigurationException extends FacetConfigServiceException
{

	/**
	 * Initialize the exception
	 * 
	 * @param configName
	 *           - name of configuration
	 * @param message
	 *           - error message
	 */
	public AmbiguousConfigurationException(final String configName, final String message)
	{
		super(configName, message);
	}

}
