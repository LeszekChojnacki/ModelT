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
package com.hybris.backoffice.widgets.searchadapters;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.navigation.NavigationNode;
import com.hybris.backoffice.widgets.advancedsearch.AdvancedSearchInitializer;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.backoffice.widgets.searchadapters.conditions.SearchConditionAdapter;


/**
 * Advanced search initialized responsible for translating object properties (connected with catalogs) to flexible
 * search properties.
 */
public class CatalogTreeFilterAdvancedSearchInitializer implements AdvancedSearchInitializer
{

	private List<SearchConditionAdapter> conditionsAdapters;

	@Override
	public void addSearchDataConditions(final AdvancedSearchData searchData, final Optional<NavigationNode> navigationNode)
	{
		if (navigationNode.isPresent())
		{
			final NavigationNode node = navigationNode.get();
			conditionsAdapters.stream().filter(adapter -> adapter.canHandle(node)).findFirst()
					.ifPresent(adapter -> adapter.addSearchCondition(searchData, node));
		}
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
