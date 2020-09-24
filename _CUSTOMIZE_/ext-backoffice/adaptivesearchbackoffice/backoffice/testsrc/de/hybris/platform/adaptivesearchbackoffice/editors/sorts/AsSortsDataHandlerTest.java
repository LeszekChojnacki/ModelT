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
package de.hybris.platform.adaptivesearchbackoffice.editors.sorts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.adaptivesearch.data.AbstractAsSortConfiguration;
import de.hybris.platform.adaptivesearch.data.AsConfigurationHolder;
import de.hybris.platform.adaptivesearch.data.AsSearchResultData;
import de.hybris.platform.adaptivesearch.data.AsSort;
import de.hybris.platform.adaptivesearch.data.AsSortData;
import de.hybris.platform.adaptivesearch.model.AsSortModel;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchResultData;
import de.hybris.platform.adaptivesearchbackoffice.data.SortEditorData;
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
import org.mockito.MockitoAnnotations;
import org.zkoss.zul.ListModel;


@UnitTest
public class AsSortsDataHandlerTest extends AbstractDataHandlerTest
{
	private static final String PRIORITY_ATTRIBUTE = "priority";

	private static final String UID_1 = "uid1";
	private static final String CODE_1 = "code1";
	private static final Integer PRIORITY_1 = Integer.valueOf(1);

	private static final String UID_2 = "uid2";
	private static final String CODE_2 = "code2";
	private static final Integer PRIORITY_2 = Integer.valueOf(2);

	private AsSortsDataHandler asSortsDataHandler;

	@Before
	public void setUp() throws Exception
	{
		MockitoAnnotations.initMocks(this);

		asSortsDataHandler = new AsSortsDataHandler();
	}

	@Test
	public void getTypeCode() throws Exception
	{
		// when
		final String type = asSortsDataHandler.getTypeCode();

		// then
		assertEquals(type, AsSortModel._TYPECODE);
	}

	@Test
	public void loadNullData()
	{
		// when
		final ListModel<SortEditorData> listModel = asSortsDataHandler.loadData(null, null, null);

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
		final ListModel<SortEditorData> listModel = asSortsDataHandler.loadData(new ArrayList<>(), searchResultData,
				new HashMap<>());

		// then
		assertEquals(0, listModel.getSize());
	}

	@Test
	public void loadDataFromInitialValue() throws Exception
	{
		// given
		final AsSortModel sortModel1 = createSortModel1();
		final AsSortModel sortModel2 = createSortModel2();
		final List<AsSortModel> initialValue = Arrays.asList(sortModel1, sortModel2);

		// when
		final ListModel<SortEditorData> listModel = asSortsDataHandler.loadData(initialValue, null, new HashMap<>());

		// then
		assertEquals(2, listModel.getSize());

		final SortEditorData sort1 = listModel.getElementAt(0);
		assertEquals(UID_2, sort1.getUid());
		assertEquals(CODE_2, sort1.getCode());
		assertEquals(PRIORITY_2, sort1.getPriority());
		assertFalse(sort1.isOpen());
		assertFalse(sort1.isOverride());
		assertFalse(sort1.isInSearchResult());
		assertSame(sortModel2, sort1.getModel());

		final SortEditorData sort2 = listModel.getElementAt(1);
		assertEquals(UID_1, sort2.getUid());
		assertEquals(CODE_1, sort2.getCode());
		assertEquals(PRIORITY_1, sort2.getPriority());
		assertFalse(sort2.isOpen());
		assertFalse(sort2.isOverride());
		assertFalse(sort2.isInSearchResult());
		assertSame(sortModel1, sort2.getModel());
	}

	@Test
	public void loadDataFromSearchResult() throws Exception
	{
		// given
		final Map<String, AsConfigurationHolder<AsSort, AbstractAsSortConfiguration>> searchProfileResultSorts = new LinkedHashMap<>();
		searchProfileResultSorts.put(CODE_1, createConfigurationHolder(createSortData1()));
		searchProfileResultSorts.put(CODE_2, createConfigurationHolder(createSortData2()));

		final SearchResultData searchResult = createSearchResult();
		final AsSearchResultData asSearchResult = searchResult.getAsSearchResult();
		asSearchResult.getSearchProfileResult().setSorts(searchProfileResultSorts);

		// when
		final ListModel<SortEditorData> listModel = asSortsDataHandler.loadData(null, searchResult, new HashMap<>());

		// then
		assertEquals(2, listModel.getSize());

		final SortEditorData sort1 = listModel.getElementAt(0);
		assertEquals(UID_2, sort1.getUid());
		assertEquals(CODE_2, sort1.getCode());
		assertEquals(PRIORITY_2, sort1.getPriority());
		assertFalse(sort1.isOpen());
		assertFalse(sort1.isOverride());
		assertFalse(sort1.isInSearchResult());
		assertNull(sort1.getModel());

		final SortEditorData sort2 = listModel.getElementAt(1);
		assertEquals(UID_1, sort2.getUid());
		assertEquals(CODE_1, sort2.getCode());
		assertEquals(PRIORITY_1, sort2.getPriority());
		assertFalse(sort2.isOpen());
		assertFalse(sort2.isOverride());
		assertFalse(sort2.isInSearchResult());
		assertNull(sort2.getModel());
	}

	@Test
	public void loadDataFromSearchResultWithSorts() throws Exception
	{
		// given
		final Map<String, AsConfigurationHolder<AsSort, AbstractAsSortConfiguration>> searchProfileResultSorts = new LinkedHashMap<>();
		searchProfileResultSorts.put(CODE_1, createConfigurationHolder(createSortData1()));
		searchProfileResultSorts.put(CODE_2, createConfigurationHolder(createSortData2()));

		final AsSortData sortData1 = new AsSortData();
		sortData1.setCode(CODE_1);
		sortData1.setName(CODE_1);

		final AsSortData sortData2 = new AsSortData();
		sortData2.setCode(CODE_2);
		sortData2.setName(CODE_2);

		final SearchResultData searchResult = createSearchResult();
		final AsSearchResultData asSearchResult = searchResult.getAsSearchResult();
		asSearchResult.setAvailableSorts(Arrays.asList(sortData1, sortData2));
		asSearchResult.getSearchProfileResult().setSorts(searchProfileResultSorts);

		// when
		final ListModel<SortEditorData> listModel = asSortsDataHandler.loadData(null, searchResult, new HashMap<>());

		// then
		assertEquals(2, listModel.getSize());

		final SortEditorData sort1 = listModel.getElementAt(0);
		assertEquals(UID_2, sort1.getUid());
		assertEquals(CODE_2, sort1.getCode());
		assertEquals(PRIORITY_2, sort1.getPriority());
		assertTrue(sort1.isOpen());
		assertFalse(sort1.isOverride());
		assertTrue(sort1.isInSearchResult());
		assertNull(sort1.getModel());

		final SortEditorData sort2 = listModel.getElementAt(1);
		assertEquals(UID_1, sort2.getUid());
		assertEquals(CODE_1, sort2.getCode());
		assertEquals(PRIORITY_1, sort2.getPriority());
		assertFalse(sort2.isOpen());
		assertFalse(sort2.isOverride());
		assertTrue(sort2.isInSearchResult());
		assertNull(sort2.getModel());
	}


	@Test
	public void loadDataCombined() throws Exception
	{
		// given
		final AsSortModel sortModel = createSortModel1();
		final List<AsSortModel> initialValue = Collections.singletonList(sortModel);

		final Map<String, AsConfigurationHolder<AsSort, AbstractAsSortConfiguration>> searchProfileResultSorts = new LinkedHashMap<>();
		searchProfileResultSorts.put(CODE_1, createConfigurationHolder(createSortData1()));
		searchProfileResultSorts.put(CODE_2, createConfigurationHolder(createSortData2()));

		final SearchResultData searchResult = createSearchResult();
		final AsSearchResultData asSearchResult = searchResult.getAsSearchResult();
		asSearchResult.getSearchProfileResult().setSorts(searchProfileResultSorts);

		// when
		final ListModel<SortEditorData> listModel = asSortsDataHandler.loadData(initialValue, searchResult, new HashMap<>());

		// then
		assertEquals(2, listModel.getSize());

		final SortEditorData sort1 = listModel.getElementAt(0);
		assertEquals(UID_2, sort1.getUid());
		assertEquals(CODE_2, sort1.getCode());
		assertEquals(PRIORITY_2, sort1.getPriority());
		assertFalse(sort1.isOpen());
		assertFalse(sort1.isOverride());
		assertFalse(sort1.isInSearchResult());
		assertNull(sort1.getModel());

		final SortEditorData sort2 = listModel.getElementAt(1);
		assertEquals(UID_1, sort2.getUid());
		assertEquals(CODE_1, sort2.getCode());
		assertEquals(PRIORITY_1, sort2.getPriority());
		assertFalse(sort2.isOpen());
		assertFalse(sort2.isOverride());
		assertFalse(sort2.isInSearchResult());
		assertSame(sortModel, sort2.getModel());
	}

	@Test
	public void getValue() throws Exception
	{
		// given
		final AsSortModel sortModel = createSortModel1();
		final List<AsSortModel> initialValue = Collections.singletonList(sortModel);

		final ListModel<SortEditorData> model = asSortsDataHandler.loadData(initialValue, null, new HashMap<>());

		// when
		final List<AsSortModel> value = asSortsDataHandler.getValue(model);

		// then
		assertNotNull(value);
		assertEquals(1, value.size());
		assertSame(sortModel, value.get(0));
	}

	@Test
	public void getItemValue() throws Exception
	{
		// given
		final AsSortModel sortModel = createSortModel1();
		final List<AsSortModel> initialValue = Collections.singletonList(sortModel);

		final ListModel<SortEditorData> model = asSortsDataHandler.loadData(initialValue, null, new HashMap<>());

		// when
		final AsSortModel itemValue = asSortsDataHandler.getItemValue(model.getElementAt(0));

		// then
		assertSame(sortModel, itemValue);
	}

	@Test
	public void getAttributeValue() throws Exception
	{
		// given
		final AsSortModel sortModel = createSortModel1();
		final List<AsSortModel> initialValue = Collections.singletonList(sortModel);

		final ListModel<SortEditorData> listModel = asSortsDataHandler.loadData(initialValue, null, new HashMap<>());
		final SortEditorData sort = listModel.getElementAt(0);

		// when
		final Object priority = asSortsDataHandler.getAttributeValue(sort, PRIORITY_ATTRIBUTE);

		// then
		assertEquals(PRIORITY_1, priority);
	}

	protected AsSortModel createSortModel1()
	{
		final AsSortModel sortModel = new AsSortModel();
		sortModel.setUid(UID_1);
		sortModel.setCode(CODE_1);
		sortModel.setPriority(PRIORITY_1);

		return sortModel;
	}

	protected AsSortModel createSortModel2()
	{
		final AsSortModel sortModel = new AsSortModel();
		sortModel.setUid(UID_2);
		sortModel.setCode(CODE_2);
		sortModel.setPriority(PRIORITY_2);

		return sortModel;
	}

	protected AsSort createSortData1()
	{
		final AsSort sortData = new AsSort();
		sortData.setUid(UID_1);
		sortData.setCode(CODE_1);
		sortData.setPriority(PRIORITY_1);

		return sortData;
	}

	protected AsSort createSortData2()
	{
		final AsSort sortData = new AsSort();
		sortData.setUid(UID_2);
		sortData.setCode(CODE_2);
		sortData.setPriority(PRIORITY_2);

		return sortData;
	}
}
