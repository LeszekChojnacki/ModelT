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
package de.hybris.platform.adaptivesearchbackoffice.editors.promotedfacets;

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
import de.hybris.platform.adaptivesearch.data.AsFacetData;
import de.hybris.platform.adaptivesearch.data.AsFacetValueData;
import de.hybris.platform.adaptivesearch.data.AsFacetVisibility;
import de.hybris.platform.adaptivesearch.data.AsPromotedFacet;
import de.hybris.platform.adaptivesearch.data.AsSearchResultData;
import de.hybris.platform.adaptivesearch.model.AsPromotedFacetModel;
import de.hybris.platform.adaptivesearchbackoffice.common.AsFacetUtils;
import de.hybris.platform.adaptivesearchbackoffice.data.PromotedFacetEditorData;
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
public class AsPromotedFacetsDataHandlerTest extends AbstractDataHandlerTest
{
	private static final String PRIORITY_ATTRIBUTE = "priority";

	private static final String UID_1 = "uid1";
	private static final String INDEX_PROPERTY_1 = "property1";
	private static final Integer PRIORITY_1 = Integer.valueOf(1);

	private static final String UID_2 = "uid2";
	private static final String INDEX_PROPERTY_2 = "property2";
	private static final Integer PRIORITY_2 = Integer.valueOf(2);

	@Mock
	private AsFacetUtils asFacetUtils;

	private AsPromotedFacetsDataHandler asPromotedFacetsDataHandler;

	@Before
	public void setUp() throws Exception
	{
		MockitoAnnotations.initMocks(this);

		asPromotedFacetsDataHandler = new AsPromotedFacetsDataHandler();
		asPromotedFacetsDataHandler.setAsFacetUtils(asFacetUtils);
	}

	@Test
	public void getTypeCode() throws Exception
	{
		// when
		final String type = asPromotedFacetsDataHandler.getTypeCode();

		// then
		assertEquals(type, AsPromotedFacetModel._TYPECODE);
	}

	@Test
	public void loadNullData()
	{
		// when
		final ListModel<PromotedFacetEditorData> listModel = asPromotedFacetsDataHandler.loadData(null, null, null);

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
		final ListModel<PromotedFacetEditorData> listModel = asPromotedFacetsDataHandler.loadData(new ArrayList<>(),
				searchResultData, new HashMap<>());

		// then
		assertEquals(0, listModel.getSize());
	}

	@Test
	public void loadDataFromInitialValue() throws Exception
	{
		// given
		final AsPromotedFacetModel promotedFacetModel1 = createPromotedFacetModel1();
		final AsPromotedFacetModel promotedFacetModel2 = createPromotedFacetModel2();
		final List<AsPromotedFacetModel> initialValue = Arrays.asList(promotedFacetModel1, promotedFacetModel2);

		// when
		final ListModel<PromotedFacetEditorData> listModel = asPromotedFacetsDataHandler.loadData(initialValue, null,
				new HashMap<>());

		// then
		assertEquals(2, listModel.getSize());

		final PromotedFacetEditorData promotedFacet1 = listModel.getElementAt(0);
		assertEquals(UID_1, promotedFacet1.getUid());
		assertEquals(INDEX_PROPERTY_1, promotedFacet1.getIndexProperty());
		assertEquals(PRIORITY_1, promotedFacet1.getPriority());
		assertFalse(promotedFacet1.isOpen());
		assertFalse(promotedFacet1.isOverride());
		assertFalse(promotedFacet1.isInSearchResult());
		assertSame(promotedFacetModel1, promotedFacet1.getModel());

		final PromotedFacetEditorData promotedFacet2 = listModel.getElementAt(1);
		assertEquals(UID_2, promotedFacet2.getUid());
		assertEquals(INDEX_PROPERTY_2, promotedFacet2.getIndexProperty());
		assertEquals(PRIORITY_2, promotedFacet2.getPriority());
		assertFalse(promotedFacet2.isOpen());
		assertFalse(promotedFacet2.isOverride());
		assertFalse(promotedFacet2.isInSearchResult());
		assertSame(promotedFacetModel2, promotedFacet2.getModel());
	}

	@Test
	public void loadDataFromSearchResult() throws Exception
	{
		// given
		final Map<String, AsConfigurationHolder<AsPromotedFacet, AbstractAsFacetConfiguration>> searchProfileResultPromotedFacets = new LinkedHashMap<>();
		searchProfileResultPromotedFacets.put(INDEX_PROPERTY_1, createConfigurationHolder(createPromotedFacetData1()));
		searchProfileResultPromotedFacets.put(INDEX_PROPERTY_2, createConfigurationHolder(createPromotedFacetData2()));

		final SearchResultData searchResult = createSearchResult();
		final AsSearchResultData asSearchResult = searchResult.getAsSearchResult();
		asSearchResult.getSearchProfileResult().setPromotedFacets(searchProfileResultPromotedFacets);

		// when
		final ListModel<PromotedFacetEditorData> listModel = asPromotedFacetsDataHandler.loadData(null, searchResult,
				new HashMap<>());

		// then
		assertEquals(2, listModel.getSize());

		final PromotedFacetEditorData promotedFacet1 = listModel.getElementAt(0);
		assertEquals(UID_1, promotedFacet1.getUid());
		assertEquals(INDEX_PROPERTY_1, promotedFacet1.getIndexProperty());
		assertEquals(PRIORITY_1, promotedFacet1.getPriority());
		assertFalse(promotedFacet1.isOpen());
		assertFalse(promotedFacet1.isOverride());
		assertFalse(promotedFacet1.isInSearchResult());
		assertFalse(promotedFacet1.isInSearchResult());
		assertNull(promotedFacet1.getModel());

		final PromotedFacetEditorData promotedFacet2 = listModel.getElementAt(1);
		assertEquals(UID_2, promotedFacet2.getUid());
		assertEquals(INDEX_PROPERTY_2, promotedFacet2.getIndexProperty());
		assertEquals(PRIORITY_2, promotedFacet2.getPriority());
		assertFalse(promotedFacet2.isOpen());
		assertFalse(promotedFacet2.isOverride());
		assertFalse(promotedFacet2.isInSearchResult());
		assertNull(promotedFacet2.getModel());
	}

	@Test
	public void loadDataFromSearchResultWithFacets() throws Exception
	{
		// given
		final Map<String, AsConfigurationHolder<AsPromotedFacet, AbstractAsFacetConfiguration>> searchProfileResultPromotedFacets = new LinkedHashMap<>();
		searchProfileResultPromotedFacets.put(INDEX_PROPERTY_1, createConfigurationHolder(createPromotedFacetData1()));
		searchProfileResultPromotedFacets.put(INDEX_PROPERTY_2, createConfigurationHolder(createPromotedFacetData2()));

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
		asSearchResult.getSearchProfileResult().setPromotedFacets(searchProfileResultPromotedFacets);

		when(asFacetUtils.isOpen(facetData1)).thenReturn(false);
		when(asFacetUtils.isOpen(facetData2)).thenReturn(true);

		// when
		final ListModel<PromotedFacetEditorData> listModel = asPromotedFacetsDataHandler.loadData(null, searchResult,
				new HashMap<>());

		// then
		assertEquals(2, listModel.getSize());

		final PromotedFacetEditorData promotedFacet1 = listModel.getElementAt(0);
		assertEquals(UID_1, promotedFacet1.getUid());
		assertEquals(INDEX_PROPERTY_1, promotedFacet1.getIndexProperty());
		assertEquals(PRIORITY_1, promotedFacet1.getPriority());
		assertFalse(promotedFacet1.isOpen());
		assertFalse(promotedFacet1.isOverride());
		assertTrue(promotedFacet1.isInSearchResult());
		assertNull(promotedFacet1.getModel());

		final PromotedFacetEditorData promotedFacet2 = listModel.getElementAt(1);
		assertEquals(UID_2, promotedFacet2.getUid());
		assertEquals(INDEX_PROPERTY_2, promotedFacet2.getIndexProperty());
		assertEquals(PRIORITY_2, promotedFacet2.getPriority());
		assertTrue(promotedFacet2.isOpen());
		assertFalse(promotedFacet2.isOverride());
		assertTrue(promotedFacet2.isInSearchResult());
		assertNull(promotedFacet2.getModel());
	}

	@Test
	public void loadDataCombined() throws Exception
	{
		// given
		final AsPromotedFacetModel promotedFacetModel = createPromotedFacetModel1();
		final List<AsPromotedFacetModel> initialValue = Collections.singletonList(promotedFacetModel);

		final Map<String, AsConfigurationHolder<AsPromotedFacet, AbstractAsFacetConfiguration>> searchProfileResultPromotedFacets = new LinkedHashMap<>();
		searchProfileResultPromotedFacets.put(INDEX_PROPERTY_1, createConfigurationHolder(createPromotedFacetData1()));
		searchProfileResultPromotedFacets.put(INDEX_PROPERTY_2, createConfigurationHolder(createPromotedFacetData2()));

		final SearchResultData searchResult = createSearchResult();
		final AsSearchResultData asSearchResult = searchResult.getAsSearchResult();
		asSearchResult.getSearchProfileResult().setPromotedFacets(searchProfileResultPromotedFacets);

		// when
		final ListModel<PromotedFacetEditorData> listModel = asPromotedFacetsDataHandler.loadData(initialValue, searchResult,
				new HashMap<>());

		// then
		assertEquals(2, listModel.getSize());

		final PromotedFacetEditorData promotedFacet1 = listModel.getElementAt(0);
		assertEquals(UID_1, promotedFacet1.getUid());
		assertEquals(INDEX_PROPERTY_1, promotedFacet1.getIndexProperty());
		assertEquals(PRIORITY_1, promotedFacet1.getPriority());
		assertFalse(promotedFacet1.isOverride());
		assertFalse(promotedFacet1.isInSearchResult());
		assertSame(promotedFacetModel, promotedFacet1.getModel());

		final PromotedFacetEditorData promotedFacet2 = listModel.getElementAt(1);
		assertEquals(UID_2, promotedFacet2.getUid());
		assertEquals(INDEX_PROPERTY_2, promotedFacet2.getIndexProperty());
		assertEquals(PRIORITY_2, promotedFacet2.getPriority());
		assertFalse(promotedFacet2.isOverride());
		assertFalse(promotedFacet2.isInSearchResult());
		assertNull(promotedFacet2.getModel());
	}

	@Test
	public void getValue() throws Exception
	{
		// given
		final AsPromotedFacetModel promotedFacetModel = createPromotedFacetModel1();
		final List<AsPromotedFacetModel> initialValue = Collections.singletonList(promotedFacetModel);

		final ListModel<PromotedFacetEditorData> model = asPromotedFacetsDataHandler.loadData(initialValue, null, new HashMap<>());

		// when
		final List<AsPromotedFacetModel> value = asPromotedFacetsDataHandler.getValue(model);

		// then
		assertNotNull(value);
		assertEquals(1, value.size());
		assertSame(promotedFacetModel, value.get(0));
	}

	@Test
	public void getItemValue() throws Exception
	{
		// given
		final AsPromotedFacetModel promotedFacetModel = createPromotedFacetModel1();
		final List<AsPromotedFacetModel> initialValue = Collections.singletonList(promotedFacetModel);

		final ListModel<PromotedFacetEditorData> model = asPromotedFacetsDataHandler.loadData(initialValue, null, new HashMap<>());

		// when
		final AsPromotedFacetModel itemValue = asPromotedFacetsDataHandler.getItemValue(model.getElementAt(0));

		// then
		assertSame(promotedFacetModel, itemValue);
	}

	@Test
	public void getAttributeValue() throws Exception
	{
		// given
		final AsPromotedFacetModel promotedFacetModel = createPromotedFacetModel1();
		final List<AsPromotedFacetModel> initialValue = Collections.singletonList(promotedFacetModel);

		final ListModel<PromotedFacetEditorData> listModel = asPromotedFacetsDataHandler.loadData(initialValue, null,
				new HashMap<>());
		final PromotedFacetEditorData promotedFacet = listModel.getElementAt(0);

		// when
		final Object priority = asPromotedFacetsDataHandler.getAttributeValue(promotedFacet, PRIORITY_ATTRIBUTE);

		// then
		assertEquals(PRIORITY_1, priority);
	}

	protected AsPromotedFacetModel createPromotedFacetModel1()
	{
		final AsPromotedFacetModel promotedFacetModel = new AsPromotedFacetModel();
		promotedFacetModel.setUid(UID_1);
		promotedFacetModel.setIndexProperty(INDEX_PROPERTY_1);
		promotedFacetModel.setPriority(PRIORITY_1);

		return promotedFacetModel;
	}

	protected AsPromotedFacetModel createPromotedFacetModel2()
	{
		final AsPromotedFacetModel promotedFacetModel = new AsPromotedFacetModel();
		promotedFacetModel.setUid(UID_2);
		promotedFacetModel.setIndexProperty(INDEX_PROPERTY_2);
		promotedFacetModel.setPriority(PRIORITY_2);

		return promotedFacetModel;
	}

	protected AsPromotedFacet createPromotedFacetData1()
	{
		final AsPromotedFacet promotedFacetData = new AsPromotedFacet();
		promotedFacetData.setUid(UID_1);
		promotedFacetData.setIndexProperty(INDEX_PROPERTY_1);
		promotedFacetData.setPriority(PRIORITY_1);

		return promotedFacetData;
	}

	protected AsPromotedFacet createPromotedFacetData2()
	{
		final AsPromotedFacet promotedFacetData = new AsPromotedFacet();
		promotedFacetData.setUid(UID_2);
		promotedFacetData.setIndexProperty(INDEX_PROPERTY_2);
		promotedFacetData.setPriority(PRIORITY_2);

		return promotedFacetData;
	}
}
