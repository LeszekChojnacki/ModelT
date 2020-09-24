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
package de.hybris.platform.adaptivesearchbackoffice.editors.promotedsorts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.adaptivesearch.data.AbstractAsSortConfiguration;
import de.hybris.platform.adaptivesearch.data.AsConfigurationHolder;
import de.hybris.platform.adaptivesearch.data.AsPromotedSort;
import de.hybris.platform.adaptivesearch.data.AsSearchResultData;
import de.hybris.platform.adaptivesearch.data.AsSortData;
import de.hybris.platform.adaptivesearch.model.AsPromotedSortModel;
import de.hybris.platform.adaptivesearchbackoffice.data.PromotedSortEditorData;
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
import org.mockito.MockitoAnnotations;
import org.zkoss.zul.ListModel;


@UnitTest
public class AsPromotedSortsDataHandlerTest extends AbstractDataHandlerTest
{
	private static final String PRIORITY_ATTRIBUTE = "priority";

	private static final String UID_1 = "uid1";
	private static final String CODE_1 = "code1";
	private static final Integer PRIORITY_1 = Integer.valueOf(1);

	private static final String UID_2 = "uid2";
	private static final String CODE_2 = "code2";
	private static final Integer PRIORITY_2 = Integer.valueOf(2);

	private AsPromotedSortsDataHandler asPromotedSortsDataHandler;

	@Before
	public void setUp() throws Exception
	{
		MockitoAnnotations.initMocks(this);

		asPromotedSortsDataHandler = new AsPromotedSortsDataHandler();
	}

	@Test
	public void getTypeCode() throws Exception
	{
		// when
		final String type = asPromotedSortsDataHandler.getTypeCode();

		// then
		assertEquals(type, AsPromotedSortModel._TYPECODE);
	}

	@Test
	public void loadNullData()
	{
		// when
		final ListModel<PromotedSortEditorData> listModel = asPromotedSortsDataHandler.loadData(null, null, null);

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
		final ListModel<PromotedSortEditorData> listModel = asPromotedSortsDataHandler.loadData(new ArrayList<>(), searchResultData,
				new HashMap<>());

		// then
		assertEquals(0, listModel.getSize());
	}

	@Test
	public void loadDataFromInitialValue() throws Exception
	{
		// given
		final AsPromotedSortModel promotedSortModel1 = createPromotedSortModel1();
		final AsPromotedSortModel promotedSortModel2 = createPromotedSortModel2();
		final List<AsPromotedSortModel> initialValue = Arrays.asList(promotedSortModel1, promotedSortModel2);

		// when
		final ListModel<PromotedSortEditorData> listModel = asPromotedSortsDataHandler.loadData(initialValue, null,
				new HashMap<>());

		// then
		assertEquals(2, listModel.getSize());

		final PromotedSortEditorData promotedSort1 = listModel.getElementAt(0);
		assertEquals(UID_1, promotedSort1.getUid());
		assertEquals(CODE_1, promotedSort1.getCode());
		assertEquals(PRIORITY_1, promotedSort1.getPriority());
		assertFalse(promotedSort1.isOpen());
		assertFalse(promotedSort1.isOverride());
		assertFalse(promotedSort1.isInSearchResult());
		assertSame(promotedSortModel1, promotedSort1.getModel());

		final PromotedSortEditorData promotedSort2 = listModel.getElementAt(1);
		assertEquals(UID_2, promotedSort2.getUid());
		assertEquals(CODE_2, promotedSort2.getCode());
		assertEquals(PRIORITY_2, promotedSort2.getPriority());
		assertFalse(promotedSort2.isOpen());
		assertFalse(promotedSort2.isOverride());
		assertFalse(promotedSort2.isInSearchResult());
		assertSame(promotedSortModel2, promotedSort2.getModel());
	}

	@Test
	public void loadDataFromSearchResult() throws Exception
	{
		// given
		final Map<String, AsConfigurationHolder<AsPromotedSort, AbstractAsSortConfiguration>> searchProfileResultPromotedSorts = new LinkedHashMap<>();
		searchProfileResultPromotedSorts.put(CODE_1, createConfigurationHolder(createPromotedSortData1()));
		searchProfileResultPromotedSorts.put(CODE_2, createConfigurationHolder(createPromotedSortData2()));

		final SearchResultData searchResult = createSearchResult();
		final AsSearchResultData asSearchResult = searchResult.getAsSearchResult();
		asSearchResult.getSearchProfileResult().setPromotedSorts(searchProfileResultPromotedSorts);

		// when
		final ListModel<PromotedSortEditorData> listModel = asPromotedSortsDataHandler.loadData(null, searchResult,
				new HashMap<>());

		// then
		assertEquals(2, listModel.getSize());

		final PromotedSortEditorData promotedSort1 = listModel.getElementAt(0);
		assertEquals(UID_1, promotedSort1.getUid());
		assertEquals(CODE_1, promotedSort1.getCode());
		assertEquals(PRIORITY_1, promotedSort1.getPriority());
		assertFalse(promotedSort1.isOpen());
		assertFalse(promotedSort1.isOverride());
		assertFalse(promotedSort1.isInSearchResult());
		assertFalse(promotedSort1.isInSearchResult());
		assertNull(promotedSort1.getModel());

		final PromotedSortEditorData promotedSort2 = listModel.getElementAt(1);
		assertEquals(UID_2, promotedSort2.getUid());
		assertEquals(CODE_2, promotedSort2.getCode());
		assertEquals(PRIORITY_2, promotedSort2.getPriority());
		assertFalse(promotedSort2.isOpen());
		assertFalse(promotedSort2.isOverride());
		assertFalse(promotedSort2.isInSearchResult());
		assertNull(promotedSort2.getModel());
	}

	@Test
	public void loadDataFromSearchResultWithSorts() throws Exception
	{
		// given
		final Map<String, AsConfigurationHolder<AsPromotedSort, AbstractAsSortConfiguration>> searchProfileResultPromotedSorts = new LinkedHashMap<>();
		searchProfileResultPromotedSorts.put(CODE_1, createConfigurationHolder(createPromotedSortData1()));
		searchProfileResultPromotedSorts.put(CODE_2, createConfigurationHolder(createPromotedSortData2()));

		final AsSortData sortData1 = new AsSortData();
		sortData1.setCode(CODE_1);
		sortData1.setName(CODE_1);

		final AsSortData sortData2 = new AsSortData();
		sortData2.setCode(CODE_2);
		sortData2.setName(CODE_2);

		final SearchResultData searchResult = createSearchResult();
		final AsSearchResultData asSearchResult = searchResult.getAsSearchResult();
		asSearchResult.setAvailableSorts(Arrays.asList(sortData1, sortData2));
		asSearchResult.getSearchProfileResult().setPromotedSorts(searchProfileResultPromotedSorts);

		// when
		final ListModel<PromotedSortEditorData> listModel = asPromotedSortsDataHandler.loadData(null, searchResult,
				new HashMap<>());

		// then
		assertEquals(2, listModel.getSize());

		final PromotedSortEditorData promotedSort1 = listModel.getElementAt(0);
		assertEquals(UID_1, promotedSort1.getUid());
		assertEquals(CODE_1, promotedSort1.getCode());
		assertEquals(PRIORITY_1, promotedSort1.getPriority());
		assertFalse(promotedSort1.isOpen());
		assertFalse(promotedSort1.isOverride());
		assertTrue(promotedSort1.isInSearchResult());
		assertNull(promotedSort1.getModel());

		final PromotedSortEditorData promotedSort2 = listModel.getElementAt(1);
		assertEquals(UID_2, promotedSort2.getUid());
		assertEquals(CODE_2, promotedSort2.getCode());
		assertEquals(PRIORITY_2, promotedSort2.getPriority());
		assertFalse(promotedSort2.isOpen());
		assertFalse(promotedSort2.isOverride());
		assertTrue(promotedSort2.isInSearchResult());
		assertNull(promotedSort2.getModel());
	}

	@Test
	public void loadDataCombined() throws Exception
	{
		// given
		final AsPromotedSortModel promotedSortModel = createPromotedSortModel1();
		final List<AsPromotedSortModel> initialValue = Collections.singletonList(promotedSortModel);

		final Map<String, AsConfigurationHolder<AsPromotedSort, AbstractAsSortConfiguration>> searchProfileResultPromotedSorts = new LinkedHashMap<>();
		searchProfileResultPromotedSorts.put(CODE_1, createConfigurationHolder(createPromotedSortData1()));
		searchProfileResultPromotedSorts.put(CODE_2, createConfigurationHolder(createPromotedSortData2()));

		final SearchResultData searchResult = createSearchResult();
		final AsSearchResultData asSearchResult = searchResult.getAsSearchResult();
		asSearchResult.getSearchProfileResult().setPromotedSorts(searchProfileResultPromotedSorts);

		// when
		final ListModel<PromotedSortEditorData> listModel = asPromotedSortsDataHandler.loadData(initialValue, searchResult,
				new HashMap<>());

		// then
		assertEquals(2, listModel.getSize());

		final PromotedSortEditorData promotedSort1 = listModel.getElementAt(0);
		assertEquals(UID_1, promotedSort1.getUid());
		assertEquals(CODE_1, promotedSort1.getCode());
		assertEquals(PRIORITY_1, promotedSort1.getPriority());
		assertFalse(promotedSort1.isOverride());
		assertFalse(promotedSort1.isInSearchResult());
		assertSame(promotedSortModel, promotedSort1.getModel());

		final PromotedSortEditorData promotedSort2 = listModel.getElementAt(1);
		assertEquals(UID_2, promotedSort2.getUid());
		assertEquals(CODE_2, promotedSort2.getCode());
		assertEquals(PRIORITY_2, promotedSort2.getPriority());
		assertFalse(promotedSort2.isOverride());
		assertFalse(promotedSort2.isInSearchResult());
		assertNull(promotedSort2.getModel());
	}

	@Test
	public void getValue() throws Exception
	{
		// given
		final AsPromotedSortModel promotedSortModel = createPromotedSortModel1();
		final List<AsPromotedSortModel> initialValue = Collections.singletonList(promotedSortModel);

		final ListModel<PromotedSortEditorData> model = asPromotedSortsDataHandler.loadData(initialValue, null, new HashMap<>());

		// when
		final List<AsPromotedSortModel> value = asPromotedSortsDataHandler.getValue(model);

		// then
		assertNotNull(value);
		assertEquals(1, value.size());
		assertSame(promotedSortModel, value.get(0));
	}

	@Test
	public void getItemValue() throws Exception
	{
		// given
		final AsPromotedSortModel promotedSortModel = createPromotedSortModel1();
		final List<AsPromotedSortModel> initialValue = Collections.singletonList(promotedSortModel);

		final ListModel<PromotedSortEditorData> model = asPromotedSortsDataHandler.loadData(initialValue, null, new HashMap<>());

		// when
		final AsPromotedSortModel itemValue = asPromotedSortsDataHandler.getItemValue(model.getElementAt(0));

		// then
		assertSame(promotedSortModel, itemValue);
	}

	@Test
	public void getAttributeValue() throws Exception
	{
		// given
		final AsPromotedSortModel promotedSortModel = createPromotedSortModel1();
		final List<AsPromotedSortModel> initialValue = Collections.singletonList(promotedSortModel);

		final ListModel<PromotedSortEditorData> listModel = asPromotedSortsDataHandler.loadData(initialValue, null,
				new HashMap<>());
		final PromotedSortEditorData promotedSort = listModel.getElementAt(0);

		// when
		final Object priority = asPromotedSortsDataHandler.getAttributeValue(promotedSort, PRIORITY_ATTRIBUTE);

		// then
		assertEquals(PRIORITY_1, priority);
	}

	protected AsPromotedSortModel createPromotedSortModel1()
	{
		final AsPromotedSortModel promotedSortModel = new AsPromotedSortModel();
		promotedSortModel.setUid(UID_1);
		promotedSortModel.setCode(CODE_1);
		promotedSortModel.setPriority(PRIORITY_1);

		return promotedSortModel;
	}

	protected AsPromotedSortModel createPromotedSortModel2()
	{
		final AsPromotedSortModel promotedSortModel = new AsPromotedSortModel();
		promotedSortModel.setUid(UID_2);
		promotedSortModel.setCode(CODE_2);
		promotedSortModel.setPriority(PRIORITY_2);

		return promotedSortModel;
	}

	protected AsPromotedSort createPromotedSortData1()
	{
		final AsPromotedSort promotedSortData = new AsPromotedSort();
		promotedSortData.setUid(UID_1);
		promotedSortData.setCode(CODE_1);
		promotedSortData.setPriority(PRIORITY_1);

		return promotedSortData;
	}

	protected AsPromotedSort createPromotedSortData2()
	{
		final AsPromotedSort promotedSortData = new AsPromotedSort();
		promotedSortData.setUid(UID_2);
		promotedSortData.setCode(CODE_2);
		promotedSortData.setPriority(PRIORITY_2);

		return promotedSortData;
	}
}
