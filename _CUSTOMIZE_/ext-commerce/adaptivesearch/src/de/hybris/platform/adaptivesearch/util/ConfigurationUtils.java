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
package de.hybris.platform.adaptivesearch.util;

import de.hybris.platform.adaptivesearch.enums.AsBoostItemsMergeMode;
import de.hybris.platform.adaptivesearch.enums.AsBoostRulesMergeMode;
import de.hybris.platform.adaptivesearch.enums.AsFacetsMergeMode;
import de.hybris.platform.adaptivesearch.enums.AsSortsMergeMode;
import de.hybris.platform.util.Config;


public final class ConfigurationUtils
{
	public static final String DEFAULT_FACETS_MERGE_MODE = "adaptivesearch.merge.facets.default";
	public static final String DEFAULT_BOOST_ITEMS_MERGE_MODE = "adaptivesearch.merge.boostitems.default";
	public static final String DEFAULT_BOOST_RULES_MERGE_MODE = "adaptivesearch.merge.boostrules.default";
	public static final String DEFAULT_SORTS_MERGE_MODE = "adaptivesearch.merge.sorts.default";

	private ConfigurationUtils()
	{
		// utility class
	}

	public static AsFacetsMergeMode getDefaultFacetsMergeMode()
	{
		return AsFacetsMergeMode.valueOf(Config.getString(DEFAULT_FACETS_MERGE_MODE, AsFacetsMergeMode.ADD_AFTER.name()));
	}

	public static AsBoostItemsMergeMode getDefaultBoostItemsMergeMode()
	{
		return AsBoostItemsMergeMode
				.valueOf(Config.getString(DEFAULT_BOOST_ITEMS_MERGE_MODE, AsBoostItemsMergeMode.ADD_AFTER.name()));
	}

	public static AsBoostRulesMergeMode getDefaultBoostRulesMergeMode()
	{
		return AsBoostRulesMergeMode.valueOf(Config.getString(DEFAULT_BOOST_RULES_MERGE_MODE, AsBoostRulesMergeMode.ADD.name()));
	}

	public static AsSortsMergeMode getDefaultSortsMergeMode()
	{
		return AsSortsMergeMode.valueOf(Config.getString(DEFAULT_SORTS_MERGE_MODE, AsSortsMergeMode.ADD_AFTER.name()));
	}
}
