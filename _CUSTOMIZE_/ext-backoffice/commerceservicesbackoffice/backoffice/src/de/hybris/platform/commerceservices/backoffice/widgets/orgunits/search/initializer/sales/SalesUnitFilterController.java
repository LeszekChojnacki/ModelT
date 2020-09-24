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
package de.hybris.platform.commerceservices.backoffice.widgets.orgunits.search.initializer.sales;

import de.hybris.platform.commerceservices.model.OrgUnitModel;

import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;

import com.hybris.backoffice.navigation.NavigationNode;
import com.hybris.backoffice.widgets.advancedsearch.AbstractInitAdvancedSearchAdapter;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.advancedsearch.FieldType;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;


/**
 * Adds conditions for filtering Sales Units
 */
public class SalesUnitFilterController extends AbstractInitAdvancedSearchAdapter
{
	protected static final String NAVIGATION_NODE_ID = "organization.typenode.units.sales";

	/**
	 * @deprecated Since 6.6. Use {@link #addSearchDataConditions(AdvancedSearchData, Optional)} instead.
	 */
	@Deprecated
	public void addSearchDataConditions(final AdvancedSearchData searchData)
	{
		this.addSearchDataConditions(searchData, Optional.empty());
	}

	@Override
	public void addSearchDataConditions(final AdvancedSearchData searchData, final Optional<NavigationNode> navigationNode)
	{
		if (searchData != null)
		{
			if (CollectionUtils.isNotEmpty(searchData.getConditions(OrgUnitModel.SUPPLIER)))
			{
				// clear existing search data
				searchData.getConditions(OrgUnitModel.SUPPLIER).clear();
			}

			final FieldType statusFieldType = new FieldType();
			statusFieldType.setDisabled(Boolean.FALSE);
			statusFieldType.setSelected(Boolean.TRUE);
			statusFieldType.setName(OrgUnitModel.SUPPLIER);

			searchData.addCondition(statusFieldType, ValueComparisonOperator.EQUALS, Boolean.TRUE);
		}
	}

	@Override
	public String getNavigationNodeId()
	{
		return NAVIGATION_NODE_ID;
	}

	@Override
	public String getTypeCode()
	{
		return OrgUnitModel._TYPECODE;
	}
}
