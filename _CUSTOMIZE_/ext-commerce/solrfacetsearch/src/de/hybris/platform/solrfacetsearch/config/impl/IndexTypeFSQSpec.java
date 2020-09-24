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
package de.hybris.platform.solrfacetsearch.config.impl;

import de.hybris.platform.solrfacetsearch.config.FlexibleSearchQuerySpec;
import de.hybris.platform.solrfacetsearch.config.IndexedTypeFlexibleSearchQuery;

import java.util.Map;


/**
 *
 */
public class IndexTypeFSQSpec<T extends IndexedTypeFlexibleSearchQuery> implements FlexibleSearchQuerySpec
{


	private final T indexedTypeFlexileSearchQueryData;

	/**
	 * 
	 */
	public IndexTypeFSQSpec(final T indexedTypeFlexileSearchQueryData)
	{
		super();
		this.indexedTypeFlexileSearchQueryData = indexedTypeFlexileSearchQueryData;
	}

	@Override
	public String getQuery()
	{
		return indexedTypeFlexileSearchQueryData.getQuery();
	}

	@Override
	public Map<String, Object> createParameters()
	{
		return indexedTypeFlexileSearchQueryData.getParameters();
	}

	@Override
	public String getUser()
	{
		return indexedTypeFlexileSearchQueryData.getUserId();
	}


}
