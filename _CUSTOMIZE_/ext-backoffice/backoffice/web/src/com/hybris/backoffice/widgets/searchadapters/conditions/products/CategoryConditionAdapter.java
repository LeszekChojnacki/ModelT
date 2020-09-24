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

import de.hybris.platform.category.model.CategoryModel;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.navigation.NavigationNode;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.backoffice.widgets.advancedsearch.impl.SearchConditionData;
import com.hybris.backoffice.widgets.searchadapters.conditions.SearchConditionAdapter;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;


/**
 * {@link SearchConditionAdapter} responsible for handling node representing category.
 * The handler gets all subcategories of given category and add them and itself as a condition to
 * {@link AdvancedSearchData}.
 */
public class CategoryConditionAdapter extends SearchConditionAdapter
{

	private String categoryPropertyName;
	private ValueComparisonOperator operator;

	@Override
	public boolean canHandle(final NavigationNode node)
	{
		return node.getData() instanceof CategoryModel;
	}

	@Override
	public void addSearchCondition(final AdvancedSearchData searchData, final NavigationNode node)
	{
		final CategoryModel category = (CategoryModel) node.getData();
		final Stream<CategoryModel> categoryStream = appendToStream(category.getAllSubcategories().stream(), category);
		final List<SearchConditionData> conditions = categoryStream.map(
				subcategory -> createSearchConditions(categoryPropertyName, subcategory.getPk(), operator)).collect(
				Collectors.toList());
		searchData.addConditionList(ValueComparisonOperator.OR, conditions);
	}

	protected Stream<CategoryModel> appendToStream(final Stream<CategoryModel> stream, final CategoryModel categoryToAdd)
	{
		return Stream.concat(stream, Stream.of(categoryToAdd));
	}

	@Required
	public void setCategoryPropertyName(final String categoryPropertyName)
	{
		this.categoryPropertyName = categoryPropertyName;
	}

	@Required
	public void setOperator(final ValueComparisonOperator operator)
	{
		this.operator = operator;
	}
}
