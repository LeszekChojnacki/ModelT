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

import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.navigation.NavigationNode;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.backoffice.widgets.advancedsearch.impl.SearchConditionData;
import com.hybris.backoffice.widgets.searchadapters.conditions.SearchConditionAdapter;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;


/**
 * {@link SearchConditionAdapter} responsible for handling node representing catalog. The handler gets all catalog's
 * versions of given catalog and add them as a condition to {@link AdvancedSearchData}.
 */
public class FlexibleSearchCatalogConditionAdapter extends SearchConditionAdapter
{
	private static final long NON_EXISTING_CATALOG_VERSION_PK = 0L;

	private String catalogVersionPropertyName;
	private ValueComparisonOperator operator;

	@Override
	public boolean canHandle(final NavigationNode node)
	{
		return node.getData() instanceof CatalogModel;
	}

	@Override
	public void addSearchCondition(final AdvancedSearchData searchData, final NavigationNode node)
	{
		final CatalogModel catalog = (CatalogModel) node.getData();
		final Collection<CatalogVersionModel> catalogVersions = catalog.getCatalogVersions();
		if (catalogVersions.isEmpty())
		{
			final SearchConditionData excludingAllCondition = createSearchConditions(catalogVersionPropertyName,
					NON_EXISTING_CATALOG_VERSION_PK, operator);
			searchData.addCondition(excludingAllCondition.getFieldType(), excludingAllCondition.getOperator(),
					excludingAllCondition.getValue());
		}
		else
		{
			final List<SearchConditionData> conditions = catalogVersions.stream()
					.map(catalogVersion -> createSearchConditions(catalogVersionPropertyName, catalogVersion.getPk(), operator))
					.collect(Collectors.toList());
			searchData.addConditionList(ValueComparisonOperator.OR, conditions);
		}
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
