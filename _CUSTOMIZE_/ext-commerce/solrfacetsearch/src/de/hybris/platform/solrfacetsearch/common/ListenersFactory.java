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
package de.hybris.platform.solrfacetsearch.common;

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;

import java.util.List;


/**
 * Implementations of this interface are responsible for resolving and creating instances of listeners.
 */
public interface ListenersFactory
{
	/**
	 * Returns instances of listeners for a specific type.
	 *
	 * @param facetSearchConfig
	 *           - the facet search configuration
	 * @param indexedType
	 *           - the indexed type
	 * @param listenerType
	 *           - the type of the listeners
	 *
	 * @return the listeners
	 */
	<T> List<T> getListeners(FacetSearchConfig facetSearchConfig, IndexedType indexedType, Class<T> listenerType);
}
