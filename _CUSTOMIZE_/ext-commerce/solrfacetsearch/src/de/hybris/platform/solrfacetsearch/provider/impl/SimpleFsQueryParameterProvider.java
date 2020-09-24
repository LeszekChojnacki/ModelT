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
package de.hybris.platform.solrfacetsearch.provider.impl;

import de.hybris.platform.solrfacetsearch.provider.ParameterProvider;

import java.util.HashMap;
import java.util.Map;


public class SimpleFsQueryParameterProvider implements ParameterProvider
{

	@Override
	public Map<String, Object> createParameters()
	{
		final Map<String, Object> parameters = new HashMap<String, Object>();
		//test
		parameters.put("price", Double.valueOf(999.99));
		return parameters;
	}

}
