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
package de.hybris.platform.solrfacetsearch.search;

import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;


/**
 * Implementations of this interface should provide the CurrencyModel based on the FacetSearchConfig and/or IndexedType
 */
public interface SearchQueryCurrencyResolver
{
	/**
	 * Method to resolve the currency
	 *
	 * @param facetSearchConfig
	 * 		- the facet search configuration
	 * @param indexedType
	 * 		- the indexed type
	 *
	 * @return the resolved currency
	 */
	CurrencyModel resolveCurrency(FacetSearchConfig facetSearchConfig, IndexedType indexedType);
}
