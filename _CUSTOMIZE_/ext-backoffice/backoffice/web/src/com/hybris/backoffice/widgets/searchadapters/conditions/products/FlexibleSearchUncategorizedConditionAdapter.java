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

import de.hybris.platform.core.model.product.ProductModel;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.navigation.NavigationNode;
import com.hybris.backoffice.navigation.impl.SimpleNode;
import com.hybris.backoffice.tree.model.CatalogTreeModelPopulator;
import com.hybris.backoffice.tree.model.UncategorizedNode;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.backoffice.widgets.advancedsearch.impl.SearchConditionData;
import com.hybris.backoffice.widgets.searchadapters.conditions.SearchConditionAdapter;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;



/**
 * {@link SearchConditionAdapter} responsible for handling node representing uncategorized products.
 * The handler adds condition to {@link AdvancedSearchData} that product doesn't have assigned any category.
 * Moreover it finds and invokes handler for parent object which may represents CatalogModel or CatalogVersionModel
 */
public class FlexibleSearchUncategorizedConditionAdapter extends SearchConditionAdapter
{

	public static final String PARENT_NODE_ID = "parentNode";

	private List<SearchConditionAdapter> conditionsAdapters;

	@Override
	public boolean canHandle(final NavigationNode node)
	{
		return StringUtils.endsWith(node.getId(), CatalogTreeModelPopulator.UNCATEGORIZED_PRODUCTS_NODE_ID)
				&& node.getData() instanceof UncategorizedNode;
	}

	@Override
	public void addSearchCondition(final AdvancedSearchData searchData, final NavigationNode node)
	{
		final UncategorizedNode uncategorizedNode = (UncategorizedNode) node.getData();
		final Object parentItem = uncategorizedNode.getParentItem();

		final List<SearchConditionData> conditionList = new ArrayList<>();
		if (parentItem != null)
		{
			final AdvancedSearchData innerSearchData = new AdvancedSearchData();
			final SimpleNode simpleNode = new SimpleNode(PARENT_NODE_ID);
			simpleNode.setData(parentItem);
			conditionsAdapters.stream().filter(adapter -> adapter.canHandle(simpleNode)).findFirst()
					.ifPresent(adapter -> adapter.addSearchCondition(innerSearchData, simpleNode));

			for (final String searchField : innerSearchData.getSearchFields())
			{
				final List<SearchConditionData> parentConditions = innerSearchData.getConditions(searchField);
				conditionList.addAll(parentConditions);
			}
		}

		final SearchConditionData supercategoriesCondition = createSearchConditions(ProductModel.SUPERCATEGORIES, null,
				ValueComparisonOperator.IS_EMPTY);
		conditionList.add(supercategoriesCondition);
		searchData.addConditionList(ValueComparisonOperator.AND, conditionList);
	}

	public List<SearchConditionAdapter> getConditionsAdapters()
	{
		return conditionsAdapters;
	}

	@Required
	public void setConditionsAdapters(final List<SearchConditionAdapter> conditionsAdapters)
	{
		this.conditionsAdapters = conditionsAdapters;
	}
}
