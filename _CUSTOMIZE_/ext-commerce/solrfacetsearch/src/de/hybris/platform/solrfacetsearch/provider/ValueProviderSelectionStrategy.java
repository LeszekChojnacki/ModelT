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

import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;

import java.util.Collection;
import java.util.Map;


/**
 * Implementations of this strategy allow to select the value provider for specific indexed properties.
 */
public interface ValueProviderSelectionStrategy
{
	/**
	 * Returns the value provider instance for a specific value provider id.
	 *
	 * @param valueProviderId
	 *           - the value provider id
	 *
	 * @return the value provider instance
	 *
	 */
	Object getValueProvider(String valueProviderId);

	/**
	 * Resolves the value provider id for an indexed property.
	 *
	 * @param indexedType
	 *           - the indexed type
	 * @param indexedProperty
	 *           - the indexed property
	 *
	 * @return the value provider id
	 */
	String resolveValueProvider(IndexedType indexedType, IndexedProperty indexedProperty);

	/**
	 * Resolves the value provider ids for multiple indexed properties. It groups all the indexed properties that have
	 * the same value provider id.
	 *
	 * @param indexedType
	 *           - the indexed type
	 * @param indexedProperties
	 *           - the indexed properties
	 *
	 * @return a map with the value provider id as key and the indexed properties as value
	 */
	Map<String, Collection<IndexedProperty>> resolveValueProviders(IndexedType indexedType,
			Collection<IndexedProperty> indexedProperties);
}
