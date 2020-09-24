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
import com.hybris.backoffice.widgets.advancedsearch.AbstractInitAdvancedSearchAdapter;
import com.hybris.backoffice.widgets.advancedsearch.AdvancedSearchOperatorService;
import com.hybris.backoffice.widgets.advancedsearch.impl.AdvancedSearchInitContext;
import com.hybris.cockpitng.core.config.CockpitConfigurationException;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.advancedsearch.AdvancedSearch;
import com.hybris.cockpitng.core.config.impl.jaxb.hybris.advancedsearch.FieldListType;
import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacade;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException;
import com.hybris.cockpitng.testing.AbstractWidgetUnitTest;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.returns.model.ReturnRequestModel;
import org.junit.Before;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Used to setup common attributes for Return-related filters and setup the testing environment for backoffice components
 */
public abstract class AbstractReturnWidgetTest<T> extends AbstractWidgetUnitTest<T>
{
	@Mock
	protected DataType dataType;

	@Mock
	protected AdvancedSearch advancedSearch;

	@Mock
	protected NavigationNode navigationNode;

	@Mock
	protected TypeFacade typeFacade;

	@Mock
	protected PermissionFacade permissionFacade;

	@Mock
	protected AdvancedSearchOperatorService advancedSearchOperatorService;

	@Mock
	protected EnumerationService enumerationService;

	/**
	 * Mock components needed by the underlying widget.
	 *
	 * @throws com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException
	 * @throws com.hybris.cockpitng.core.config.CockpitConfigurationException
	 */
	@Before
	public void setUpAbstractReturnWidgetUnitTest() throws TypeNotFoundException, CockpitConfigurationException
	{
		when(dataType.getCode()).thenReturn(ReturnRequestModel._TYPECODE);
		when(typeFacade.load(ReturnRequestModel._TYPECODE)).thenReturn(dataType);
		when(widgetInstanceManager.loadConfiguration(any(), any())).thenReturn(advancedSearch);
		when(advancedSearch.getFieldList()).thenReturn(new FieldListType());
	}

	/**
	 * Sets up the controller and assigns necessary permissions.
	 */
	public void setupController()
	{
		final AbstractInitAdvancedSearchAdapter controller = (AbstractInitAdvancedSearchAdapter) getWidgetController();
		controller.setWidgetInstanceManager(widgetInstanceManager);
		controller.setTypeFacade(typeFacade);
		controller.setPermissionFacade(permissionFacade);
		controller.setAdvancedSearchOperatorService(advancedSearchOperatorService);
	}

	/**
	 * Tests whether the widget will not answer with a socket event if the input is null.
	 *
	 * @param socketIn  the input socket
	 * @param socketOut the output socket
	 */
	public void testInputNullNavigationNode(final String socketIn, final String socketOut)
	{
		executeInputSocketEvent(socketIn, (Object) null);
		assertNoSocketOutputInteractions(socketOut);
	}

	/**
	 * Tests whether the advanced search data context will be returned if a navigation node is selected.
	 *
	 * @param socketIn  the input socket
	 * @param socketOut the output socket
	 * @throws com.hybris.cockpitng.dataaccess.facades.type.exceptions.TypeNotFoundException
	 */
	public void testInputNullSearchData(final String socketIn, final String socketOut) throws TypeNotFoundException
	{
		when(typeFacade.load(ReturnRequestModel._TYPECODE)).thenReturn(null);
		executeInputSocketEvent(socketIn, navigationNode);
		verify(widgetInstanceManager).sendOutput(eq(socketOut), any(AdvancedSearchInitContext.class));
	}

}
