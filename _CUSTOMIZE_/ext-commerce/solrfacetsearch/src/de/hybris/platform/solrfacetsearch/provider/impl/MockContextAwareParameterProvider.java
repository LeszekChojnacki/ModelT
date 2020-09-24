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

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.provider.ContextAwareParameterProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 *
 */
public class MockContextAwareParameterProvider implements ContextAwareParameterProvider
{

	@Override
	public Map<String, Object> createParameters(final IndexConfig indexConfig, final IndexedType indexedType)
	{
		Map<String, Object> result = null;
		int counter = 0;
		for (final CatalogVersionModel catVer : indexConfig.getCatalogVersions())
		{
			if (result == null)
			{
				result = new HashMap<String, Object>();
			}
			result.put("catalogVersion" + ++counter, catVer.getVersion());
		}
		return result == null ? Collections.emptyMap() : result;
	}



}
