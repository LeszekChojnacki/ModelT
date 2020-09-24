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
package com.hybris.backoffice.solrsearch.decorators;

import de.hybris.platform.solrfacetsearch.config.IndexedType;

import org.springframework.core.Ordered;

import com.hybris.backoffice.solrsearch.dataaccess.SearchConditionData;
import com.hybris.cockpitng.search.data.SearchQueryData;


/**
 * Provider for custom conditions for a search query.
 */
public interface SearchConditionDecorator extends Ordered
{
	/**
	 * Decorates search condition data{@link SearchConditionData}. In this decorator list of fq conditions and search
	 * conditions can be modified.
	 * 
	 * @param searchConditionData search condition data which contains filter query conditions and query conditions.
	 * @param queryData query data which comes from client side.
	 * @param indexedType indexed type.
	 */
	void decorate(SearchConditionData searchConditionData, SearchQueryData queryData, IndexedType indexedType);
}
