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
package de.hybris.platform.solrfacetsearch.provider;

import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;

import java.util.Map;


/**
 * Generates it's own set of flexible search query parameters that can be injected to the solr indexing queries
 * (full-index, update, delete). The parameters are index-config aware
 */
public interface ContextAwareParameterProvider
{
	/**
	 * Create provider's parameter in the form of Map<String, Object> representing parameter-value map.
	 * 
	 * @param indexConfig -
	 *           context index configuration
	 * 
	 * @param indexedType
	 *           context indexed type
	 * @return Map<String, Object>
	 */
	Map<String, Object> createParameters(IndexConfig indexConfig, IndexedType indexedType);
}
