/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousingbackoffice.widgets.returns;

import de.hybris.platform.basecommerce.enums.ReturnStatus;
import de.hybris.platform.returns.model.ReturnRequestModel;

import java.util.Arrays;
import java.util.Optional;

import com.hybris.backoffice.navigation.NavigationNode;
import com.hybris.backoffice.widgets.advancedsearch.AbstractInitAdvancedSearchAdapter;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.backoffice.widgets.advancedsearch.impl.SearchConditionData;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.advancedsearch.FieldType;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;
import org.apache.commons.collections.CollectionUtils;


/**
 * Filter to display returns that are in {@link ReturnStatus#WAIT} status. Used in Waiting for Goods
 * Returns treenode.
 */
public class WaitingForGoodsFilterController extends AbstractInitAdvancedSearchAdapter
{
	protected static final String NAVIGATION_NODE_ID = "warehousing.typenode.return.waiting.goods";
	protected static final ReturnStatus FILTER_STATUS = ReturnStatus.WAIT;

	private transient AdvancedSearchData advancedSearchData;

	@Override
	public void addSearchDataConditions(final AdvancedSearchData searchData, final Optional<NavigationNode> navigationNode)
	{
		if (searchData != null)
		{
			this.advancedSearchData = searchData;

			if (CollectionUtils.isNotEmpty(searchData.getConditions(ReturnRequestModel.STATUS)))
			{
				searchData.getConditions(ReturnRequestModel.STATUS).clear();
			}

			final FieldType returnStatusFieldType = new FieldType();
			returnStatusFieldType.setDisabled(Boolean.FALSE);
			returnStatusFieldType.setSelected(Boolean.TRUE);
			returnStatusFieldType.setName(ReturnRequestModel.STATUS);

			final SearchConditionData returnStatusSearchCondition = new SearchConditionData(returnStatusFieldType, FILTER_STATUS,
					ValueComparisonOperator.EQUALS);

			searchData.addConditionList(ValueComparisonOperator.OR, Arrays.asList(returnStatusSearchCondition));
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
		return ReturnRequestModel._TYPECODE;
	}

	public AdvancedSearchData getAdvancedSearchData()
	{
		return advancedSearchData;
	}

}
