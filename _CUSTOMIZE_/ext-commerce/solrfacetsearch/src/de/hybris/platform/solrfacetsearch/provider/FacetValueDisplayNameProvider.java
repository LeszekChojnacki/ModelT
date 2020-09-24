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
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

/**
 * Interface used to lookup a display name for a specific facet value.
 *
 * Facet values retrieved from Solr are simple strings which cannot encode localised display values, therefore this interface can
 * be used to provide additional displayable data for the facet value.
 *
 * A spring bean that implements the {@link FacetDisplayNameProvider} interface can be associated with an
 * {@link de.hybris.platform.solrfacetsearch.config.IndexedProperty} and is then used to resolve a display name for a specific facet value.
 * The {@link FacetDisplayNameProvider} interface is deprecated in favour of this interface, however implementers of this interface
 * must also implement the {@link FacetDisplayNameProvider} interface otherwise they cannot be associated with the {@link de.hybris.platform.solrfacetsearch.config.IndexedProperty}.
 * Where a bean implements both this interface and the {@link FacetDisplayNameProvider} interface then this interface will be called
 * in preference.
 *
 * Implementers should typically extend the {@link de.hybris.platform.solrfacetsearch.provider.impl.AbstractFacetValueDisplayNameProvider} class.
 */
public interface FacetValueDisplayNameProvider
{
	/**
	 * Get the display name for a facet value.
	 *
	 * @param query      The search query
	 * @param property   The indexed property (i.e. the facet)
	 * @param facetValue The facet value
	 * @return The display name for the specified facet value.
	 */
	String getDisplayName(SearchQuery query, IndexedProperty property, String facetValue);
}
