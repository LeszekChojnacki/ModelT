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
import de.hybris.platform.adaptivesearch.data.AsMergeConfiguration;
import de.hybris.platform.adaptivesearch.data.AsSearchProfileResult;

import java.util.List;

/**
 * Strategy for merging search profile results.
 */
public interface AsMergeStrategy
{
	/**
	 * Merges multiple search profile results into a single result.
	 *
	 * @param context
	 *           - the search profile context
	 * @param results
	 *           - the search profiles results to merge
	 *
	 * @return the result of the merge
	 */
	default AsSearchProfileResult merge(AsSearchProfileContext context, List<AsSearchProfileResult> results)
	{
		return merge(context, results, null);
	}

	/**
	 * Merges multiple search profile results into a single result.
	 *
	 * @param context
	 *           - the search profile context
	 * @param results
	 *           - the search profiles results to merge
	 * @param mergeConfiguration
	 *           - the merge configuration
	 *
	 * @return the result of the merge
	 */
	AsSearchProfileResult merge(AsSearchProfileContext context, List<AsSearchProfileResult> results, AsMergeConfiguration mergeConfiguration);
}
