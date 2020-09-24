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
package de.hybris.platform.adaptivesearchbackoffice.editors.excludeditems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.adaptivesearch.data.AbstractAsBoostItemConfiguration;
import de.hybris.platform.adaptivesearch.data.AsConfigurationHolder;
import de.hybris.platform.adaptivesearch.data.AsExcludedItem;
import de.hybris.platform.adaptivesearch.data.AsSearchResultData;
import de.hybris.platform.adaptivesearch.model.AsExcludedItemModel;
import de.hybris.platform.adaptivesearchbackoffice.data.ExcludedItemEditorData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchResultData;
import de.hybris.platform.adaptivesearchbackoffice.editors.configurablemultireference.AbstractDataHandlerTest;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zkoss.zul.ListModel;

import com.hybris.cockpitng.labels.LabelService;


@UnitTest
public class AsExcludedItemsDataHandlerTest extends AbstractDataHandlerTest
{
	private static final String ITEM_PK_ATTRIBUTE = "itemPk";

	private static final String UID_1 = "uid1";
	private static final PK ITEM_PK_1 = PK.fromLong(1);
	private static final String LABEL_1 = "label1";

	private static final String UID_2 = "uid2";
	private static final PK ITEM_PK_2 = PK.fromLong(2);
	private static final String LABEL_2 = "label2";

	@Mock
	private ItemModel item1;

	@Mock
	private ItemModel item2;

	@Mock
	private LabelService labelService;

	@Mock
	private ModelService modelService;

	private AsExcludedItemsDataHandler asExcludedItemsDataProvider;

	@Before
	public void setUp() throws Exception
	{
		MockitoAnnotations.initMocks(this);

		when(item1.getPk()).thenReturn(ITEM_PK_1);
		when(item2.getPk()).thenReturn(ITEM_PK_2);

		when(labelService.getObjectLabel(item1)).thenReturn(LABEL_1);
		when(labelService.getObjectLabel(item2)).thenReturn(LABEL_2);

		when(modelService.get(ITEM_PK_1)).thenReturn(item1);
		when(modelService.get(ITEM_PK_2)).thenReturn(item2);

		asExcludedItemsDataProvider = new AsExcludedItemsDataHandler();

		asExcludedItemsDataProvider.setLabelService(labelService);
		asExcludedItemsDataProvider.setModelService(modelService);
	}

	@Test
	public void getTypeCode() throws Exception
	{
		// when
		final String type = asExcludedItemsDataProvider.getTypeCode();

		// then
		assertEquals(type, AsExcludedItemModel._TYPECODE);
	}

	@Test
	public void loadNullData()
	{
		// when
		final ListModel<ExcludedItemEditorData> listModel = asExcludedItemsDataProvider.loadData(null, null, null);

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
		final ListModel<ExcludedItemEditorData> listModel = asExcludedItemsDataProvider.loadData(new ArrayList<>(),
				searchResultData, new HashMap<>());

		// then
		assertEquals(0, listModel.getSize());
	}

	@Test
	public void loadDataFromInitialValue() throws Exception
	{
		// given
		final AsExcludedItemModel excludedItemModel1 = createExcludedItemModel1();
		final AsExcludedItemModel excludedItemModel2 = createExcludedItemModel2();
		final List<AsExcludedItemModel> initialValue = Arrays.asList(excludedItemModel1, excludedItemModel2);

		// when
		final ListModel<ExcludedItemEditorData> listModel = asExcludedItemsDataProvider.loadData(initialValue, null,
				new HashMap<>());

		// then
		assertEquals(2, listModel.getSize());

		final ExcludedItemEditorData excludedItem1 = listModel.getElementAt(0);
		assertEquals(UID_1, excludedItem1.getUid());
		assertEquals(ITEM_PK_1, excludedItem1.getItemPk());
		assertFalse(excludedItem1.isOpen());
		assertFalse(excludedItem1.isOverride());
		assertFalse(excludedItem1.isInSearchResult());
		assertSame(excludedItemModel1, excludedItem1.getModel());

		final ExcludedItemEditorData excludedItem2 = listModel.getElementAt(1);
		assertEquals(UID_2, excludedItem2.getUid());
		assertEquals(ITEM_PK_2, excludedItem2.getItemPk());
		assertFalse(excludedItem2.isOpen());
		assertFalse(excludedItem2.isOverride());
		assertFalse(excludedItem2.isInSearchResult());
		assertSame(excludedItemModel2, excludedItem2.getModel());
	}

	@Test
	public void loadDataFromSearchResult() throws Exception
	{
		// given
		final Map<PK, AsConfigurationHolder<AsExcludedItem, AbstractAsBoostItemConfiguration>> searchProfileResultExcludedItems = new LinkedHashMap<>();
		searchProfileResultExcludedItems.put(ITEM_PK_1, createConfigurationHolder(createExcludedItemData1()));
		searchProfileResultExcludedItems.put(ITEM_PK_2, createConfigurationHolder(createExcludedItemData2()));

		final SearchResultData searchResult = createSearchResult();
		final AsSearchResultData asSearchResult = searchResult.getAsSearchResult();
		asSearchResult.getSearchProfileResult().setExcludedItems(searchProfileResultExcludedItems);

		// when
		final ListModel<ExcludedItemEditorData> listModel = asExcludedItemsDataProvider.loadData(null, searchResult,
				new HashMap<>());

		// then
		assertEquals(2, listModel.getSize());

		final ExcludedItemEditorData excludedItem1 = listModel.getElementAt(0);
		assertEquals(UID_1, excludedItem1.getUid());
		assertEquals(ITEM_PK_1, excludedItem1.getItemPk());
		assertFalse(excludedItem1.isOpen());
		assertFalse(excludedItem1.isOverride());
		assertFalse(excludedItem1.isInSearchResult());
		assertNull(excludedItem1.getModel());

		final ExcludedItemEditorData excludedItem2 = listModel.getElementAt(1);
		assertEquals(UID_2, excludedItem2.getUid());
		assertEquals(ITEM_PK_2, excludedItem2.getItemPk());
		assertFalse(excludedItem2.isOpen());
		assertFalse(excludedItem2.isOverride());
		assertFalse(excludedItem2.isInSearchResult());
		assertNull(excludedItem2.getModel());
	}

	@Test
	public void loadDataCombined() throws Exception
	{
		// given
		final AsExcludedItemModel excludedItemModel = createExcludedItemModel1();
		final List<AsExcludedItemModel> initialValue = Arrays.asList(excludedItemModel);

		final Map<PK, AsConfigurationHolder<AsExcludedItem, AbstractAsBoostItemConfiguration>> searchProfileResultExcludedItems = new LinkedHashMap<>();
		searchProfileResultExcludedItems.put(ITEM_PK_1, createConfigurationHolder(createExcludedItemData1()));
		searchProfileResultExcludedItems.put(ITEM_PK_2, createConfigurationHolder(createExcludedItemData2()));

		final SearchResultData searchResult = createSearchResult();
		final AsSearchResultData asSearchResult = searchResult.getAsSearchResult();
		asSearchResult.getSearchProfileResult().setExcludedItems(searchProfileResultExcludedItems);

		// when
		final ListModel<ExcludedItemEditorData> listModel = asExcludedItemsDataProvider.loadData(initialValue, searchResult,
				new HashMap<>());

		// then
		assertEquals(2, listModel.getSize());

		final ExcludedItemEditorData excludedItem1 = listModel.getElementAt(0);
		assertEquals(UID_1, excludedItem1.getUid());
		assertEquals(ITEM_PK_1, excludedItem1.getItemPk());
		assertFalse(excludedItem1.isOpen());
		assertFalse(excludedItem1.isOverride());
		assertFalse(excludedItem1.isInSearchResult());
		assertSame(excludedItemModel, excludedItem1.getModel());

		final ExcludedItemEditorData excludedItem2 = listModel.getElementAt(1);
		assertEquals(UID_2, excludedItem2.getUid());
		assertEquals(ITEM_PK_2, excludedItem2.getItemPk());
		assertFalse(excludedItem2.isOpen());
		assertFalse(excludedItem2.isOverride());
		assertFalse(excludedItem2.isInSearchResult());
		assertNull(excludedItem2.getModel());
	}

	@Test
	public void getValue() throws Exception
	{
		// given
		final AsExcludedItemModel excludedItemModel = createExcludedItemModel1();
		final List<AsExcludedItemModel> initialValue = Arrays.asList(excludedItemModel);

		final ListModel<ExcludedItemEditorData> model = asExcludedItemsDataProvider.loadData(initialValue, null, new HashMap<>());

		// when
		final List<AsExcludedItemModel> value = asExcludedItemsDataProvider.getValue(model);

		// then
		assertNotNull(value);
		assertEquals(1, value.size());
		assertSame(excludedItemModel, value.get(0));
	}

	@Test
	public void getItemValue() throws Exception
	{
		// given
		final AsExcludedItemModel excludedItemModel = createExcludedItemModel1();
		final List<AsExcludedItemModel> initialValue = Arrays.asList(excludedItemModel);

		final ListModel<ExcludedItemEditorData> model = asExcludedItemsDataProvider.loadData(initialValue, null, new HashMap<>());

		// when
		final AsExcludedItemModel itemValue = asExcludedItemsDataProvider.getItemValue(model.getElementAt(0));

		// then
		assertSame(excludedItemModel, itemValue);
	}

	@Test
	public void getAttributeValue() throws Exception
	{
		// given
		final AsExcludedItemModel excludedItemModel = createExcludedItemModel1();
		final List<AsExcludedItemModel> initialValue = Arrays.asList(excludedItemModel);

		final ListModel<ExcludedItemEditorData> listModel = asExcludedItemsDataProvider.loadData(initialValue, null,
				new HashMap<>());
		final ExcludedItemEditorData excludedItem = listModel.getElementAt(0);

		// when
		final Object priority = asExcludedItemsDataProvider.getAttributeValue(excludedItem, ITEM_PK_ATTRIBUTE);

		// then
		assertEquals(ITEM_PK_1, priority);
	}

	protected AsExcludedItemModel createExcludedItemModel1()
	{
		final AsExcludedItemModel excludedItemModel = new AsExcludedItemModel();
		excludedItemModel.setUid(UID_1);
		excludedItemModel.setItem(item1);

		return excludedItemModel;
	}

	protected AsExcludedItemModel createExcludedItemModel2()
	{
		final AsExcludedItemModel excludedItemModel = new AsExcludedItemModel();
		excludedItemModel.setUid(UID_2);
		excludedItemModel.setItem(item2);

		return excludedItemModel;
	}

	protected AsExcludedItem createExcludedItemData1()
	{
		final AsExcludedItem excludedItemData = new AsExcludedItem();
		excludedItemData.setUid(UID_1);
		excludedItemData.setItemPk(ITEM_PK_1);

		return excludedItemData;
	}

	protected AsExcludedItem createExcludedItemData2()
	{
		final AsExcludedItem excludedItemData = new AsExcludedItem();
		excludedItemData.setUid(UID_2);
		excludedItemData.setItemPk(ITEM_PK_2);

		return excludedItemData;
	}
}
