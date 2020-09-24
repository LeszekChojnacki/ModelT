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
package de.hybris.platform.solrfacetsearch.search.context;

import de.hybris.platform.solrfacetsearch.search.FacetSearchException;


/**
 * Interface for receiving notifications about facet search execution.
 */
public interface FacetSearchListener
{
	/**
	 * Handles a notification that a facet search service is about to begin execution.
	 *
	 * @param facetSearchContext
	 *           - the {@link FacetSearchContext}
	 *
	 * @throws FacetSearchException
	 *            if an error occurs
	 */
	void beforeSearch(FacetSearchContext facetSearchContext) throws FacetSearchException;

	/**
	 * Handles a notification that a facet search service has just completed.
	 *
	 * @param facetSearchContext
	 *           - the {@link FacetSearchContext}
	 *
	 * @throws FacetSearchException
	 *            if an error occurs
	 */
	void afterSearch(FacetSearchContext facetSearchContext) throws FacetSearchException;

	/**
	 * Handles a notification that a facet search service failed (this may also be due to listeners failing).
	 *
	 * @param facetSearchContext
	 *           - the {@link FacetSearchContext}
	 *
	 * @throws FacetSearchException
	 *            if an error occurs
	 */
	void afterSearchError(FacetSearchContext facetSearchContext) throws FacetSearchException;
}
