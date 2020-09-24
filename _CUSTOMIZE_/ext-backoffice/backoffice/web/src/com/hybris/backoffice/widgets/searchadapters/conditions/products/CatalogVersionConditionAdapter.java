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
package com.hybris.backoffice.widgets.searchadapters.conditions.products;

import de.hybris.platform.catalog.model.CatalogVersionModel;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.navigation.NavigationNode;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.backoffice.widgets.advancedsearch.impl.SearchConditionData;
import com.hybris.backoffice.widgets.searchadapters.conditions.SearchConditionAdapter;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;


/**
 * {@link SearchConditionAdapter} responsible for handling node representing catalog version.
 * The handler gets catalog's version and add them as a condition to {@link AdvancedSearchData}.
 */
public class CatalogVersionConditionAdapter extends SearchConditionAdapter
{

	private String catalogVersionPropertyName;
	private ValueComparisonOperator operator;

	@Override
	public boolean canHandle(final NavigationNode node)
	{
		return node.getData() instanceof CatalogVersionModel;
	}

	@Override
	public void addSearchCondition(final AdvancedSearchData searchData, final NavigationNode node)
	{
		final CatalogVersionModel catalogVersion = (CatalogVersionModel) node.getData();
		final SearchConditionData condition = createSearchConditions(catalogVersionPropertyName, catalogVersion.getPk(), operator);
		searchData.addCondition(condition.getFieldType(), condition.getOperator(), condition.getValue());
	}

	@Required
	public void setCatalogVersionPropertyName(final String catalogVersionPropertyName)
	{
		this.catalogVersionPropertyName = catalogVersionPropertyName;
	}

	@Required
	public void setOperator(final ValueComparisonOperator operator)
	{
		this.operator = operator;
	}
}
