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
package de.hybris.platform.adaptivesearch.daos;

import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * The {@link AbstractAsSearchProfileModel} DAO.
 */
public interface AsSearchProfileDao
{
	/**
	 * Finds all search profiles.
	 *
	 * @return list of search profiles or empty list if no profile is found
	 */
	<T extends AbstractAsSearchProfileModel> List<T> findAllSearchProfiles();

	/**
	 * Finds all search profiles for a list of index types and catalog versions.
	 *
	 * @return list of search profiles or empty list if no profile is found
	 */
	<T extends AbstractAsSearchProfileModel> List<T> findSearchProfilesByIndexTypesAndCatalogVersions(final List<String> indexTypes, final List<CatalogVersionModel>
			catalogVersions);

	/**
	 * Finds all search profiles for a specific catalog version.
	 *
	 * @return list of search profiles or empty list if no profile is found
	 */
	<T extends AbstractAsSearchProfileModel> List<T> findSearchProfilesByCatalogVersion(final CatalogVersionModel catalogVersion);

	/**
	 * Finds the search profile for a specific catalog version and code.
	 *
	 * @param catalogVersion
	 *           - the catalog version
	 * @param code
	 *           - the code
	 *
	 * @return the search profile
	 */
	<T extends AbstractAsSearchProfileModel> Optional<T> findSearchProfileByCode(final CatalogVersionModel catalogVersion,
																				 final String code);

	/**
	 * Search for search profiles with filters.
	 *
	 * @param query
	 *           query parameter for filtering
	 * @param filters
	 *           parameters for search
	 *
	 * @return list of search profiles or empty list if no profile is found
	 */
	<T extends AbstractAsSearchProfileModel> List<T> getSearchProfiles(final String query, final Map<String, Object> filters);

	/**
	 * Search for search profiles with filters.
	 *
	 * @param query
	 *           query parameter for filtering
	 * @param filters
	 *           parameters for search
	 * @param pagination
	 *           holder of pagination data and sort options
	 *
	 * @return list of search profiles or empty list if no profile is found
	 */
	<T extends AbstractAsSearchProfileModel> SearchPageData<T> getSearchProfiles(final String query, final Map<String, Object> filters, final SearchPageData<?> pagination);
}
