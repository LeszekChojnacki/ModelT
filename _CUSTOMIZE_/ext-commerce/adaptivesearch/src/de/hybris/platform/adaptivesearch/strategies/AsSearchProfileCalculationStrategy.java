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
import de.hybris.platform.adaptivesearch.data.AsSearchProfileResult;


/**
 * Strategy for calculating search profiles.
 *
 * @param <T>
 *           - the type of search profile data
 */
public interface AsSearchProfileCalculationStrategy<T extends AbstractAsSearchProfile> extends AsCacheAwareStrategy<T>
{
	/**
	 * Calculates the search profile result for a given search profile data object. The returned value can be cached.
	 *
	 * @param context
	 *           - the search profile context
	 * @param searchProfile
	 *           - the search profile data
	 *
	 * @return the search profile result
	 */
	AsSearchProfileResult calculate(AsSearchProfileContext context, T searchProfile);

	/**
	 * Allows additional processing on the search profile result. This method should not modify the given search profile
	 * result. The returned value should not be cached.
	 *
	 * @param context
	 *           - the search profile context
	 * @param result
	 *           - the search profile result
	 *
	 * @return the new search profile result
	 */
	default AsSearchProfileResult map(final AsSearchProfileContext context, final AsSearchProfileResult result)
	{
		return result;
	}
}
