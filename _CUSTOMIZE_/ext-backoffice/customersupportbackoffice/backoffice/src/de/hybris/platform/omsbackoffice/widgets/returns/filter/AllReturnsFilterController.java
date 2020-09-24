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
package de.hybris.platform.omsbackoffice.widgets.returns.filter;

import com.hybris.backoffice.navigation.NavigationNode;
import de.hybris.platform.returns.model.ReturnRequestModel;

import com.hybris.backoffice.widgets.advancedsearch.AbstractInitAdvancedSearchAdapter;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;

import java.util.Optional;

/**
 * Adds conditions to the advanced search widget to filter for all {@link ReturnRequestModel}.
 */
public class AllReturnsFilterController extends AbstractInitAdvancedSearchAdapter
{
	public static final String NAVIGATION_NODE_ID = "customersupportbackoffice.typenode.all.returns";

	@Override
	public void addSearchDataConditions(final AdvancedSearchData searchData, final Optional<NavigationNode> navigationNode)
	{
		// No need for search condition simply need all of the Returns available
	}

	@Override
	public String getTypeCode()
	{
		return ReturnRequestModel._TYPECODE;
	}

	@Override
	public String getNavigationNodeId()
	{
		return NAVIGATION_NODE_ID;
	}
}

