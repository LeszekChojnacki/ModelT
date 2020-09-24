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
/**
 *
 */
package de.hybris.platform.adaptivesearchbackoffice.editors.facets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.adaptivesearch.data.AbstractAsFacetConfiguration;
import de.hybris.platform.adaptivesearch.data.AsConfigurationHolder;
import de.hybris.platform.adaptivesearch.data.AsFacet;
import de.hybris.platform.adaptivesearch.data.AsFacetData;
import de.hybris.platform.adaptivesearch.data.AsFacetValueData;
import de.hybris.platform.adaptivesearch.data.AsFacetVisibility;
import de.hybris.platform.adaptivesearch.data.AsSearchResultData;
import de.hybris.platform.adaptivesearch.model.AsFacetModel;
import de.hybris.platform.adaptivesearchbackoffice.common.AsFacetUtils;
import de.hybris.platform.adaptivesearchbackoffice.data.FacetEditorData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchResultData;
import de.hybris.platform.adaptivesearchbackoffice.editors.configurablemultireference.AbstractDataHandlerTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zkoss.zul.ListModel;


@UnitTest
public class AsFacetsDataHandlerTest extends AbstractDataHandlerTest
{
	private static final String PRIORITY_ATTRIBUTE = "priority";

	private static final String UID_1 = "uid1";
	private static final String INDEX_PROPERTY_1 = "property1";
	private static final Integer PRIORITY_1 = Integer.valueOf(1);

	private static final String UID_2 = "uid2";
	private static final String INDEX_PROPERTY_2 = "property2";
	private static final Integer PRIORITY_2 = Integer.valueOf(2);

	private AsFacetsDataHandler asFacetsDataHandler;

	@Mock
	private AsFacetUtils asFacetUtils;

	@Before
	public void setUp() throws Exception
	{
		MockitoAnnotations.initMocks(this);

		asFacetsDataHandler = new AsFacetsDataHandler();
		asFacetsDataHandler.setAsFacetUtils(asFacetUtils);
	}

	@Test
	public void getTypeCode() throws Exception
	{
		// when
		final String type = asFacetsDataHandler.getTypeCode();

		// then
		assertEquals(type, AsFacetModel._TYPECODE);
	}

	@Test
	public void loadNullData()
	{
		// when
		final ListModel<FacetEditorData> listModel = asFacetsDataHandler.loadData(null, null, null);

		// then
		assertEquals(0, listModel.getSize());
	}

	@Test
	public void loadEmptyData()
	{
		// given
		final SearchResultData searchResultData = new SearchResultData();
		searchResultData.setAsSearchResult(new AsSearchResultData());

		// when
		final ListModel<FacetEditorData> listModel = asFacetsDataHandler.loadData(new ArrayList<>(), searchResultData,
				new HashMap<>());

		// then
		assertEquals(0, listModel.getSize());
	}

	@Test
	public void loadDataFromInitialValue() throws Exception
	{
		// given
		final AsFacetModel facetModel1 = createFacetModel1();
		final AsFacetModel facetModel2 = createFacetModel2();
		final List<AsFacetModel> initialValue = Arrays.asList(facetModel1, facetModel2);

		// when
		final ListModel<FacetEditorData> listModel = asFacetsDataHandler.loadData(initialValue, null, new HashMap<>());

		// then
		assertEquals(2, listModel.getSize());

		final FacetEditorData facet1 = listModel.getElementAt(0);
		assertEquals(UID_2, facet1.getUid());
		assertEquals(INDEX_PROPERTY_2, facet1.getIndexProperty());
		assertEquals(PRIORITY_2, facet1.getPriority());
		assertFalse(facet1.isOpen());
		assertFalse(facet1.isOverride());
		assertFalse(facet1.isInSearchResult());
		assertSame(facetModel2, facet1.getModel());

		final FacetEditorData facet2 = listModel.getElementAt(1);
		assertEquals(UID_1, facet2.getUid());
		assertEquals(INDEX_PROPERTY_1, facet2.getIndexProperty());
		assertEquals(PRIORITY_1, facet2.getPriority());
		assertFalse(facet2.isOpen());
		assertFalse(facet2.isOverride());
		assertFalse(facet2.isInSearchResult());
		assertSame(facetModel1, facet2.getModel());
	}

	@Test
	public void loadDataFromSearchResult() throws Exception
	{
		// given
		final Map<String, AsConfigurationHolder<AsFacet, AbstractAsFacetConfiguration>> searchProfileResultFacets = new LinkedHashMap<>();
		searchProfileResultFacets.put(INDEX_PROPERTY_1, createConfigurationHolder(createFacetData1()));
		searchProfileResultFacets.put(INDEX_PROPERTY_2, createConfigurationHolder(createFacetData2()));

		final SearchResultData searchResult = createSearchResult();
		final AsSearchResultData asSearchResult = searchResult.getAsSearchResult();
		asSearchResult.getSearchProfileResult().setFacets(searchProfileResultFacets);

		// when
		final ListModel<FacetEditorData> listModel = asFacetsDataHandler.loadData(null, searchResult, new HashMap<>());

		// then
		assertEquals(2, listModel.getSize());

		final FacetEditorData facet1 = listModel.getElementAt(0);
		assertEquals(UID_2, facet1.getUid());
		assertEquals(INDEX_PROPERTY_2, facet1.getIndexProperty());
		assertEquals(PRIORITY_2, facet1.getPriority());
		assertFalse(facet1.isOpen());
		assertFalse(facet1.isOverride());
		assertFalse(facet1.isInSearchResult());
		assertNull(facet1.getModel());

		final FacetEditorData facet2 = listModel.getElementAt(1);
		assertEquals(UID_1, facet2.getUid());
		assertEquals(INDEX_PROPERTY_1, facet2.getIndexProperty());
		assertEquals(PRIORITY_1, facet2.getPriority());
		assertFalse(facet2.isOpen());
		assertFalse(facet2.isOverride());
		assertFalse(facet2.isInSearchResult());
		assertNull(facet2.getModel());
	}

	@Test
	public void loadDataFromSearchResultWithFacets() throws Exception
	{
		// given
		final Map<String, AsConfigurationHolder<AsFacet, AbstractAsFacetConfiguration>> searchProfileResultFacets = new LinkedHashMap<>();
		searchProfileResultFacets.put(INDEX_PROPERTY_1, createConfigurationHolder(createFacetData1()));
		searchProfileResultFacets.put(INDEX_PROPERTY_2, createConfigurationHolder(createFacetData2()));

		final AsFacetValueData facetData1Value = new AsFacetValueData();
		facetData1Value.setValue("123");

		final AsFacetData facetData1 = new AsFacetData();
		facetData1.setIndexProperty(INDEX_PROPERTY_1);
		facetData1.setName(INDEX_PROPERTY_1);
		facetData1.setValues(Collections.singletonList(facetData1Value));
		facetData1.setVisibility(AsFacetVisibility.SHOW);

		final AsFacetValueData facetData2Value = new AsFacetValueData();
		facetData2Value.setValue("456");

		final AsFacetData facetData2 = new AsFacetData();
		facetData2.setIndexProperty(INDEX_PROPERTY_2);
		facetData2.setName(INDEX_PROPERTY_2);
		facetData2.setValues(Collections.singletonList(facetData2Value));
		facetData2.setVisibility(AsFacetVisibility.SHOW_VALUES);

		final SearchResultData searchResult = createSearchResult();
		final AsSearchResultData asSearchResult = searchResult.getAsSearchResult();
		asSearchResult.setFacets(Arrays.asList(facetData1, facetData2));
		asSearchResult.getSearchProfileResult().setFacets(searchProfileResultFacets);

		when(asFacetUtils.isOpen(facetData1)).thenReturn(false);
		when(asFacetUtils.isOpen(facetData2)).thenReturn(true);

		// when
		final ListModel<FacetEditorData> listModel = asFacetsDataHandler.loadData(null, searchResult,
				new HashMap<>());

		// then
		assertEquals(2, listModel.getSize());

		final FacetEditorData facet1 = listModel.getElementAt(0);
		assertEquals(UID_2, facet1.getUid());
		assertEquals(INDEX_PROPERTY_2, facet1.getIndexProperty());
		assertEquals(PRIORITY_2, facet1.getPriority());
		assertTrue(facet1.isOpen());
		assertFalse(facet1.isOverride());
		assertTrue(facet1.isInSearchResult());
		assertNull(facet1.getModel());

		final FacetEditorData facet2 = listModel.getElementAt(1);
		assertEquals(UID_1, facet2.getUid());
		assertEquals(INDEX_PROPERTY_1, facet2.getIndexProperty());
		assertEquals(PRIORITY_1, facet2.getPriority());
		assertFalse(facet2.isOpen());
		assertFalse(facet2.isOverride());
		assertTrue(facet2.isInSearchResult());
		assertNull(facet2.getModel());
	}


	@Test
	public void loadDataCombined() throws Exception
	{
		// given
		final AsFacetModel facetModel = createFacetModel1();
		final List<AsFacetModel> initialValue = Collections.singletonList(facetModel);

		final Map<String, AsConfigurationHolder<AsFacet, AbstractAsFacetConfiguration>> searchProfileResultFacets = new LinkedHashMap<>();
		searchProfileResultFacets.put(INDEX_PROPERTY_1, createConfigurationHolder(createFacetData1()));
		searchProfileResultFacets.put(INDEX_PROPERTY_2, createConfigurationHolder(createFacetData2()));

		final SearchResultData searchResult = createSearchResult();
		final AsSearchResultData asSearchResult = searchResult.getAsSearchResult();
		asSearchResult.getSearchProfileResult().setFacets(searchProfileResultFacets);

		// when
		final ListModel<FacetEditorData> listModel = asFacetsDataHandler.loadData(initialValue, searchResult, new HashMap<>());

		// then
		assertEquals(2, listModel.getSize());

		final FacetEditorData facet1 = listModel.getElementAt(0);
		assertEquals(UID_2, facet1.getUid());
		assertEquals(INDEX_PROPERTY_2, facet1.getIndexProperty());
		assertEquals(PRIORITY_2, facet1.getPriority());
		assertFalse(facet1.isOpen());
		assertFalse(facet1.isOverride());
		assertFalse(facet1.isInSearchResult());
		assertNull(facet1.getModel());

		final FacetEditorData facet2 = listModel.getElementAt(1);
		assertEquals(UID_1, facet2.getUid());
		assertEquals(INDEX_PROPERTY_1, facet2.getIndexProperty());
		assertEquals(PRIORITY_1, facet2.getPriority());
		assertFalse(facet2.isOpen());
		assertFalse(facet2.isOverride());
		assertFalse(facet2.isInSearchResult());
		assertSame(facetModel, facet2.getModel());
	}

	@Test
	public void getValue() throws Exception
	{
		// given
		final AsFacetModel facetModel = createFacetModel1();
		final List<AsFacetModel> initialValue = Collections.singletonList(facetModel);

		final ListModel<FacetEditorData> model = asFacetsDataHandler.loadData(initialValue, null, new HashMap<>());

		// when
		final List<AsFacetModel> value = asFacetsDataHandler.getValue(model);

		// then
		assertNotNull(value);
		assertEquals(1, value.size());
		assertSame(facetModel, value.get(0));
	}

	@Test
	public void getItemValue() throws Exception
	{
		// given
		final AsFacetModel facetModel = createFacetModel1();
		final List<AsFacetModel> initialValue = Collections.singletonList(facetModel);

		final ListModel<FacetEditorData> model = asFacetsDataHandler.loadData(initialValue, null, new HashMap<>());

		// when
		final AsFacetModel itemValue = asFacetsDataHandler.getItemValue(model.getElementAt(0));

		// then
		assertSame(facetModel, itemValue);
	}

	@Test
	public void getAttributeValue() throws Exception
	{
		// given
		final AsFacetModel facetModel = createFacetModel1();
		final List<AsFacetModel> initialValue = Collections.singletonList(facetModel);

		final ListModel<FacetEditorData> listModel = asFacetsDataHandler.loadData(initialValue, null, new HashMap<>());
		final FacetEditorData facet = listModel.getElementAt(0);

		// when
		final Object priority = asFacetsDataHandler.getAttributeValue(facet, PRIORITY_ATTRIBUTE);

		// then
		assertEquals(PRIORITY_1, priority);
	}

	protected AsFacetModel createFacetModel1()
	{
		final AsFacetModel facetModel = new AsFacetModel();
		facetModel.setUid(UID_1);
		facetModel.setIndexProperty(INDEX_PROPERTY_1);
		facetModel.setPriority(PRIORITY_1);

		return facetModel;
	}

	protected AsFacetModel createFacetModel2()
	{
		final AsFacetModel facetModel = new AsFacetModel();
		facetModel.setUid(UID_2);
		facetModel.setIndexProperty(INDEX_PROPERTY_2);
		facetModel.setPriority(PRIORITY_2);

		return facetModel;
	}

	protected AsFacet createFacetData1()
	{
		final AsFacet facetData = new AsFacet();
		facetData.setUid(UID_1);
		facetData.setIndexProperty(INDEX_PROPERTY_1);
		facetData.setPriority(PRIORITY_1);

		return facetData;
	}

	protected AsFacet createFacetData2()
	{
		final AsFacet facetData = new AsFacet();
		facetData.setUid(UID_2);
		facetData.setIndexProperty(INDEX_PROPERTY_2);
		facetData.setPriority(PRIORITY_2);

		return facetData;
	}
}
