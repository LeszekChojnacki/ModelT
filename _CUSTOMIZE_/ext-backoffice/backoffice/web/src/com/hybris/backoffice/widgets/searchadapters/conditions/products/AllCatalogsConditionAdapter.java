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

import org.apache.commons.lang3.StringUtils;

import com.hybris.backoffice.navigation.NavigationNode;
import com.hybris.backoffice.tree.model.CatalogTreeModelPopulator;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.backoffice.widgets.searchadapters.conditions.SearchConditionAdapter;


/**
 * {@link SearchConditionAdapter} responsible for handling AllCatalogs node.
 */
public class AllCatalogsConditionAdapter extends SearchConditionAdapter
{

	@Override
	public boolean canHandle(final NavigationNode node)
	{
		return StringUtils.endsWith(node.getId(), CatalogTreeModelPopulator.ALL_CATALOGS_NODE_ID);
	}

	@Override
	public void addSearchCondition(final AdvancedSearchData searchData, final NavigationNode node)
	{
		// do nothing. allCatalogs shouldn't add any conditions
	}
}
