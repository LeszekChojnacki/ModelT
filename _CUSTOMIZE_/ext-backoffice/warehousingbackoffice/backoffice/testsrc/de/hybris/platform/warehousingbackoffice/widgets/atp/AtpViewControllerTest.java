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

import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.testing.AbstractWidgetUnitTest;
import com.hybris.cockpitng.testing.annotation.DeclaredInput;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.storelocator.model.PointOfServiceModel;
import de.hybris.platform.warehousing.atp.formula.services.AtpFormulaService;
import de.hybris.platform.warehousing.atp.services.impl.WarehousingCommerceStockService;
import de.hybris.platform.warehousing.model.AtpFormulaModel;
import de.hybris.platform.warehousingbackoffice.dtos.AtpFormDto;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Listbox;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@DeclaredInput(value = AtpViewController.IN_SOCKET, socketType = AtpFormDto.class)
public class AtpViewControllerTest extends AbstractWidgetUnitTest<AtpViewController>
{
	private static final String PRODUCT_CODE = "Camera_Code";
	@Spy
	private AtpViewController atpViewController;
	@Mock
	private AtpFormulaService atpFormulaService;
	@Mock
	private WidgetInstanceManager widgetInstanceManager;
	@Mock
	private Component widgetComponent;
	@Mock
	private WarehousingCommerceStockService warehousingCommerceStockService;
	@Mock
	private AtpFormDto atpFormDto;
	@Mock
	private BaseStoreModel electronicsStore;
	@Mock
	private PointOfServiceModel electronicsPoS;
	@Mock
	private ProductModel camera;
	@Mock
	private AtpFormulaModel defaultFormula;
	@Mock
	private AtpFormulaModel addtlFormula;
	@Mock
	private Listbox atpListView;

	@Before
	public void setUpController()
	{
		atpViewController.setWidgetInstanceManager(widgetInstanceManager);
		atpViewController.setAtpFormulaService(atpFormulaService);
		atpViewController.setWarehousingCommerceStockService(warehousingCommerceStockService);
		atpViewController.widgetComponent = widgetComponent;

		when(atpFormDto.getProduct()).thenReturn(camera);
		when(atpFormDto.getBaseStore()).thenReturn(electronicsStore);
		when(atpFormDto.getPointOfService()).thenReturn(electronicsPoS);
		when(camera.getCode()).thenReturn(PRODUCT_CODE);
		when(electronicsStore.getPointsOfService()).thenReturn(Collections.singletonList(electronicsPoS));
		when(atpFormulaService.getAllAtpFormula()).thenReturn(Collections.singletonList(defaultFormula));
	}

	@Test
	public void testInitializeViewWhenPoSSelected()
	{
		//When
		executeInputSocketEvent(AtpViewController.IN_SOCKET, atpFormDto);

		//Then
		verify(warehousingCommerceStockService).getStockLevelForProductAndPointOfService(camera, electronicsPoS);
		verify(warehousingCommerceStockService, never()).getStockLevelForProductAndBaseStore(camera, electronicsStore);
		verify(atpListView).setModel(any(ListModelArray.class));
	}

	@Test
	public void testInitializeViewWhenNoPoSSelected()
	{
		//When
		when(atpFormDto.getPointOfService()).thenReturn(null);
		executeInputSocketEvent(AtpViewController.IN_SOCKET, atpFormDto);

		//Then
		verify(warehousingCommerceStockService, never()).getStockLevelForProductAndPointOfService(camera, electronicsPoS);
		verify(warehousingCommerceStockService).getStockLevelForProductAndBaseStore(camera, electronicsStore);
		verify(atpListView).setModel(any(ListModelArray.class));
	}

	@Test
	public void testInitializeViewWhenNoPoSSelectedMultiAtpFormulaAvl()
	{
		//When
		when(atpFormDto.getPointOfService()).thenReturn(null);
		when(atpFormulaService.getAllAtpFormula()).thenReturn(Arrays.asList(defaultFormula, addtlFormula));
		executeInputSocketEvent(AtpViewController.IN_SOCKET, atpFormDto);

		//Then
		verify(warehousingCommerceStockService, never()).getStockLevelForProductAndPointOfService(camera, electronicsPoS);
		verify(warehousingCommerceStockService, times(2)).getStockLevelForProductAndBaseStore(camera, electronicsStore);
		verify(atpListView).setModel(any(ListModelArray.class));
	}

	@Override
	protected AtpViewController getWidgetController()
	{
		return atpViewController;
	}

}
