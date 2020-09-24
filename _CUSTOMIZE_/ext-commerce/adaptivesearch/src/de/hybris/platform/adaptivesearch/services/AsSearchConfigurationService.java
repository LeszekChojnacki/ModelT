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

import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;
import de.hybris.platform.adaptivesearch.data.AsSearchConfigurationInfoData;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;

import java.util.List;
import java.util.Optional;
import java.util.Set;


/**
 * Service that provides basic functionality for search configurations.
 */
public interface AsSearchConfigurationService
{
	/**
	 * Returns all search configurations.
	 *
	 * @return list of search configurations or empty list if no configuration is found
	 */
	<T extends AbstractAsSearchConfigurationModel> List<T> getAllSearchConfigurations();

	/**
	 * Returns all search configurations for a specific catalog version.
	 *
	 * @param catalogVersion
	 *           - the catalog version
	 *
	 * @return list of search configurations or empty list if no configuration is found
	 */
	<T extends AbstractAsSearchConfigurationModel> List<T> getSearchConfigurationsForCatalogVersion(
			final CatalogVersionModel catalogVersion);

	/**
	 * Returns the search configuration for a specific catalog version and uid.
	 *
	 * @param catalogVersion
	 *           - the catalog version
	 * @param uid
	 *           - the unique identifier
	 *
	 * @return the search configuration
	 */
	<T extends AbstractAsSearchConfigurationModel> Optional<T> getSearchConfigurationForUid(
			final CatalogVersionModel catalogVersion, final String uid);

	/**
	 * Returns the search configuration for a specific context and search profile.
	 *
	 * @param context
	 *           - the search profile context
	 * @param searchProfile
	 *           - the search profile
	 *
	 * @return the search configuration
	 */
	<T extends AbstractAsSearchConfigurationModel> Optional<T> getSearchConfigurationForContext(AsSearchProfileContext context,
			AbstractAsSearchProfileModel searchProfile);

	/**
	 * Returns the search configuration for a specific context and search profile. If the search configuration does not
	 * yet exist a new one is created.
	 *
	 * @param context
	 *           - the search profile context
	 * @param searchProfile
	 *           - the search profile
	 *
	 * @return the search configuration
	 */
	<T extends AbstractAsSearchConfigurationModel> T getOrCreateSearchConfigurationForContext(AsSearchProfileContext context,
			AbstractAsSearchProfileModel searchProfile);

	/**
	 * Returns search configuration related information for a specific context and search profile.
	 *
	 * @param context
	 *           - the search profile context
	 * @param searchProfile
	 *           - the search profile
	 *
	 * @return the search configuration
	 */
	AsSearchConfigurationInfoData getSearchConfigurationInfoForContext(AsSearchProfileContext context,
			AbstractAsSearchProfileModel searchProfile);

	/**
	 * Clones the given search configuration.
	 *
	 * @param searchConfiguration
	 *           - the search configuration to be cloned
	 * @return - the cloned version
	 */
	<T extends AbstractAsSearchConfigurationModel> T cloneSearchConfiguration(T searchConfiguration);

	/**
	 * Gets the qualifiers for the search profile
	 *
	 * @param searchProfile
	 *           - the search profile
	 *           
	 * @return qualifiers for the search profile
	 */
	Set<String> getSearchConfigurationQualifiers(AbstractAsSearchProfileModel searchProfile);
}
