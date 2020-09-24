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

import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurationModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;

import java.util.Optional;


/**
 * The {@link AbstractAsConfigurationModel} DAO.
 */
public interface AsConfigurationDao
{
	/**
	 * Finds the configuration for a specific type, catalog version and uid.
	 *
	 * @param type
	 *           - the type
	 * @param catalogVersion
	 *           - the catalog version
	 * @param uid
	 *           - the unique identifier
	 *
	 * @return the configuration
	 */
	<T extends AbstractAsConfigurationModel> Optional<T> findConfigurationByUid(Class<T> type,
			final CatalogVersionModel catalogVersion, final String uid);
}
