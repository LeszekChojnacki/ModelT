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

import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import org.apache.log4j.Logger;


/**
 * Interface used to lookup a display name for a specific facet value.
 *
 * Facet values retrieved from Solr are simple strings which cannot encode localised display values, therefore this
 * interface can be used to provide additional displayable data for the facet value.
 *
 * A spring bean that implements this interface can be associated with an
 * {@link de.hybris.platform.solrfacetsearch.config.IndexedProperty} and is then used to resolve a display name for a
 * specific facet value.
 */
public interface FacetDisplayNameProvider
{
	Logger LOG = Logger.getLogger("solrIndexThreadLogger");

	/**
	 * Get the display name for a facet value.
	 *
	 * This method is deprecated as is does not pass the facet which contains the facet value. Implementers should
	 * implement the
	 * {@link FacetValueDisplayNameProvider#getDisplayName(de.hybris.platform.solrfacetsearch.search.SearchQuery, de.hybris.platform.solrfacetsearch.config.IndexedProperty, String)}
	 * method instead.
	 *
	 * @param query
	 *           The search query
	 * @param name
	 *           The facet value
	 * @return The display name for the specified facet value.
	 * @deprecated Since 5.5, implement the
	 *             {@link FacetValueDisplayNameProvider#getDisplayName(de.hybris.platform.solrfacetsearch.search.SearchQuery, de.hybris.platform.solrfacetsearch.config.IndexedProperty, String)}
	 *             method instead.
	 */
	@Deprecated
	String getDisplayName(SearchQuery query, String name);
}
