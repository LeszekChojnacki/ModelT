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
package de.hybris.platform.warehousingbackoffice.widgets.atp;


import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.testing.AbstractWidgetUnitTest;
import com.hybris.cockpitng.testing.annotation.DeclaredViewEvent;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import de.hybris.platform.warehousingbackoffice.dtos.AtpFormDto;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;

import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@DeclaredViewEvent(componentID = AtpFormController.SEARCH_ATP, eventName = Events.ON_CLICK)
public class AtpFormControllerTest extends AbstractWidgetUnitTest<AtpFormController>
{
	private static final String PRODUCT_CODE = "Product_Code";
	@Spy
	private AtpFormController atpFormController;
	@Mock
	private BaseStoreService baseStoreService;
	@Mock
	private WidgetInstanceManager widgetInstanceManager;
	@Mock
	private BaseStoreModel electronicsStore;
	@Mock
	private PointOfServiceModel electronicsPoS;
	@Mock
	private PointOfServiceModel powertoolsPoS;
	@Mock
	private Component component;
	@Mock
	private Combobox baseStores;
	@Mock
	private Comboitem selectedBaseStoreComboItem;
	@Mock
	private Combobox pointOfServices;
	@Mock
	private Comboitem selectedPoSComboItem;
	@Mock
	private Editor product;
	@Mock
	private ProductModel productModel;

	@Before
	public void setUpController()
	{
		atpFormController.setWidgetInstanceManager(widgetInstanceManager);
		atpFormController.setBaseStoreService(baseStoreService);

		when(product.getValue()).thenReturn(productModel);
		when(productModel.getCode()).thenReturn(PRODUCT_CODE);
		when(baseStores.getSelectedItem()).thenReturn(selectedBaseStoreComboItem);
		when(selectedBaseStoreComboItem.getValue()).thenReturn(electronicsStore);
		when(electronicsStore.getPointsOfService()).thenReturn(Collections.singletonList(electronicsPoS));
		when(pointOfServices.getSelectedItem()).thenReturn(selectedPoSComboItem);
		when(baseStoreService.getAllBaseStores()).thenReturn(Collections.singletonList(electronicsStore));
	}

	@Test
	public void testInitialize()
	{
		//When
		atpFormController.initialize(component);

		//Then
		verify(atpFormController).refreshForm();
	}

	@Test
	public void testRefreshForm()
	{
		//When
		atpFormController.refreshForm();

		//Then
		verify(baseStores).setModel(any());
		verify(atpFormController).addListeners();
	}

	@Test
	public void testPerformSearch()
	{
		//When
		atpFormController.performSearchOperation();

		//Then
		verify(atpFormController).validateForm();
		verify(widgetInstanceManager).sendOutput(eq(AtpFormController.OUT_CONFIRM), any(AtpFormDto.class));
	}

	@Test
	public void testAddListeners()
	{
		//When
		atpFormController.addListeners();

		//Then
		verify(baseStores).addEventListener(eq(AtpFormController.ON_STORE_CHANGE), any(EventListener.class));
		verify(baseStores).addEventListener(eq(AtpFormController.ON_LATER_STORE_CHANGE), any(EventListener.class));
		verify(pointOfServices).addEventListener(eq(AtpFormController.ON_POS_CHANGE), any(EventListener.class));
		verify(pointOfServices).addEventListener(eq(AtpFormController.ON_LATER_POS_CHANGE), any(EventListener.class));
	}

	@Test(expected = WrongValueException.class)
	public void testValidateFormsWhenNoProductSelected()
	{
		//Given
		when(product.getValue()).thenReturn(null);

		//When
		atpFormController.validateForm();
	}

	@Test(expected = WrongValueException.class)
	public void testValidateFormsWhenNoBaseStoreSelected()
	{
		//Given
		when(baseStores.getSelectedItem()).thenReturn(null);

		//When
		atpFormController.validateForm();
	}

	@Test(expected = WrongValueException.class)
	public void testValidateFormsWhenBaseStoreSelectStatementSelected()
	{
		//Given
		when(selectedBaseStoreComboItem.getValue()).thenReturn("Test");

		//When
		atpFormController.validateForm();
	}

	@Test(expected = WrongValueException.class)
	public void testValidateFormsWhenPoSFromDiffBaseStoreSelected()
	{
		//Given
		when(selectedPoSComboItem.getValue()).thenReturn(powertoolsPoS);

		//When
		atpFormController.validateForm();
	}


	@Override
	protected AtpFormController getWidgetController()
	{
		return atpFormController;
	}

}
