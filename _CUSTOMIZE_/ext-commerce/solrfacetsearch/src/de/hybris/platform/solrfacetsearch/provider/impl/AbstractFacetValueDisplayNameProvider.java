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

import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.provider.FacetDisplayNameProvider;
import de.hybris.platform.solrfacetsearch.provider.FacetValueDisplayNameProvider;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

/**
 * Abstract implementation of the {@link FacetValueDisplayNameProvider} interface.
 * Implementers of the {@link FacetValueDisplayNameProvider} interface should typically extend this class.
 *
 * This class also implements the deprecated {@link FacetDisplayNameProvider} simply as a marker interface
 * as this is the interface that is bound to the {@link IndexedProperty}.
 */
public abstract class AbstractFacetValueDisplayNameProvider implements FacetValueDisplayNameProvider, FacetDisplayNameProvider
{
	@Override
	public final String getDisplayName(final SearchQuery query, final String name)
	{
		throw new IllegalStateException("Do not call the FacetDisplayNameProvider#getDisplayName method call the FacetValueDisplayNameProvider#getDisplayName method instead.");
	}

	/**
	 * Get the display name for a facet value.
	 *
	 * @param query      The search query
	 * @param property   The indexed property (i.e. the facet)
	 * @param facetValue The facet value
	 * @return The display name for the specified facet value.
	 */
	@Override
	public abstract String getDisplayName(final SearchQuery query, final IndexedProperty property, final String facetValue);
}
