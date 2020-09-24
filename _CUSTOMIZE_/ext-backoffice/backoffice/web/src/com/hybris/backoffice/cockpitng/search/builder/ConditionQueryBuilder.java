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
package com.hybris.backoffice.cockpitng.search.builder;

import de.hybris.platform.core.GenericCondition;
import de.hybris.platform.core.GenericQuery;

import java.util.Collections;
import java.util.List;

import com.hybris.cockpitng.dataaccess.facades.search.FieldSearchFacadeStrategy;
import com.hybris.cockpitng.search.data.SearchAttributeDescriptor;
import com.hybris.cockpitng.search.data.SearchQueryCondition;
import com.hybris.cockpitng.search.data.SearchQueryData;


/**
 * An interface represents condition query builder that is used by {@link FieldSearchFacadeStrategy}.
 * </p>
 */
public interface ConditionQueryBuilder
{
	List<GenericCondition> buildQuery(final GenericQuery query, String typeCode, SearchAttributeDescriptor attribute,
			final SearchQueryData searchQueryData);

	default List<GenericCondition> buildQuery(final GenericQuery query, final String typeCode,
			final SearchQueryCondition condition, final SearchQueryData searchQueryData)
	{
		return Collections.emptyList();
	}
}
