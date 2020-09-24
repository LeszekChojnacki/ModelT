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

import de.hybris.platform.adaptivesearch.model.AsSearchProfileActivationSetModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;

import java.util.List;
import java.util.Optional;


/**
 * The {@link AsSearchProfileActivationSetModel} DAO.
 */
public interface AsSearchProfileActivationSetDao
{
	/**
	 * Finds all search profile activation sets.
	 *
	 * @return the search profiles activation sets or empty list if no search profile activation set is found
	 */
	List<AsSearchProfileActivationSetModel> findAllSearchProfileActivationSets();

	/**
	 * Finds the search profile activation set for a specific catalog version and index type.
	 *
	 * @param catalogVersion
	 *           - the catalog version
	 * @param indexType
	 *           - the index type
	 *
	 * @return the search profile
	 */
	Optional<AsSearchProfileActivationSetModel> findSearchProfileActivationSetByIndexType(final CatalogVersionModel catalogVersion,
			final String indexType);
}
