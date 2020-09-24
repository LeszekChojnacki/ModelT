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
package de.hybris.platform.adaptivesearch.services;

import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Service that provides basic functionality for search profiles.
 */
public interface AsSearchProfileService
{
	/**
	 * Returns all search profiles.
	 *
	 * @return list of search profiles or empty list if no profile is found
	 */
	<T extends AbstractAsSearchProfileModel> List<T> getAllSearchProfiles();

	/**
	 * Returns all search profiles for a list of index types and catalog versions.
	 *
	 * @param indexTypes
	 *           - list of index types
	 * @param catalogVersions
	 *           - list of catalog versions
	 *
	 * @return list of search profiles or empty list if no profile is found
	 */
	<T extends AbstractAsSearchProfileModel> List<T> getSearchProfilesForIndexTypesAndCatalogVersions(List<String> indexTypes,
			List<CatalogVersionModel> catalogVersions);

	/**
	 * Returns all search profiles for a specific catalog version.
	 *
	 * @param catalogVersion
	 *           - the catalog version
	 *
	 * @return list of search profiles or empty list if no profile is found
	 */
	<T extends AbstractAsSearchProfileModel> List<T> getSearchProfilesForCatalogVersion(final CatalogVersionModel catalogVersion);

	/**
	 * Returns the search profile a specific catalog version and code.
	 *
	 * @param catalogVersion
	 *           - the catalog version
	 * @param code
	 *           - the code
	 *
	 * @return the search profile
	 */
	<T extends AbstractAsSearchProfileModel> Optional<T> getSearchProfileForCode(final CatalogVersionModel catalogVersion,
			final String code);

	/**
	 * Returns search profiles.
	 *
	 * @param query
	 *           query parameter for filtering
	 * @param filters
	 *           parameters for filtering
	 *
	 * @return list of search profiles or empty list if no profile is found
	 */
	<T extends AbstractAsSearchProfileModel> List<T> getSearchProfiles(final String query, final Map<String, Object> filters);

	/**
	 * Returns paginated search profiles.
	 *
	 * @param query
	 *           query parameter for filtering
	 * @param filters
	 *           parameters for filtering
	 * @param pagination
	 *           holder of pagination data and sort options
	 *
	 * @return list of search profiles or empty list if no profile is found
	 */
	<T extends AbstractAsSearchProfileModel> SearchPageData<T> getSearchProfiles(final String query,
			final Map<String, Object> filters, final SearchPageData<?> pagination);

	/**
	 * Creates a deep copy of the given search profile. The resulting object is not persisted yet in order to allow
	 * modifications like unique key adjustments etc.
	 *
	 * @param original
	 *           - the original search profile
	 *
	 * @return the deep copy of the search profile
	 */
	<T extends AbstractAsSearchProfileModel> T cloneSearchProfile(T original);

}
