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
package com.hybris.backoffice.solrsearch.converters.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.hybris.platform.solrfacetsearch.config.FacetType;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.search.Breadcrumb;
import de.hybris.platform.solrfacetsearch.search.Facet;
import de.hybris.platform.solrfacetsearch.search.FacetValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.hybris.backoffice.solrsearch.converters.FullTextSearchDataConverter;
import com.hybris.cockpitng.search.data.facet.FacetData;
import com.hybris.cockpitng.search.data.facet.FacetValueData;


public class DefaultFullTextSearchDataConverterTest
{
	public static final String FACET_CATEGORY = "category";
	public static final String FACET_PRICE = "price";
	public static final String FACET_BREADCRUMB = "breadcrumbFacet";
	public static final String VAL_BREADCRUMB = "breadcrumbValue";
	public static final String VAL_SHOES = "shoes";
	public static final String VAL_SHIRTS = "shirts";
	public static final String VAL_PRICE_100 = "100";
	public static final String VAL_PRICE_200 = "200";
	@Mock
	private IndexedType indexedType;
	private FullTextSearchDataConverter converter;

	@Before
	public void setUp() throws Exception
	{
		MockitoAnnotations.initMocks(this);
		converter = new DefaultFullTextSearchDataConverter();
		final Map<String, IndexedProperty> indexedPropertyMap = mock(Map.class);
		when(indexedType.getIndexedProperties()).thenReturn(indexedPropertyMap);
		when(indexedPropertyMap.get(anyString())).thenAnswer(invocationOnMock -> {
			final String name = (String) invocationOnMock.getArguments()[0];
			final IndexedProperty indexedProperty = new IndexedProperty();
			indexedProperty.setName(name);
			indexedProperty.setBackofficeDisplayName(name);
			indexedProperty.setFacetType(FacetType.MULTISELECTAND);
			return indexedProperty;
		});
	}

	@Test
	public void convertFacetsWithNotDuplicatedBreadcrumbs() throws Exception
	{
		final List<Facet> facets = new ArrayList<>();
		facets.add(new Facet(FACET_CATEGORY,//
				Lists.newArrayList(new FacetValue(VAL_SHOES, 20, true), new FacetValue(VAL_SHIRTS, 10, false))));
		facets.add(new Facet(FACET_PRICE,//
				Lists.newArrayList(new FacetValue(VAL_PRICE_100, 20, true), new FacetValue(VAL_PRICE_200, 10, false))));

		final List<Breadcrumb> breadcrumbs = new ArrayList<>();
		breadcrumbs.add(createBreadcrumb(FACET_BREADCRUMB, VAL_BREADCRUMB));

		final Collection<FacetData> convertFacets = converter.convertFacets(facets, breadcrumbs, indexedType);
		assertThat(convertFacets).hasSize(3);

		final Map<String, FacetData> facetDataMap = convertFacets.stream().collect(Collectors.toMap(FacetData::getName, fd -> fd));
		assertThat(facetDataMap.get(FACET_CATEGORY)).isNotNull();
		assertThat(facetDataMap.get(FACET_PRICE)).isNotNull();
		assertThat(facetDataMap.get(FACET_BREADCRUMB)).isNotNull();

		assertThat(facetDataMap.get(FACET_CATEGORY).getFacetValues()).hasSize(2);
		assertThat(facetDataMap.get(FACET_CATEGORY).getFacetValue(VAL_SHOES).isSelected()).isTrue();
		assertThat(facetDataMap.get(FACET_CATEGORY).getFacetValue(VAL_SHOES).getCount()).isEqualTo(20);
		assertThat(facetDataMap.get(FACET_CATEGORY).getFacetValue(VAL_SHIRTS).isSelected()).isFalse();
		assertThat(facetDataMap.get(FACET_CATEGORY).getFacetValue(VAL_SHIRTS).getCount()).isEqualTo(10);

		assertThat(facetDataMap.get(FACET_PRICE).getFacetValues()).hasSize(2);
		assertThat(facetDataMap.get(FACET_PRICE).getFacetValue(VAL_PRICE_100).isSelected()).isTrue();
		assertThat(facetDataMap.get(FACET_PRICE).getFacetValue(VAL_PRICE_100).getCount()).isEqualTo(20);
		assertThat(facetDataMap.get(FACET_PRICE).getFacetValue(VAL_PRICE_200).isSelected()).isFalse();
		assertThat(facetDataMap.get(FACET_PRICE).getFacetValue(VAL_PRICE_200).getCount()).isEqualTo(10);

		assertThat(facetDataMap.get(FACET_BREADCRUMB).getFacetValues()).hasSize(1);
		assertThat(facetDataMap.get(FACET_BREADCRUMB).getFacetValue(VAL_BREADCRUMB).isSelected()).isTrue();
		assertThat(facetDataMap.get(FACET_BREADCRUMB).getFacetValue(VAL_BREADCRUMB).getCount()).isEqualTo(0);
	}

	@Test
	public void convertFacetsWithBreadcrumbForExistingFacet() throws Exception
	{
		final List<Facet> facets = new ArrayList<>();
		facets.add(new Facet(FACET_CATEGORY,//
				Lists.newArrayList(new FacetValue(VAL_SHOES, 20, true), new FacetValue(VAL_SHIRTS, 10, false))));
		facets.add(new Facet(FACET_PRICE,//
				Lists.newArrayList(new FacetValue(VAL_PRICE_100, 20, true), new FacetValue(VAL_PRICE_200, 10, false), new FacetValue(
						VAL_BREADCRUMB, 5, false))));

		final List<Breadcrumb> breadcrumbs = new ArrayList<>();
		breadcrumbs.add(createBreadcrumb(FACET_CATEGORY, VAL_BREADCRUMB));
		breadcrumbs.add(createBreadcrumb(FACET_PRICE, VAL_BREADCRUMB));

		final Collection<FacetData> convertFacets = converter.convertFacets(facets, breadcrumbs, indexedType);
		assertThat(convertFacets).hasSize(2);

		final Map<String, FacetData> facetDataMap = convertFacets.stream().collect(Collectors.toMap(FacetData::getName, fd -> fd));
		assertThat(facetDataMap.get(FACET_CATEGORY)).isNotNull();
		assertThat(facetDataMap.get(FACET_PRICE)).isNotNull();

		assertThat(facetDataMap.get(FACET_CATEGORY).getFacetValues()).hasSize(3);
		assertThat(facetDataMap.get(FACET_CATEGORY).getFacetValue(VAL_BREADCRUMB).isSelected()).isTrue();
		assertThat(facetDataMap.get(FACET_CATEGORY).getFacetValue(VAL_BREADCRUMB).getCount()).isEqualTo(FacetValueData.BREADCRUMB_COUNT);

		assertThat(facetDataMap.get(FACET_PRICE).getFacetValues()).hasSize(3);
		assertThat(facetDataMap.get(FACET_PRICE).getFacetValue(VAL_BREADCRUMB).isSelected()).isTrue();
		assertThat(facetDataMap.get(FACET_PRICE).getFacetValue(VAL_BREADCRUMB).getCount()).isEqualTo(5);
	}

	@Test
	public void shouldKeepFacetsOrder()
	{
		// given
		final List<Facet> facets = new ArrayList<>();
		facets.add(new Facet(FACET_CATEGORY, Collections.singletonList(new FacetValue(VAL_SHOES, 20, false))));
		facets.add(new Facet(FACET_PRICE,Collections.singletonList(new FacetValue(VAL_PRICE_100, 20, false))));
		facets.add(new Facet(FACET_BREADCRUMB,Collections.singletonList(new FacetValue(VAL_PRICE_100, 20, false))));

		final List<Breadcrumb> breadcrumbs = new ArrayList<>();
		breadcrumbs.add(createBreadcrumb(FACET_CATEGORY, VAL_BREADCRUMB));
		breadcrumbs.add(createBreadcrumb(FACET_PRICE, VAL_BREADCRUMB));
		breadcrumbs.add(createBreadcrumb(FACET_BREADCRUMB, VAL_BREADCRUMB));

		// when
		final Collection<FacetData> facetData = converter.convertFacets(facets, breadcrumbs, indexedType);

		// then
		Assertions.assertThat(facetData).extracting(FacetData::getName).containsExactly(FACET_CATEGORY, FACET_PRICE, FACET_BREADCRUMB);
	}

	public Breadcrumb createBreadcrumb(final String fieldName, final String value)
	{
		// breadcrumb has protected constructor, that's why it has to be mocked
		final Breadcrumb breadcrumb = mock(Breadcrumb.class);
		when(breadcrumb.getFieldName()).thenReturn(fieldName);
		when(breadcrumb.getValue()).thenReturn(value);
		when(breadcrumb.getDisplayValue()).thenReturn(value);
		return breadcrumb;
	}
}
