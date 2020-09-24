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
package com.hybris.backoffice.solrsearch.adapters.conditions.product;

import de.hybris.platform.catalog.model.classification.ClassificationSystemModel;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.navigation.NavigationNode;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.backoffice.widgets.advancedsearch.impl.SearchConditionData;
import com.hybris.backoffice.widgets.searchadapters.conditions.SearchConditionAdapter;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;


/**
 * {@link SearchConditionAdapter} responsible for handling node representing classification catalog.
 * The handler gets classification system pk and add it as a condition to {@link AdvancedSearchData}.
 */
public class SolrSearchClassificationSystemConditionAdapter extends SearchConditionAdapter
{
	private String classificationSystemPropertyName;
	private ValueComparisonOperator operator;

	@Override
	public boolean canHandle(final NavigationNode node)
	{
		return node.getData() instanceof ClassificationSystemModel;
	}

	@Override
	public void addSearchCondition(final AdvancedSearchData searchData, final NavigationNode node)
	{
		final ClassificationSystemModel classificationSystem = (ClassificationSystemModel) node.getData();
		final SearchConditionData catalogCondition = createSearchConditions(classificationSystemPropertyName,
				classificationSystem.getPk(), operator);
		searchData.addCondition(catalogCondition.getFieldType(), catalogCondition.getOperator(), catalogCondition.getValue());
	}

	@Required
	public void setClassificationSystemPropertyName(final String classificationSystemPropertyName)
	{
		this.classificationSystemPropertyName = classificationSystemPropertyName;
	}

	@Required
	public void setOperator(final ValueComparisonOperator operator)
	{
		this.operator = operator;
	}
}
