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
package com.hybris.backoffice.widgets.searchadapters.conditions;

import com.hybris.backoffice.navigation.NavigationNode;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.backoffice.widgets.advancedsearch.impl.SearchConditionData;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.advancedsearch.FieldType;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;


/**
 * Abstract adapter responsible for preparing {@link SearchConditionData} based on {@link NavigationNode}
 */
public abstract class SearchConditionAdapter
{

	/**
	 * Indicated whether handler is able to handle given navigationNode.
	 *
	 * @param node {@link NavigationNode}
	 * @return boolean
	 */
	public abstract boolean canHandle(final NavigationNode node);

	/**
	 * Adds condition to existing set of conditions.
	 *
	 * @param searchData {@link AdvancedSearchData} existing set of conditions
	 * @param node {@link NavigationNode} navigation node to handle
	 */
	public abstract void addSearchCondition(final AdvancedSearchData searchData, final NavigationNode node);

	/**
	 * Creates {@link SearchConditionData} based on field name, condition data and comparison operator
	 * 
	 * @param fieldName {@link String} name of field which should be used in condition
	 * @param data {@link Object} data used in condition, for example: PK
	 * @param comparisonOperator {@link ValueComparisonOperator} operator used in condition
	 * @return {@link SearchConditionData}
	 */
	protected SearchConditionData createSearchConditions(final String fieldName, final Object data,
			final ValueComparisonOperator comparisonOperator)
	{
		final FieldType fieldType = new FieldType();
		fieldType.setDisabled(Boolean.FALSE);
		fieldType.setSelected(Boolean.TRUE);
		fieldType.setName(fieldName);
		return new SearchConditionData(fieldType, data, comparisonOperator);
	}

}
