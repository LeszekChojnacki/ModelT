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
package com.hybris.backoffice.solrsearch.services;

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.exceptions.FacetConfigServiceException;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedTypeModel;

import java.util.Collection;


/**
 * Configuration service for backoffice solr search.
 */
public interface BackofficeFacetSearchConfigService
{

	/**
	 * Finds matching config for given type or one of its superTypes.
	 *
	 * @param typeCode
	 *           type code.
	 * @return config for given type code.
	 * @throws FacetConfigServiceException
	 * 			when configuration cannot be load
	 */
	FacetSearchConfig getFacetSearchConfig(String typeCode) throws FacetConfigServiceException;

	/**
	 * Finds all configs configured to be used in backoffice.
	 *
	 * @return collection of configs or an empty collection if there is none
	 */
	Collection<FacetSearchConfig> getAllMappedFacetSearchConfigs();

	/**
	 * Finds matching config for given type or one of its superTypes.
	 *
	 * @param typeCode
	 *           type code.
	 * @return config for given type code.
	 * @throws FacetConfigServiceException
	 * 			when configuration cannot be load
	 */
	SolrFacetSearchConfigModel getSolrFacetSearchConfigModel(final String typeCode) throws FacetConfigServiceException;

	/**
	 * Finds indexed type for given type code or one of its superTypes.
	 *
	 * @param typeCode
	 *           type code.
	 * @return index config for given type code.
	 */
	SolrIndexedTypeModel getSolrIndexedType(String typeCode);

	/**
	 * Gets converted index config from given config for given type code or one of its superTypes
	 *
	 * @param config
	 *           config which includes indexed types.
	 * @param typeCode
	 *           type code.
	 * @return converted index for given type code
	 */
	IndexedType getIndexedType(final FacetSearchConfig config, final String typeCode);

	/**
	 * Tells whether there is existing solr config for given type or one of it's superTypes.
	 *
	 * @param typeCode
	 *           type code.
	 * @return true if there is solr config for given type code.
	 */
	boolean isSolrSearchConfiguredForType(final String typeCode);

	/**
	 * Tells whether there is existing backoffice solr config for given name.
	 *
	 * @param facetSearchConfigName
	 *           name of the solr configuration
	 *
	 * @return true if there is solr config for given name.
	 */
	boolean isBackofficeSolrSearchConfiguredForName(final String facetSearchConfigName);

}
