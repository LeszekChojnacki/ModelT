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
package de.hybris.platform.adaptivesearch.strategies;

import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;
import de.hybris.platform.adaptivesearch.data.AbstractAsSearchProfile;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;


/**
 * Strategy for loading search profiles.
 *
 * @param <T>
 *           - the type of search profile model
 * @param <R>
 *           - the type of search profile data
 */
public interface AsSearchProfileLoadStrategy<T extends AbstractAsSearchProfileModel, R extends AbstractAsSearchProfile>
		extends AsCacheAwareStrategy<T>
{
	/**
	 * Loads the search profile model and converts it to some data object. The returned value can be cached.
	 *
	 * @param context
	 *           - the search profile context
	 * @param searchProfile
	 *           - the search profile model
	 *
	 * @return the search profile data
	 */
	R load(AsSearchProfileContext context, T searchProfile);

	/**
	 * Allows additional processing on the search profile data. This method should not modify the given search profile
	 * data. The returned value should not be cached.
	 *
	 * @param context
	 *           - the search profile context
	 * @param searchProfile
	 *           - the search profile data
	 *
	 * @return the new search profile data
	 */
	default R map(final AsSearchProfileContext context, final R searchProfile)
	{
		return searchProfile;
	}
}
