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
package de.hybris.platform.adaptivesearchbackoffice.editors.boostrules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.adaptivesearch.data.AbstractAsBoostRuleConfiguration;
import de.hybris.platform.adaptivesearch.data.AsBoostRule;
import de.hybris.platform.adaptivesearch.data.AsConfigurationHolder;
import de.hybris.platform.adaptivesearch.data.AsSearchResultData;
import de.hybris.platform.adaptivesearch.enums.AsBoostType;
import de.hybris.platform.adaptivesearch.model.AsBoostRuleModel;
import de.hybris.platform.adaptivesearchbackoffice.data.BoostRuleEditorData;
import de.hybris.platform.adaptivesearchbackoffice.data.SearchResultData;
import de.hybris.platform.adaptivesearchbackoffice.editors.configurablemultireference.AbstractDataHandlerTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.zkoss.zul.ListModel;


@UnitTest
public class AsBoostRulesDataHandlerTest extends AbstractDataHandlerTest
{
	private static final String BOOST_TYPE_ATTRIBUTE = "boostType";

	private static final String UID_1 = "uid1";
	private static final String INDEX_PROPERTY_1 = "property1";
	private static final AsBoostType BOOST_TYPE_1 = AsBoostType.MULTIPLICATIVE;
	private static final Float BOOST_1 = Float.valueOf(1.2f);

	private static final String UID_2 = "uid2";
	private static final String INDEX_PROPERTY_2 = "property2";
	private static final AsBoostType BOOST_TYPE_2 = AsBoostType.ADDITIVE;
	private static final Float BOOST_2 = Float.valueOf(1.6f);

	private AsBoostRulesDataHandler asBoostRulesDataProvider;

	@Before
	public void setUp() throws Exception
	{
		MockitoAnnotations.initMocks(this);

		asBoostRulesDataProvider = new AsBoostRulesDataHandler();
	}

	@Test
	public void getTypeCode() throws Exception
	{
		// when
		final String type = asBoostRulesDataProvider.getTypeCode();

		// then
		assertEquals(type, AsBoostRuleModel._TYPECODE);
	}

	@Test
	public void loadNullData()
	{
		// when
		final ListModel<BoostRuleEditorData> listModel = asBoostRulesDataProvider.loadData(null, null, null);

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
		final ListModel<BoostRuleEditorData> listModel = asBoostRulesDataProvider.loadData(new ArrayList<>(), searchResult,
				new HashMap<>());

		// then
		assertEquals(0, listModel.getSize());
	}

	@Test
	public void loadDataFromInitialValue() throws Exception
	{
		// given
		final AsBoostRuleModel boostRuleModel1 = createBoostRuleModel1();
		final AsBoostRuleModel boostRuleModel2 = createBoostRuleModel2();
		final List<AsBoostRuleModel> initialValue = Arrays.asList(boostRuleModel1, boostRuleModel2);

		// when
		final ListModel<BoostRuleEditorData> listModel = asBoostRulesDataProvider.loadData(initialValue, null, new HashMap<>());

		// then
		assertEquals(2, listModel.getSize());

		final BoostRuleEditorData boostRule1 = listModel.getElementAt(0);
		assertEquals(UID_1, boostRule1.getUid());
		assertEquals(INDEX_PROPERTY_1, boostRule1.getIndexProperty());
		assertEquals(BOOST_TYPE_1, boostRule1.getBoostType());
		assertEquals(BOOST_1, boostRule1.getBoost());
		assertFalse(boostRule1.isOpen());
		assertFalse(boostRule1.isOverride());
		assertFalse(boostRule1.isInSearchResult());
		assertSame(boostRuleModel1, boostRule1.getModel());

		final BoostRuleEditorData boostRule2 = listModel.getElementAt(1);
		assertEquals(UID_2, boostRule2.getUid());
		assertEquals(INDEX_PROPERTY_2, boostRule2.getIndexProperty());
		assertEquals(BOOST_TYPE_2, boostRule2.getBoostType());
		assertEquals(BOOST_2, boostRule2.getBoost());
		assertFalse(boostRule2.isOpen());
		assertFalse(boostRule2.isOverride());
		assertFalse(boostRule2.isInSearchResult());
		assertSame(boostRuleModel2, boostRule2.getModel());
	}

	@Test
	public void loadDataFromSearchResult() throws Exception
	{
		// given
		final List<AsConfigurationHolder<AsBoostRule, AbstractAsBoostRuleConfiguration>> searchProfileResultBoostRules = Arrays
				.asList(createConfigurationHolder(createBoostRuleData1()), createConfigurationHolder(createBoostRuleData2()));

		final SearchResultData searchResult = createSearchResult();
		final AsSearchResultData asSearchResult = searchResult.getAsSearchResult();
		asSearchResult.getSearchProfileResult().setBoostRules(searchProfileResultBoostRules);

		// when
		final ListModel<BoostRuleEditorData> listModel = asBoostRulesDataProvider.loadData(null, searchResult, new HashMap<>());

		// then
		assertEquals(2, listModel.getSize());

		final BoostRuleEditorData boostRule1 = listModel.getElementAt(0);
		assertEquals(UID_1, boostRule1.getUid());
		assertEquals(INDEX_PROPERTY_1, boostRule1.getIndexProperty());
		assertEquals(BOOST_TYPE_1, boostRule1.getBoostType());
		assertEquals(BOOST_1, boostRule1.getBoost());
		assertFalse(boostRule1.isOpen());
		assertFalse(boostRule1.isOverride());
		assertFalse(boostRule1.isInSearchResult());
		assertNull(boostRule1.getModel());

		final BoostRuleEditorData boostRule2 = listModel.getElementAt(1);
		assertEquals(UID_2, boostRule2.getUid());
		assertEquals(INDEX_PROPERTY_2, boostRule2.getIndexProperty());
		assertEquals(BOOST_TYPE_2, boostRule2.getBoostType());
		assertEquals(BOOST_2, boostRule2.getBoost());
		assertFalse(boostRule2.isOpen());
		assertFalse(boostRule2.isOverride());
		assertFalse(boostRule2.isInSearchResult());
		assertNull(boostRule2.getModel());
	}

	@Test
	public void loadDataCombined() throws Exception
	{
		// given
		final AsBoostRuleModel boostRuleModel = createBoostRuleModel1();
		final List<AsBoostRuleModel> initialValue = Arrays.asList(boostRuleModel);

		final List<AsConfigurationHolder<AsBoostRule, AbstractAsBoostRuleConfiguration>> searchProfileResultBoostRules = Arrays
				.asList(createConfigurationHolder(createBoostRuleData1()), createConfigurationHolder(createBoostRuleData2()));

		final SearchResultData searchResult = createSearchResult();
		final AsSearchResultData asSearchResult = searchResult.getAsSearchResult();
		asSearchResult.getSearchProfileResult().setBoostRules(searchProfileResultBoostRules);

		// when
		final ListModel<BoostRuleEditorData> listModel = asBoostRulesDataProvider.loadData(initialValue, searchResult,
				new HashMap<>());

		// then
		assertEquals(2, listModel.getSize());

		final BoostRuleEditorData boostRule1 = listModel.getElementAt(0);
		assertEquals(UID_1, boostRule1.getUid());
		assertEquals(INDEX_PROPERTY_1, boostRule1.getIndexProperty());
		assertEquals(BOOST_TYPE_1, boostRule1.getBoostType());
		assertEquals(BOOST_1, boostRule1.getBoost());
		assertFalse(boostRule1.isOpen());
		assertFalse(boostRule1.isOverride());
		assertFalse(boostRule1.isInSearchResult());
		assertSame(boostRuleModel, boostRule1.getModel());

		final BoostRuleEditorData boostRule2 = listModel.getElementAt(1);
		assertEquals(UID_2, boostRule2.getUid());
		assertEquals(INDEX_PROPERTY_2, boostRule2.getIndexProperty());
		assertEquals(BOOST_TYPE_2, boostRule2.getBoostType());
		assertEquals(BOOST_2, boostRule2.getBoost());
		assertFalse(boostRule2.isOpen());
		assertFalse(boostRule2.isOverride());
		assertFalse(boostRule2.isInSearchResult());
		assertNull(boostRule2.getModel());
	}

	@Test
	public void getValue() throws Exception
	{
		// given
		final AsBoostRuleModel boostRuleModel = createBoostRuleModel1();
		final List<AsBoostRuleModel> initialValue = Arrays.asList(boostRuleModel);

		final ListModel<BoostRuleEditorData> model = asBoostRulesDataProvider.loadData(initialValue, null, new HashMap<>());

		// when
		final List<AsBoostRuleModel> value = asBoostRulesDataProvider.getValue(model);

		// then
		assertNotNull(value);
		assertEquals(1, value.size());
		assertSame(boostRuleModel, value.get(0));
	}

	@Test
	public void getItemValue() throws Exception
	{
		// given
		final AsBoostRuleModel boostRuleModel = createBoostRuleModel1();
		final List<AsBoostRuleModel> initialValue = Arrays.asList(boostRuleModel);

		final ListModel<BoostRuleEditorData> model = asBoostRulesDataProvider.loadData(initialValue, null, new HashMap<>());

		// when
		final AsBoostRuleModel itemValue = asBoostRulesDataProvider.getItemValue(model.getElementAt(0));

		// then
		assertSame(boostRuleModel, itemValue);
	}

	@Test
	public void getAttributeValue() throws Exception
	{
		// given
		final AsBoostRuleModel boostRuleModel = createBoostRuleModel1();
		final List<AsBoostRuleModel> initialValue = Arrays.asList(boostRuleModel);

		final ListModel<BoostRuleEditorData> listModel = asBoostRulesDataProvider.loadData(initialValue, null, new HashMap<>());
		final BoostRuleEditorData boostRule = listModel.getElementAt(0);

		// when
		final Object boostType = asBoostRulesDataProvider.getAttributeValue(boostRule, BOOST_TYPE_ATTRIBUTE);

		// then
		assertEquals(BOOST_TYPE_1, boostType);
	}

	protected AsBoostRuleModel createBoostRuleModel1()
	{
		final AsBoostRuleModel boostRuleModel = new AsBoostRuleModel();
		boostRuleModel.setUid(UID_1);
		boostRuleModel.setIndexProperty(INDEX_PROPERTY_1);
		boostRuleModel.setBoostType(BOOST_TYPE_1);
		boostRuleModel.setBoost(BOOST_1);

		return boostRuleModel;
	}

	protected AsBoostRuleModel createBoostRuleModel2()
	{
		final AsBoostRuleModel boostRuleModel = new AsBoostRuleModel();
		boostRuleModel.setUid(UID_2);
		boostRuleModel.setIndexProperty(INDEX_PROPERTY_2);
		boostRuleModel.setBoostType(BOOST_TYPE_2);
		boostRuleModel.setBoost(BOOST_2);

		return boostRuleModel;
	}

	protected AsBoostRule createBoostRuleData1()
	{
		final AsBoostRule boostRuleData = new AsBoostRule();
		boostRuleData.setUid(UID_1);
		boostRuleData.setIndexProperty(INDEX_PROPERTY_1);
		boostRuleData.setBoostType(BOOST_TYPE_1);
		boostRuleData.setBoost(BOOST_1);

		return boostRuleData;
	}

	protected AsBoostRule createBoostRuleData2()
	{
		final AsBoostRule boostRuleData = new AsBoostRule();
		boostRuleData.setUid(UID_2);
		boostRuleData.setIndexProperty(INDEX_PROPERTY_2);
		boostRuleData.setBoostType(BOOST_TYPE_2);
		boostRuleData.setBoost(BOOST_2);

		return boostRuleData;
	}
}
