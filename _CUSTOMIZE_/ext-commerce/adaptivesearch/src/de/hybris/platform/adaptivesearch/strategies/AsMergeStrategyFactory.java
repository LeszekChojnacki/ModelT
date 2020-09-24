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

import de.hybris.platform.adaptivesearch.enums.AsBoostItemsMergeMode;
import de.hybris.platform.adaptivesearch.enums.AsBoostRulesMergeMode;
import de.hybris.platform.adaptivesearch.enums.AsFacetsMergeMode;
import de.hybris.platform.adaptivesearch.enums.AsSortsMergeMode;


/**
 * Implementations of this interface are responsible for resolving and creating instances of merge strategies.
 */
public interface AsMergeStrategyFactory
{
	/**
	 * Returns the facets merge strategy.
	 *
	 * @param mergeMode
	 *           - the facets merge mode
	 *
	 * @return the strategy
	 */
	AsFacetsMergeStrategy getFacetsMergeStrategy(final AsFacetsMergeMode mergeMode);

	/**
	 * Returns the boost items merge strategy.
	 *
	 * @param mergeMode
	 *           - the boost items merge mode
	 *
	 * @return the strategy
	 */
	AsBoostItemsMergeStrategy getBoostItemsMergeStrategy(final AsBoostItemsMergeMode mergeMode);

	/**
	 * Returns the boost rules merge strategy.
	 *
	 * @param mergeMode
	 *           - the boost rules merge mode
	 *
	 * @return the strategy
	 */
	AsBoostRulesMergeStrategy getBoostRulesMergeStrategy(final AsBoostRulesMergeMode mergeMode);


	/**
	 * Returns the sorts merge strategy.
	 *
	 * @param mergeMode
	 *           - the sorts merge mode
	 *
	 * @return the strategy
	 */
	AsSortsMergeStrategy getSortsMergeStrategy(final AsSortsMergeMode mergeMode);
}
