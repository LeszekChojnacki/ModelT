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

import de.hybris.platform.adaptivesearch.model.AbstractAsSearchConfigurationModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;

import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * The {@link AbstractAsSearchConfigurationModel} DAO.
 */
public interface AsSearchConfigurationDao
{
	/**
	 * Finds all search configurations.
	 *
	 * @return list of search configurations or empty list if no configuration is found
	 */
	<T extends AbstractAsSearchConfigurationModel> List<T> findAllSearchConfigurations();

	/**
	 * Finds all search configurations for a specific catalog version.
	 *
	 * @return list of search configurations or empty list if no configuration is found
	 */
	<T extends AbstractAsSearchConfigurationModel> List<T> findSearchConfigurationsByCatalogVersion(
			final CatalogVersionModel catalogVersion);

	/**
	 * Finds the search configuration for a specific catalog version and uid.
	 *
	 * @param catalogVersion
	 *           - the catalog version
	 * @param uid
	 *           - the unique identifier
	 *
	 * @return the search configuration
	 */
	<T extends AbstractAsSearchConfigurationModel> Optional<T> findSearchConfigurationByUid(
			final CatalogVersionModel catalogVersion, final String uid);

	/**
	 * Finds all search configurations matching the given filters.
	 *
	 * @param type
	 *           - the search configuration type
	 * @param filters
	 *           - the parameters
	 *
	 * @return list of search configurations or empty list if no configuration is found
	 */
	<T extends AbstractAsSearchConfigurationModel> List<T> findSearchConfigurations(Class<T> type, Map<String, Object> filters);
}
