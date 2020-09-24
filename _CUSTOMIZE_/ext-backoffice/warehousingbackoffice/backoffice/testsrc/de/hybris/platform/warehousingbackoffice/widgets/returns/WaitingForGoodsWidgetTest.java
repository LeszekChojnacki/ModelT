/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousingbackoffice.widgets.returns;

import com.hybris.backoffice.navigation.NavigationNode;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchInitContext;
import com.hybris.backoffice.widgets.advancedsearch.impl.SearchConditionData;
import com.hybris.backoffice.widgets.advancedsearch.impl.SearchConditionDataList;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.cockpitng.testing.annotation.DeclaredInput;
import de.hybris.platform.returns.model.ReturnRequestModel;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Tests the functionality of the {@link WaitingForGoodsFilterController} and whether it filters and displays
 * orders in the WAIT {@link de.hybris.platform.basecommerce.enums.ReturnStatus}.
 */
@DeclaredInput(value = WaitingForGoodsFilterController.SOCKET_IN_NODE_SELECTED, socketType = NavigationNode.class)
public class WaitingForGoodsWidgetTest extends AbstractReturnWidgetTest<WaitingForGoodsFilterController>
{
	private final WaitingForGoodsFilterController controller = new WaitingForGoodsFilterController();

	@Before
	public void setup()
	{
		setupController();

		when(navigationNode.getId()).thenReturn(WaitingForGoodsFilterController.NAVIGATION_NODE_ID);
	}

	@Test
	public void testNullNavigationNode()
	{
		testInputNullNavigationNode(WaitingForGoodsFilterController.SOCKET_IN_NODE_SELECTED,
				WaitingForGoodsFilterController.SOCKET_OUT_CONTEXT);
		assertNull(controller.getAdvancedSearchData());
	}

	@Test
	public void testNullSearchData() throws TypeNotFoundException
	{
		testInputNullSearchData(WaitingForGoodsFilterController.SOCKET_IN_NODE_SELECTED,
				WaitingForGoodsFilterController.SOCKET_OUT_CONTEXT);
		assertNull(controller.getAdvancedSearchData());
	}

	/**
	 * Tests the following filter: (ReturnStatus=WAIT)
	 */
	@Test
	public void testWaitReturnStatusSearchData()
	{
		executeInputSocketEvent(WaitingForGoodsFilterController.SOCKET_IN_NODE_SELECTED, navigationNode);

		assertNotNull(controller.getAdvancedSearchData());
		assertEquals(ReturnRequestModel._TYPECODE, controller.getAdvancedSearchData().getTypeCode());

		final List<SearchConditionData> conditions = controller.getAdvancedSearchData().getConditions(ReturnRequestModel.STATUS);
		assertFalse(conditions.isEmpty());
		assertEquals(1, ((SearchConditionDataList) conditions.get(0)).getConditions().size());

		final String firstField = ((SearchConditionDataList) conditions.get(0)).getConditions().get(0).getFieldType().getName();
		assertEquals(ReturnRequestModel.STATUS, firstField);

		final String firstFieldValue = ((SearchConditionDataList) conditions.get(0)).getConditions().get(0).getValue().toString();
		assertEquals("WAIT", firstFieldValue);

		verify(widgetInstanceManager).sendOutput(eq(WaitingForGoodsFilterController.SOCKET_OUT_CONTEXT), any(
				AdvancedSearchInitContext.class));
	}

	@Override
	protected WaitingForGoodsFilterController getWidgetController()
	{
		return controller;
	}
}
