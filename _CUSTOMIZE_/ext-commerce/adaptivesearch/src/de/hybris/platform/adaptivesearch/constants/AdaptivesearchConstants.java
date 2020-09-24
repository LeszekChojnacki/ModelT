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
package de.hybris.platform.adaptivesearch.constants;


public final class AdaptivesearchConstants extends GeneratedAdaptivesearchConstants
{
	public static final String EXTENSIONNAME = "adaptivesearch";

	public static final String SEARCH_PROFILE_ATTRIBUTE = "searchProfile";
	public static final String SEARCH_CONFIGURATION_ATTRIBUTE = "searchConfiguration";
	public static final String FACET_CONFIGURATION_ATTRIBUTE = "facetConfiguration";
	public static final String SORT_CONFIGURATION_ATTRIBUTE = "sortConfiguration";

	public static final String UNIQUE_IDX_SEPARATOR = "_";
	public static final String UNIQUE_IDX_NULL_IDENTIFIER = "null";

	public static final String PROMOTED_TAG = "promoted";
	public static final String HIGHLIGHTED_TAG = "highlighted";
	public static final String EXCLUDED_TAG = "excluded";

	public static final String PK_FIELD = "pk";

	public static final int DEFAULT_FACET_PRIORITY = 100;
	public static final int DEFAULT_SORT_PRIORITY = 100;

	public static final String GLOBAL_CATEGORY_LABEL = "adaptivesearch.globalcategory";

	public static final String CATEGORY_QUALIFIER_TYPE = "category";
	public static final String DEFAULT_QUALIFIER = null;

	private AdaptivesearchConstants()
	{
		//empty to avoid instantiating this constant class
	}
}
