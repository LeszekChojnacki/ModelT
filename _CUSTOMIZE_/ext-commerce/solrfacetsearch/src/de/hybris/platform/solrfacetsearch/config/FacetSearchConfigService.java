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

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.solrfacetsearch.config.exceptions.FacetConfigServiceException;

import java.util.Collection;
import java.util.List;


/**
 * This service provides access to the facet search configurations
 */
public interface FacetSearchConfigService
{
	/**
	 * Returns the configuration by name
	 *
	 * @param name
	 *           - Name of configuration
	 *
	 * @return {@link FacetSearchConfig} object
	 *
	 * @throws FacetConfigServiceException
	 *            if an error occurs
	 */
	FacetSearchConfig getConfiguration(String name) throws FacetConfigServiceException;

	/**
	 * Searches for the configuration for the specified catalog version.
	 *
	 * @param catalogVersion
	 *           the catalog version to be searched
	 *
	 * @return {@link FacetSearchConfig} if it is found. If more than one configurations can be found, return the first one.
	 *         If it cannot be found, return null.
	 *
	 * @throws FacetConfigServiceException
	 *            if an error occurs
	 */
	FacetSearchConfig getConfiguration(CatalogVersionModel catalogVersion) throws FacetConfigServiceException;

	/**
	 * Resolves indexed type by name.
	 *
	 * @param facetSearchConfig
	 *           facet search configuration.
	 * @param indexedTypeName
	 *           name of indexed type
	 *
	 * @return {@link IndexedType}
	 *
	 * @throws FacetConfigServiceException
	 *            if an error occurs
	 */
	IndexedType resolveIndexedType(final FacetSearchConfig facetSearchConfig, final String indexedTypeName)
			throws FacetConfigServiceException;

	/**
	 * Resolves indexed properties by ids.
	 *
	 * @param facetSearchConfig
	 *           facet search configuration.
	 * @param indexedType
	 *           indexed type
	 * @param indexedPropertiesIds
	 *           list of indexed properties ids
	 *
	 * @return List of {@link IndexedProperty}
	 *
	 * @throws FacetConfigServiceException
	 *            if an error occurs
	 */
	List<IndexedProperty> resolveIndexedProperties(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final Collection<String> indexedPropertiesIds) throws FacetConfigServiceException;
}
