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
package de.hybris.platform.adaptivesearchbackoffice.editors.excludedfacets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.adaptivesearch.data.AbstractAsFacetConfiguration;
import de.hybris.platform.adaptivesearch.data.AsConfigurationHolder;
import de.hybris.platform.adaptivesearch.data.AsExcludedFacet;
import de.hybris.platform.adaptivesearch.data.AsSearchResultData;
import de.hybris.platform.adaptivesearch.model.AsExcludedFacetModel;
import de.hybris.platform.adaptivesearchbackoffice.common.AsFacetUtils;
import de.hybris.platform.adaptivesearchbackoffice.data.ExcludedFacetEditorData;
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
public class AsExcludedFacetsDataHandlerTest extends AbstractDataHandlerTest
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

	private AsExcludedFacetsDataHandler asExcludedFacetsDataHandler;

	@Before
	public void setUp() throws Exception
	{
		MockitoAnnotations.initMocks(this);

		asExcludedFacetsDataHandler = new AsExcludedFacetsDataHandler();
		asExcludedFacetsDataHandler.setAsFacetUtils(asFacetUtils);
	}

	@Test
	public void getTypeCode() throws Exception
	{
		// when
		final String type = asExcludedFacetsDataHandler.getTypeCode();

		// then
		assertEquals(type, AsExcludedFacetModel._TYPECODE);
	}

	@Test
	public void loadNullData()
	{
		// when
		final ListModel<ExcludedFacetEditorData> listModel = asExcludedFacetsDataHandler.loadData(null, null, null);

		// then
		assertEquals(0, listModel.getSize());
	}

	@Test
	public void loadEmptyData()
	{
		// given
		final SearchResultData searchResult = new SearchResultData();
		searchResult.setAsSearchResult(new AsSearchResultData());

		// when
		final ListModel<ExcludedFacetEditorData> listModel = asExcludedFacetsDataHandler.loadData(new ArrayList<>(), searchResult,
				new HashMap<>());

		// then
		assertEquals(0, listModel.getSize());
	}

	@Test
	public void loadDataFromInitialValue() throws Exception
	{
		// given
		final AsExcludedFacetModel excludedFacetModel1 = createExcludedFacetModel1();
		final AsExcludedFacetModel excludedFacetModel2 = createExcludedFacetModel2();
		final List<AsExcludedFacetModel> initialValue = Arrays.asList(excludedFacetModel1, excludedFacetModel2);

		// when
		final ListModel<ExcludedFacetEditorData> listModel = asExcludedFacetsDataHandler.loadData(initialValue, null,
				new HashMap<>());

		// then
		assertEquals(2, listModel.getSize());

		final ExcludedFacetEditorData excludedFacet1 = listModel.getElementAt(0);
		assertEquals(UID_1, excludedFacet1.getUid());
		assertEquals(INDEX_PROPERTY_1, excludedFacet1.getIndexProperty());
		assertEquals(PRIORITY_1, excludedFacet1.getPriority());
		assertFalse(excludedFacet1.isOpen());
		assertFalse(excludedFacet1.isOverride());
		assertFalse(excludedFacet1.isInSearchResult());
		assertSame(excludedFacetModel1, excludedFacet1.getModel());

		final ExcludedFacetEditorData excludedFacet2 = listModel.getElementAt(1);
		assertEquals(UID_2, excludedFacet2.getUid());
		assertEquals(INDEX_PROPERTY_2, excludedFacet2.getIndexProperty());
		assertEquals(PRIORITY_2, excludedFacet2.getPriority());
		assertFalse(excludedFacet2.isOpen());
		assertFalse(excludedFacet2.isOverride());
		assertFalse(excludedFacet2.isInSearchResult());
		assertSame(excludedFacetModel2, excludedFacet2.getModel());
	}

	@Test
	public void loadDataFromSearchResult() throws Exception
	{
		// given
		final Map<String, AsConfigurationHolder<AsExcludedFacet, AbstractAsFacetConfiguration>> searchProfileResultExcludedFacets = new LinkedHashMap<>();
		searchProfileResultExcludedFacets.put(INDEX_PROPERTY_1, createConfigurationHolder(createExcludedFacetData1()));
		searchProfileResultExcludedFacets.put(INDEX_PROPERTY_2, createConfigurationHolder(createExcludedFacetData2()));

		final SearchResultData searchResult = createSearchResult();
		final AsSearchResultData asSearchResult = searchResult.getAsSearchResult();
		asSearchResult.getSearchProfileResult().setExcludedFacets(searchProfileResultExcludedFacets);

		// when
		final ListModel<ExcludedFacetEditorData> listModel = asExcludedFacetsDataHandler.loadData(null, searchResult,
				new HashMap<>());

		// then
		assertEquals(2, listModel.getSize());

		final ExcludedFacetEditorData excludedFacet1 = listModel.getElementAt(0);
		assertEquals(UID_1, excludedFacet1.getUid());
		assertEquals(INDEX_PROPERTY_1, excludedFacet1.getIndexProperty());
		assertEquals(PRIORITY_1, excludedFacet1.getPriority());
		assertFalse(excludedFacet1.isOpen());
		assertFalse(excludedFacet1.isOverride());
		assertNull(excludedFacet1.getModel());

		final ExcludedFacetEditorData excludedFacet2 = listModel.getElementAt(1);
		assertEquals(UID_2, excludedFacet2.getUid());
		assertEquals(INDEX_PROPERTY_2, excludedFacet2.getIndexProperty());
		assertEquals(PRIORITY_2, excludedFacet2.getPriority());
		assertFalse(excludedFacet2.isOpen());
		assertFalse(excludedFacet2.isOverride());
		assertFalse(excludedFacet2.isInSearchResult());
		assertNull(excludedFacet2.getModel());
	}

	@Test
	public void loadDataCombined() throws Exception
	{
		// given
		final AsExcludedFacetModel excludedFacetModel = createExcludedFacetModel1();
		final List<AsExcludedFacetModel> initialValue = Collections.singletonList(excludedFacetModel);

		final Map<String, AsConfigurationHolder<AsExcludedFacet, AbstractAsFacetConfiguration>> searchProfileResultExcludedFacets = new LinkedHashMap<>();
		searchProfileResultExcludedFacets.put(INDEX_PROPERTY_1, createConfigurationHolder(createExcludedFacetData1()));
		searchProfileResultExcludedFacets.put(INDEX_PROPERTY_2, createConfigurationHolder(createExcludedFacetData2()));

		final SearchResultData searchResult = createSearchResult();
		final AsSearchResultData asSearchResult = searchResult.getAsSearchResult();
		asSearchResult.getSearchProfileResult().setExcludedFacets(searchProfileResultExcludedFacets);

		// when
		final ListModel<ExcludedFacetEditorData> listModel = asExcludedFacetsDataHandler.loadData(initialValue, searchResult,
				new HashMap<>());

		// then
		assertEquals(2, listModel.getSize());

		final ExcludedFacetEditorData excludedFacet1 = listModel.getElementAt(0);
		assertEquals(UID_1, excludedFacet1.getUid());
		assertEquals(INDEX_PROPERTY_1, excludedFacet1.getIndexProperty());
		assertEquals(PRIORITY_1, excludedFacet1.getPriority());
		assertFalse(excludedFacet1.isOpen());
		assertFalse(excludedFacet1.isOverride());
		assertFalse(excludedFacet1.isInSearchResult());
		assertSame(excludedFacetModel, excludedFacet1.getModel());

		final ExcludedFacetEditorData excludedFacet2 = listModel.getElementAt(1);
		assertEquals(UID_2, excludedFacet2.getUid());
		assertEquals(INDEX_PROPERTY_2, excludedFacet2.getIndexProperty());
		assertEquals(PRIORITY_2, excludedFacet2.getPriority());
		assertFalse(excludedFacet2.isOpen());
		assertFalse(excludedFacet2.isOverride());
		assertFalse(excludedFacet2.isInSearchResult());
		assertNull(excludedFacet2.getModel());
	}

	@Test
	public void getValue() throws Exception
	{
		// given
		final AsExcludedFacetModel excludedFacetModel = createExcludedFacetModel1();
		final List<AsExcludedFacetModel> initialValue = Collections.singletonList(excludedFacetModel);

		final ListModel<ExcludedFacetEditorData> model = asExcludedFacetsDataHandler.loadData(initialValue, null, new HashMap<>());

		// when
		final List<AsExcludedFacetModel> value = asExcludedFacetsDataHandler.getValue(model);

		// then
		assertNotNull(value);
		assertEquals(1, value.size());
		assertSame(excludedFacetModel, value.get(0));
	}

	@Test
	public void getItemValue() throws Exception
	{
		// given
		final AsExcludedFacetModel excludedFacetModel = createExcludedFacetModel1();
		final List<AsExcludedFacetModel> initialValue = Collections.singletonList(excludedFacetModel);

		final ListModel<ExcludedFacetEditorData> model = asExcludedFacetsDataHandler.loadData(initialValue, null, new HashMap<>());

		// when
		final AsExcludedFacetModel itemValue = asExcludedFacetsDataHandler.getItemValue(model.getElementAt(0));

		// then
		assertSame(excludedFacetModel, itemValue);
	}

	@Test
	public void getAttributeValue() throws Exception
	{
		// given
		final AsExcludedFacetModel excludedFacetModel = createExcludedFacetModel1();
		final List<AsExcludedFacetModel> initialValue = Collections.singletonList(excludedFacetModel);

		final ListModel<ExcludedFacetEditorData> listModel = asExcludedFacetsDataHandler.loadData(initialValue, null,
				new HashMap<>());
		final ExcludedFacetEditorData excludedFacet = listModel.getElementAt(0);

		// when
		final Object priority = asExcludedFacetsDataHandler.getAttributeValue(excludedFacet, PRIORITY_ATTRIBUTE);

		// then
		assertEquals(PRIORITY_1, priority);
	}

	protected AsExcludedFacetModel createExcludedFacetModel1()
	{
		final AsExcludedFacetModel excludedFacetModel = new AsExcludedFacetModel();
		excludedFacetModel.setUid(UID_1);
		excludedFacetModel.setIndexProperty(INDEX_PROPERTY_1);
		excludedFacetModel.setPriority(PRIORITY_1);

		return excludedFacetModel;
	}

	protected AsExcludedFacetModel createExcludedFacetModel2()
	{
		final AsExcludedFacetModel excludedFacetModel = new AsExcludedFacetModel();
		excludedFacetModel.setUid(UID_2);
		excludedFacetModel.setIndexProperty(INDEX_PROPERTY_2);
		excludedFacetModel.setPriority(PRIORITY_2);

		return excludedFacetModel;
	}

	protected AsExcludedFacet createExcludedFacetData1()
	{
		final AsExcludedFacet excludedFacetData = new AsExcludedFacet();
		excludedFacetData.setUid(UID_1);
		excludedFacetData.setIndexProperty(INDEX_PROPERTY_1);
		excludedFacetData.setPriority(PRIORITY_1);

		return excludedFacetData;
	}

	protected AsExcludedFacet createExcludedFacetData2()
	{
		final AsExcludedFacet excludedFacetData = new AsExcludedFacet();
		excludedFacetData.setUid(UID_2);
		excludedFacetData.setIndexProperty(INDEX_PROPERTY_2);
		excludedFacetData.setPriority(PRIORITY_2);

		return excludedFacetData;
	}
}
