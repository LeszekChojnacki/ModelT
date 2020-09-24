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

import de.hybris.platform.catalog.model.classification.ClassificationSystemVersionModel;
import de.hybris.platform.core.PK;

import org.junit.Before;
import org.junit.Test;

import com.hybris.backoffice.navigation.NavigationNode;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchData;
import com.hybris.backoffice.widgets.advancedsearch.impl.SearchConditionData;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;


public class SolrSearchClassificationSystemVersionConditionAdapterTest
{

	public static final String CLASSIFICATION_SYSTEM_VERSION_PROPERTY_NAME = "classificationSystemVersion";

	private SolrSearchClassificationSystemVersionConditionAdapter solrSearchClassificationSystemVersionConditionAdapter;

	@Before
	public void setup()
	{
		solrSearchClassificationSystemVersionConditionAdapter = new SolrSearchClassificationSystemVersionConditionAdapter();
		solrSearchClassificationSystemVersionConditionAdapter.setOperator(ValueComparisonOperator.EQUALS);
		solrSearchClassificationSystemVersionConditionAdapter.setClassificationSystemVersionPropertyName(CLASSIFICATION_SYSTEM_VERSION_PROPERTY_NAME);
	}

	@Test
	public void shouldAddCatalogCondition()
	{
		// given
		final AdvancedSearchData searchData = new AdvancedSearchData();
		final NavigationNode navigationNode = mock(NavigationNode.class);
		final ClassificationSystemVersionModel classificationSystemVersion = mock(ClassificationSystemVersionModel.class);
		final PK classificationSystemPK = PK.BIG_PK;

		given(navigationNode.getData()).willReturn(classificationSystemVersion);
		given(classificationSystemVersion.getPk()).willReturn(classificationSystemPK);

		// when
		solrSearchClassificationSystemVersionConditionAdapter.addSearchCondition(searchData, navigationNode);

		// then
		final SearchConditionData searchConditionData = searchData.getCondition(0);
		assertThat(searchConditionData.getFieldType().getName()).isEqualTo(CLASSIFICATION_SYSTEM_VERSION_PROPERTY_NAME);
		assertThat(searchConditionData.getValue()).isEqualTo(classificationSystemPK);
		assertThat(searchConditionData.getOperator()).isEqualTo(ValueComparisonOperator.EQUALS);
	}
}
