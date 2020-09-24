/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.omsbackoffice.widgets.order.filter;

import de.hybris.platform.core.model.order.OrderModel;

import java.util.Optional;

import com.hybris.backoffice.navigation.NavigationNode;
import com.hybris.backoffice.widgets.advancedsearch.AbstractInitAdvancedSearchAdapter;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;


/**
 * Adds conditions to the advanced search widget to filter for all {@link OrderModel}.
 */
public class AllOrdersFilterController extends AbstractInitAdvancedSearchAdapter
{
	public static final String NAVIGATION_NODE_ID = "customersupportbackoffice.typenode.order.all";

	@Override
	public void addSearchDataConditions(final AdvancedSearchData searchData, final Optional<NavigationNode> navigationNode)
	{
		// No need for search condition simply need all of the Orders available
	}

	@Override
	public String getTypeCode()
	{
		return OrderModel._TYPECODE;
	}

	@Override
	public String getNavigationNodeId()
	{
		return NAVIGATION_NODE_ID;
	}
}

