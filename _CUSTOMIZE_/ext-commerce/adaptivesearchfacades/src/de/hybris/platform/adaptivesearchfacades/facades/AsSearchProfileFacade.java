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
package de.hybris.platform.adaptivesearchfacades.facades;

import de.hybris.adaptivesearchfacades.data.AsSearchProfileData;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

import java.util.List;
import java.util.Map;


/**
 * Facade for search profile functionality.
 */
public interface AsSearchProfileFacade
{
	/**
	 * Returns search profiles
	 *
	 * @param query
	 *           query parameter for filtering
	 * @param filters
	 *           parameters for filtering
	 *
	 * @return list of search profiles or empty list if no profile is found
	 */
	List<AsSearchProfileData> getSearchProfiles(final String query, final Map<String, String> filters);

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
	SearchPageData<AsSearchProfileData> getSearchProfiles(final String query, final Map<String, String> filters, final SearchPageData<?> pagination);
}
