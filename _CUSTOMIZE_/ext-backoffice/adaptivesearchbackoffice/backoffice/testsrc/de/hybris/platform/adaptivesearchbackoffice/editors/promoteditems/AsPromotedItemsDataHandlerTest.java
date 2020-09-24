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
package de.hybris.platform.adaptivesearchbackoffice.editors.promoteditems;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.adaptivesearch.data.AbstractAsBoostItemConfiguration;
import de.hybris.platform.adaptivesearch.data.AsConfigurationHolder;
import de.hybris.platform.adaptivesearch.data.AsPromotedItem;
import de.hybris.platform.adaptivesearch.data.AsSearchResultData;
import de.hybris.platform.adaptivesearch.model.AsPromotedItemModel;
import de.hybris.platform.adaptivesearchbackoffice.data.PromotedItemEditorData;
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
public class AsPromotedItemsDataHandlerTest extends AbstractDataHandlerTest
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

	private AsPromotedItemsDataHandler asPromotedItemsDataProvider;

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

		asPromotedItemsDataProvider = new AsPromotedItemsDataHandler();

		asPromotedItemsDataProvider.setLabelService(labelService);
		asPromotedItemsDataProvider.setModelService(modelService);
	}

	@Test
	public void getTypeCode() throws Exception
	{
		// when
		final String type = asPromotedItemsDataProvider.getTypeCode();

		// then
		assertEquals(type, AsPromotedItemModel._TYPECODE);
	}

	@Test
	public void loadNullData()
	{
		// when
		final ListModel<PromotedItemEditorData> listModel = asPromotedItemsDataProvider.loadData(null, null, null);

		// then
		assertEquals(0, listModel.getSize());
	}

	@Test
	public void loadEmptyData()
	{
		// when
		final SearchResultData searchResultData = new SearchResultData();
		searchResultData.setAsSearchResult(new AsSearchResultData());

		final ListModel<PromotedItemEditorData> listModel = asPromotedItemsDataProvider.loadData(new ArrayList<>(),
				searchResultData, new HashMap<>());

		// then
		assertEquals(0, listModel.getSize());
	}

	@Test
	public void loadDataFromInitialValue() throws Exception
	{
		// given
		final AsPromotedItemModel promotedItemModel1 = createPromotedItemModel1();
		final AsPromotedItemModel promotedItemModel2 = createPromotedItemModel2();
		final List<AsPromotedItemModel> initialValue = Arrays.asList(promotedItemModel1, promotedItemModel2);

		// when
		final ListModel<PromotedItemEditorData> listModel = asPromotedItemsDataProvider.loadData(initialValue, null,
				new HashMap<>());

		// then
		assertEquals(2, listModel.getSize());

		final PromotedItemEditorData promotedItem1 = listModel.getElementAt(0);
		assertEquals(UID_1, promotedItem1.getUid());
		assertEquals(ITEM_PK_1, promotedItem1.getItemPk());
		assertFalse(promotedItem1.isOpen());
		assertFalse(promotedItem1.isOverride());
		assertFalse(promotedItem1.isInSearchResult());
		assertSame(promotedItemModel1, promotedItem1.getModel());

		final PromotedItemEditorData promotedItem2 = listModel.getElementAt(1);
		assertEquals(UID_2, promotedItem2.getUid());
		assertEquals(ITEM_PK_2, promotedItem2.getItemPk());
		assertFalse(promotedItem2.isOpen());
		assertFalse(promotedItem2.isOverride());
		assertFalse(promotedItem2.isInSearchResult());
		assertSame(promotedItemModel2, promotedItem2.getModel());
	}

	@Test
	public void loadDataFromSearchResult() throws Exception
	{
		// given
		final Map<PK, AsConfigurationHolder<AsPromotedItem, AbstractAsBoostItemConfiguration>> searchProfileResultPromotedItems = new LinkedHashMap<>();
		searchProfileResultPromotedItems.put(ITEM_PK_1, createConfigurationHolder(createPromotedItemData1()));
		searchProfileResultPromotedItems.put(ITEM_PK_2, createConfigurationHolder(createPromotedItemData2()));

		final SearchResultData searchResult = createSearchResult();
		final AsSearchResultData asSearchResult = searchResult.getAsSearchResult();
		asSearchResult.getSearchProfileResult().setPromotedItems(searchProfileResultPromotedItems);

		// when
		final ListModel<PromotedItemEditorData> listModel = asPromotedItemsDataProvider.loadData(null, searchResult,
				new HashMap<>());

		// then
		assertEquals(2, listModel.getSize());

		final PromotedItemEditorData promotedItem1 = listModel.getElementAt(0);
		assertEquals(UID_1, promotedItem1.getUid());
		assertEquals(ITEM_PK_1, promotedItem1.getItemPk());
		assertFalse(promotedItem1.isOpen());
		assertFalse(promotedItem1.isOverride());
		assertFalse(promotedItem1.isInSearchResult());
		assertNull(promotedItem1.getModel());

		final PromotedItemEditorData promotedItem2 = listModel.getElementAt(1);
		assertEquals(UID_2, promotedItem2.getUid());
		assertEquals(ITEM_PK_2, promotedItem2.getItemPk());
		assertFalse(promotedItem2.isOpen());
		assertFalse(promotedItem2.isOverride());
		assertFalse(promotedItem2.isInSearchResult());
		assertNull(promotedItem2.getModel());
	}

	@Test
	public void loadDataCombined() throws Exception
	{
		// given
		final AsPromotedItemModel promotedItemModel = createPromotedItemModel1();
		final List<AsPromotedItemModel> initialValue = Arrays.asList(promotedItemModel);

		final Map<PK, AsConfigurationHolder<AsPromotedItem, AbstractAsBoostItemConfiguration>> searchProfileResultPromotedItems = new LinkedHashMap<>();
		searchProfileResultPromotedItems.put(ITEM_PK_1, createConfigurationHolder(createPromotedItemData1()));
		searchProfileResultPromotedItems.put(ITEM_PK_2, createConfigurationHolder(createPromotedItemData2()));

		final SearchResultData searchResult = createSearchResult();
		final AsSearchResultData asSearchResult = searchResult.getAsSearchResult();
		asSearchResult.getSearchProfileResult().setPromotedItems(searchProfileResultPromotedItems);

		// when
		final ListModel<PromotedItemEditorData> listModel = asPromotedItemsDataProvider.loadData(initialValue, searchResult,
				new HashMap<>());

		// then
		assertEquals(2, listModel.getSize());

		final PromotedItemEditorData promotedItem1 = listModel.getElementAt(0);
		assertEquals(UID_1, promotedItem1.getUid());
		assertEquals(ITEM_PK_1, promotedItem1.getItemPk());
		assertFalse(promotedItem1.isOpen());
		assertFalse(promotedItem1.isOverride());
		assertFalse(promotedItem1.isInSearchResult());
		assertSame(promotedItemModel, promotedItem1.getModel());

		final PromotedItemEditorData promotedItem2 = listModel.getElementAt(1);
		assertEquals(UID_2, promotedItem2.getUid());
		assertEquals(ITEM_PK_2, promotedItem2.getItemPk());
		assertFalse(promotedItem2.isOpen());
		assertFalse(promotedItem2.isOverride());
		assertFalse(promotedItem2.isInSearchResult());
		assertNull(promotedItem2.getModel());
	}

	@Test
	public void getValue() throws Exception
	{
		// given
		final AsPromotedItemModel promotedItemModel = createPromotedItemModel1();
		final List<AsPromotedItemModel> initialValue = Arrays.asList(promotedItemModel);

		final ListModel<PromotedItemEditorData> model = asPromotedItemsDataProvider.loadData(initialValue, null, new HashMap<>());

		// when
		final List<AsPromotedItemModel> value = asPromotedItemsDataProvider.getValue(model);

		// then
		assertNotNull(value);
		assertEquals(1, value.size());
		assertSame(promotedItemModel, value.get(0));
	}

	@Test
	public void getItemValue() throws Exception
	{
		// given
		final AsPromotedItemModel promotedItemModel = createPromotedItemModel1();
		final List<AsPromotedItemModel> initialValue = Arrays.asList(promotedItemModel);

		final ListModel<PromotedItemEditorData> model = asPromotedItemsDataProvider.loadData(initialValue, null, new HashMap<>());

		// when
		final AsPromotedItemModel itemValue = asPromotedItemsDataProvider.getItemValue(model.getElementAt(0));

		// then
		assertSame(promotedItemModel, itemValue);
	}


	@Test
	public void getAttributeValue() throws Exception
	{
		// given
		final AsPromotedItemModel promotedItemModel = createPromotedItemModel1();
		final List<AsPromotedItemModel> initialValue = Arrays.asList(promotedItemModel);

		final ListModel<PromotedItemEditorData> listModel = asPromotedItemsDataProvider.loadData(initialValue, null,
				new HashMap<>());
		final PromotedItemEditorData promotedItem = listModel.getElementAt(0);

		// when
		final Object priority = asPromotedItemsDataProvider.getAttributeValue(promotedItem, ITEM_PK_ATTRIBUTE);

		// then
		assertEquals(ITEM_PK_1, priority);
	}

	protected AsPromotedItemModel createPromotedItemModel1()
	{
		final AsPromotedItemModel promotedItemModel = new AsPromotedItemModel();
		promotedItemModel.setUid(UID_1);
		promotedItemModel.setItem(item1);

		return promotedItemModel;
	}

	protected AsPromotedItemModel createPromotedItemModel2()
	{
		final AsPromotedItemModel promotedItemModel = new AsPromotedItemModel();
		promotedItemModel.setUid(UID_2);
		promotedItemModel.setItem(item2);

		return promotedItemModel;
	}

	protected AsPromotedItem createPromotedItemData1()
	{
		final AsPromotedItem promotedItemData = new AsPromotedItem();
		promotedItemData.setUid(UID_1);
		promotedItemData.setItemPk(ITEM_PK_1);

		return promotedItemData;
	}

	protected AsPromotedItem createPromotedItemData2()
	{
		final AsPromotedItem promotedItemData = new AsPromotedItem();
		promotedItemData.setUid(UID_2);
		promotedItemData.setItemPk(ITEM_PK_2);

		return promotedItemData;
	}
}
