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
package de.hybris.platform.solrfacetsearch.config;

import de.hybris.platform.solrfacetsearch.search.FacetValue;

import java.util.Comparator;


/**
 *
 */
/**
 * Interface for loading a custom comparator for a facet
 */
public interface FacetSortProvider
{
	/**
	 * Get the comparator for the type and property
	 * 
	 * @param indexedType
	 *           the type
	 * @param indexedProperty
	 *           the property
	 * @return the comparator
	 */
	Comparator<FacetValue> getComparatorForTypeAndProperty(IndexedType indexedType, IndexedProperty indexedProperty);
}
