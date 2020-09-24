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
package com.hybris.backoffice.solrsearch.adapters.conditions.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.core.PK;

import org.junit.Before;
import org.junit.Test;

import com.hybris.backoffice.navigation.NavigationNode;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.backoffice.widgets.advancedsearch.impl.SearchConditionData;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;


public class SolrSearchCatalogConditionAdapterTest
{

	public static final String CATALOG_PROPERTY_NAME = "catalogPK";

	private SolrSearchCatalogConditionAdapter solrSearchCatalogConditionAdapter;

	@Before
	public void setup()
	{
		solrSearchCatalogConditionAdapter = new SolrSearchCatalogConditionAdapter();
		solrSearchCatalogConditionAdapter.setOperator(ValueComparisonOperator.EQUALS);
		solrSearchCatalogConditionAdapter.setCatalogPropertyName(CATALOG_PROPERTY_NAME);
	}

	@Test
	public void shouldAddCatalogCondition()
	{
		// given
		final AdvancedSearchData searchData = new AdvancedSearchData();
		final NavigationNode navigationNode = mock(NavigationNode.class);
		final CatalogModel catalog = mock(CatalogModel.class);
		final PK catalogPK = PK.BIG_PK;

		given(navigationNode.getData()).willReturn(catalog);
		given(catalog.getPk()).willReturn(catalogPK);

		// when
		solrSearchCatalogConditionAdapter.addSearchCondition(searchData, navigationNode);

		// then
		final SearchConditionData searchConditionData = searchData.getCondition(0);
		assertThat(searchConditionData.getFieldType().getName()).isEqualTo(CATALOG_PROPERTY_NAME);
		assertThat(searchConditionData.getValue()).isEqualTo(catalogPK);
		assertThat(searchConditionData.getOperator()).isEqualTo(ValueComparisonOperator.EQUALS);
	}
}
