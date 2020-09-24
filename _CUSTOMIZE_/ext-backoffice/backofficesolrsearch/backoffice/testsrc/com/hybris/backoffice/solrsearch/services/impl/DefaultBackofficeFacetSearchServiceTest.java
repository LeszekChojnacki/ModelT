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
package com.hybris.backoffice.solrsearch.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import de.hybris.platform.catalog.CatalogTypeService;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import com.hybris.backoffice.solrsearch.constants.BackofficesolrsearchConstants;
import com.hybris.backoffice.solrsearch.converters.SearchConditionDataConverter;
import com.hybris.backoffice.solrsearch.converters.impl.DefaultSearchQueryConditionsConverter;
import com.hybris.backoffice.solrsearch.dataaccess.SearchConditionData;
import com.hybris.backoffice.solrsearch.dataaccess.SolrSearchCondition;
import com.hybris.cockpitng.search.data.SearchQueryData;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;


public class DefaultBackofficeFacetSearchServiceTest
{

	public static final String CUSTOM_CV_QUALIFIER = "_custom_cv_qualifier";
	public static final String BACKOFFICE_PRODUCT = "BackofficeProduct";
	public static final long STAGED_PK_LONG = 1L;
	public static final long ONLINE_PK_LONG = 2L;
	public static final int IMPOSSIBLE_PK = -1;
	private static final String CATALOG_VERSION_PROPERTY = "catalogVersionPk";

	@Spy
	@InjectMocks
	private DefaultBackofficeFacetSearchService service;

	@Mock
	private IndexedType indexedType;
	@Mock
	private ComposedTypeModel catalogAwareType;
	@Mock
	private ComposedTypeModel catalogUnawareType;
	@Mock
	private CatalogTypeService catalogTypeService;
	@Mock
	private SearchConditionData searchConditionData;
	@Mock
	private SearchQueryData queryData;
	@Mock
	private CatalogVersionModel staged;
	@Mock
	private CatalogVersionModel online;
	@Mock
	private UserService userService;
	@Mock
	private DefaultSearchQueryConditionsConverter searchQueryConditionsConverter;
	@Mock
	private SearchConditionDataConverter searchConditionDataConverter;
	@Mock
	private TypeService typeService;
	@Mock
	private CatalogVersionService catalogVersionService;

	private Collection<CatalogVersionModel> readableCatalogVersions;


	@Before
	public void setUp()
	{
		initMocks(this);
		when(indexedType.getIdentifier()).thenReturn(BACKOFFICE_PRODUCT);
		when(catalogTypeService.isCatalogVersionAwareType(catalogAwareType)).thenReturn(Boolean.TRUE);
		when(catalogTypeService.isCatalogVersionAwareType(catalogUnawareType)).thenReturn(Boolean.FALSE);
		service.setIndexedTypeToCatalogVersionPropertyMapping(new HashMap<>());
		readableCatalogVersions = new ArrayList<>();
		readableCatalogVersions.add(staged);
		readableCatalogVersions.add(online);
		final PK stagedPK = PK.fromLong(STAGED_PK_LONG);
		when(staged.getPk()).thenReturn(stagedPK);
		final PK onlinePK = PK.fromLong(ONLINE_PK_LONG);
		when(online.getPk()).thenReturn(onlinePK);
	}

	@Test
	public void isCatalogVersionAwareCatalogUnawareType()
	{
		when(indexedType.getComposedType()).thenReturn(catalogUnawareType);
		when(catalogTypeService.isCatalogVersionAwareType(catalogUnawareType)).thenReturn(Boolean.FALSE);

		assertThat(service.isCatalogVersionAware(indexedType)).isFalse();
	}

	@Test
	public void isCatalogVersionAwareCustomCVQualifier()
	{

		final HashMap<String, IndexedProperty> indexedProperties = new HashMap<>();
		indexedProperties.put(CUSTOM_CV_QUALIFIER, null);

		when(indexedType.getIndexedProperties()).thenReturn(indexedProperties);
		when(indexedType.getComposedType()).thenReturn(catalogAwareType);

		final HashMap<String, String> typeToCatalogVersionPropertyMapping = new HashMap<>();
		typeToCatalogVersionPropertyMapping.put(BACKOFFICE_PRODUCT, CUSTOM_CV_QUALIFIER);
		service.setIndexedTypeToCatalogVersionPropertyMapping(typeToCatalogVersionPropertyMapping);

		assertThat(service.isCatalogVersionAware(indexedType)).isTrue();
	}

	@Test
	public void isCatalogVersionAwareDefaultCVQualifier()
	{
		when(indexedType.getComposedType()).thenReturn(catalogAwareType);
		final HashMap<String, IndexedProperty> indexedProperties = new HashMap<>();
		indexedProperties.put(BackofficesolrsearchConstants.CATALOG_VERSION_PK, null);
		when(indexedType.getIndexedProperties()).thenReturn(indexedProperties);

		assertThat(service.isCatalogVersionAware(indexedType)).isTrue();
	}

	@Test
	public void isCatalogVersionAwareNoIndexedProperties()
	{
		when(indexedType.getComposedType()).thenReturn(catalogAwareType);
		final HashMap<String, IndexedProperty> indexedProperties = new HashMap<>();
		when(indexedType.getIndexedProperties()).thenReturn(indexedProperties);

		assertThat(service.isCatalogVersionAware(indexedType)).isFalse();
	}

	@Test
	public void prepareCatalogVersionConditionNoReadableCVs()
	{
		when(indexedType.getComposedType()).thenReturn(catalogAwareType);
		service.prepareCatalogVersionCondition(indexedType, searchConditionData, queryData, new ArrayList<>());

		final ArgumentCaptor<SolrSearchCondition> captor = ArgumentCaptor.forClass(SolrSearchCondition.class);

		verify(searchConditionData).addFilterQueryCondition(captor.capture());

		final SolrSearchCondition effectiveCondition = captor.getValue();
		final List<SolrSearchCondition.ConditionValue> conditionValues = effectiveCondition.getConditionValues();
		assertThat(conditionValues).hasSize(1);
		assertThat(conditionValues.get(0).getValue()).isEqualTo(IMPOSSIBLE_PK);
	}

	@Test
	public void prepareCatalogVersionConditionTwoReadableCVs()
	{
		//given
		when(indexedType.getComposedType()).thenReturn(catalogAwareType);
		final ArgumentCaptor<SolrSearchCondition> captor = ArgumentCaptor.forClass(SolrSearchCondition.class);

		//when
		service.prepareCatalogVersionCondition(indexedType, searchConditionData, queryData, readableCatalogVersions);

		//then
		verify(searchConditionData).addFilterQueryCondition(captor.capture());

		final SolrSearchCondition effectiveCondition = captor.getValue();
		final List<SolrSearchCondition.ConditionValue> conditionValues = effectiveCondition.getConditionValues();
		assertThat(conditionValues).hasSize(2);
		assertThat(conditionValues.stream().map(SolrSearchCondition.ConditionValue::getValue).collect(Collectors.toList()))
				.contains(STAGED_PK_LONG, ONLINE_PK_LONG);
	}

	@Test
	public void shouldAddCatalogVersionConditionWhenUserIsNotAdmin()
	{
		//given
		when(indexedType.getComposedType()).thenReturn(catalogAwareType);
		when(queryData.getConditions()).thenReturn(Lists.emptyList());
		when(queryData.getSearchType()).thenReturn(StringUtils.EMPTY);
		when(searchConditionDataConverter.convertConditions(any(), any())).thenReturn(searchConditionData);

		final Map<String, IndexedProperty> indexedProperties = new HashMap<>();
		indexedProperties.put(CATALOG_VERSION_PROPERTY, mock(IndexedProperty.class));
		when(indexedType.getIndexedProperties()).thenReturn(indexedProperties);

		final List<CatalogVersionModel> readableCatalogVersions = Lists.newArrayList(staged, online);
		when(catalogVersionService.getAllReadableCatalogVersions(any())).thenReturn(readableCatalogVersions);

		final ArgumentCaptor<SolrSearchCondition> captor = ArgumentCaptor.forClass(SolrSearchCondition.class);

		//when
		service.prepareSearchConditionData(queryData, indexedType);

		//then
		verify(searchConditionData).addFilterQueryCondition(captor.capture());

		final SolrSearchCondition effectiveCondition = captor.getValue();
		final List<SolrSearchCondition.ConditionValue> conditionValues = effectiveCondition.getConditionValues();

		assertThat(conditionValues).hasSize(2);
		assertThat(conditionValues.stream().map(SolrSearchCondition.ConditionValue::getValue).collect(Collectors.toList()))
				.contains(STAGED_PK_LONG, ONLINE_PK_LONG);
	}

	@Test
	public void shouldNotAddCatalogVersionConditionWhenUserIsAdmin()
	{
		//given
		when(indexedType.getComposedType()).thenReturn(catalogAwareType);
		when(userService.isAdmin(any())).thenReturn(true);
		when(queryData.getConditions()).thenReturn(Lists.emptyList());
		when(queryData.getSearchType()).thenReturn(StringUtils.EMPTY);
		when(searchConditionDataConverter.convertConditions(any(), any())).thenReturn(searchConditionData);

		final Map<String, IndexedProperty> indexedProperties = new HashMap<>();
		indexedProperties.put(CATALOG_VERSION_PROPERTY, mock(IndexedProperty.class));
		when(indexedType.getIndexedProperties()).thenReturn(indexedProperties);

		final List<CatalogVersionModel> readableCatalogVersions = Collections.singletonList(staged);
		when(catalogVersionService.getAllReadableCatalogVersions(any())).thenReturn(readableCatalogVersions);

		//when
		final SearchConditionData searchCondition = service.prepareSearchConditionData(queryData, indexedType);

		//then
		assertThat(searchCondition.getFilterQueryConditions()).isEmpty();
	}

	@Test
	public void shouldAddTypeConditionWhenItemtypeIsIndexed()
	{
		final Map<String, IndexedProperty> indexedProperties = Collections.singletonMap(ComposedTypeModel.ITEMTYPE, null);
		when(indexedType.getIndexedProperties()).thenReturn(indexedProperties);
		when(queryData.getSearchType()).thenReturn(StringUtils.EMPTY);
		when(typeService.getComposedTypeForCode(StringUtils.EMPTY)).thenReturn(null);
		when(queryData.isIncludeSubtypes()).thenReturn(Boolean.FALSE);

		final Optional<SolrSearchCondition> condition = service.prepareTypeCondition(indexedType, queryData);

		assertThat(condition).isPresent();
		assertThat(condition.get().getAttributeName()).isEqualTo(ComposedTypeModel.ITEMTYPE);
		assertThat(condition.get().getConditionValues().get(0).getComparisonOperator()).isEqualTo(ValueComparisonOperator.EQUALS);
	}

	@Test
	public void shouldNotAddTypeConditionWhenItemtypeIsNotIndexed()
	{
		final Map<String, IndexedProperty> indexedProperties = Collections.singletonMap(CUSTOM_CV_QUALIFIER, null);
		when(indexedType.getIndexedProperties()).thenReturn(indexedProperties);

		final Optional<SolrSearchCondition> condition = service.prepareTypeCondition(indexedType, queryData);

		assertThat(condition).isNotPresent();
	}

}
