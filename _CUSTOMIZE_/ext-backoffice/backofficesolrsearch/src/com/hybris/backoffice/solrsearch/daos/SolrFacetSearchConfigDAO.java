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
package com.hybris.backoffice.solrsearch.daos;

import de.hybris.platform.core.model.type.ComposedTypeModel;

import java.util.Collection;
import java.util.List;

import com.hybris.backoffice.solrsearch.model.BackofficeIndexedTypeToSolrFacetSearchConfigModel;


/**
 * DAO for {@link BackofficeIndexedTypeToSolrFacetSearchConfigModel}
 */
public interface SolrFacetSearchConfigDAO
{
	/**
	 * Finds list of {@link com.hybris.backoffice.solrsearch.model.BackofficeIndexedTypeToSolrFacetSearchConfigModel} for
	 * given composedTypes.
	 */
	Collection<BackofficeIndexedTypeToSolrFacetSearchConfigModel> findSearchConfigurationsForTypes(final List<ComposedTypeModel> composedTypes);

	/**
	 * Finds list of {@link com.hybris.backoffice.solrsearch.model.BackofficeIndexedTypeToSolrFacetSearchConfigModel} for
	 * given facetSearchConfig name.
	 */
	Collection<BackofficeIndexedTypeToSolrFacetSearchConfigModel> findSearchConfigurationsForName(final String facetSearchConfigName);

	/**
	 * Finds all {@link com.hybris.backoffice.solrsearch.model.BackofficeIndexedTypeToSolrFacetSearchConfigModel}
	 *
 	 * @return list of search configs
	 */
	Collection<BackofficeIndexedTypeToSolrFacetSearchConfigModel> findAllSearchConfigs();

}
