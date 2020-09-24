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
 */
package de.hybris.platform.datahubbackoffice.presentation.widgets.dashboard;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import de.hybris.platform.datahubbackoffice.datahub.dashboard.ComposeDashboardRowRenderer;
import de.hybris.platform.datahubbackoffice.datahub.dashboard.LoadDashboardRowRenderer;
import de.hybris.platform.datahubbackoffice.datahub.dashboard.PublicationDashboardRowRenderer;

import com.hybris.cockpitng.testing.AbstractWidgetUnitTest;
import com.hybris.cockpitng.testing.annotation.DeclaredInput;
import com.hybris.cockpitng.testing.annotation.DeclaredViewEvent;
import com.hybris.cockpitng.testing.annotation.DeclaredViewEvents;
import com.hybris.cockpitng.testing.annotation.ExtensibleWidget;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import org.zkoss.spring.SpringUtil;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Timer;

@DeclaredInput(value = DashboardLayoutContainer.SOCKET_IN_ACTIVE, socketType = Boolean.class)
@DeclaredViewEvents(@DeclaredViewEvent(componentID = DashboardLayoutContainer.TABBOX_WIDGET_ID, eventName = Events.ON_SELECT))
@ExtensibleWidget(level = ExtensibleWidget.METHODS)
public class DashboardLayoutContainerUnitTest extends AbstractWidgetUnitTest<DashboardLayoutContainer>
{
	private final DashboardLayoutContainer container = new DashboardLayoutContainer();
	@Mock
	private Tabbox tabbox;
	@Mock
	private Div statusCnt;
	@Mock
	private Label statusLabel;
	@Mock
	private Timer timer;
	@Mock
	private LoadDashboardRowRenderer loadCountsRenderer;
	@Mock
	private ComposeDashboardRowRenderer composeCountsRenderer;
	@Mock
	private PublicationDashboardRowRenderer publishCountsRenderer;

	@Override
	protected DashboardLayoutContainer getWidgetController()
	{
		return container;
	}

	@Before
	public void setUp()
	{
		super.setUpAbstractWidgetUnitTest();

		doReturn(new ArrayList<>()).when(timer).getEventListeners(Events.ON_TIMER);
		configureRenderers();
	}

	private void configureRenderers()
	{
		registerApplicationContextBean("loadCountsRenderer", loadCountsRenderer);
		widgetSettings.put("renderers_load", "loadCountsRenderer");
		registerApplicationContextBean("composeCountsRenderer", composeCountsRenderer);
		widgetSettings.put("renderers_compose", "composeCountsRenderer");
		registerApplicationContextBean("publishCountsRenderer", publishCountsRenderer);
		widgetSettings.put("renderers_publish", "publishCountsRenderer");
	}

	private void registerApplicationContextBean(final String beanName, final Object bean)
	{
		final ApplicationContext appContext = SpringUtil.getApplicationContext();
		doReturn(true).when(appContext).containsBean(beanName);
		doReturn(bean).when(appContext).getBean(beanName);
	}

	@Test
	public void testActivate_falseReceived()
	{
		givenTabSelected("load");

		container.activate(Boolean.FALSE);

		verify(timer).setRunning(false);
		verifyZeroInteractions(loadCountsRenderer);
	}

	@Test
	public void testActivate_trueReceived()
	{
		givenTabSelected("load");

		container.activate(Boolean.TRUE);

		verify(timer).setRunning(true);
		verify(loadCountsRenderer).renderDashboardRow(tabbox.getSelectedPanel(), widgetInstanceManager);
	}

	@Test
	public void testActivate_nullReceived()
	{
		givenTabSelected("load");

		container.activate(null);

		verify(timer).setRunning(false);
		verifyZeroInteractions(loadCountsRenderer);
	}

	@Test
	public void testActivate_noTabSelected()
	{
		givenNoTabSelected();

		container.activate(Boolean.TRUE);

		verify(timer).setRunning(false);
		verifyZeroInteractions(loadCountsRenderer, composeCountsRenderer, publishCountsRenderer);
	}

	@Test
	public void testDashboardTabSelected_loadTabSelected()
	{
		givenTabSelected("load");
		container.dashboardTabSelected();
		verify(loadCountsRenderer).renderDashboardRow(tabbox.getSelectedPanel(), widgetInstanceManager);
	}

	@Test
	public void testDashboardTabSelected_composeTabSelected()
	{
		givenTabSelected("compose");
		container.dashboardTabSelected();
		verify(composeCountsRenderer).renderDashboardRow(tabbox.getSelectedPanel(), widgetInstanceManager);
	}

	@Test
	public void testDashboardTabSelected_publishTabSelected()
	{
		givenTabSelected("publish");
		container.dashboardTabSelected();
		verify(publishCountsRenderer).renderDashboardRow(tabbox.getSelectedPanel(), widgetInstanceManager);
	}

	private void givenNoTabSelected()
	{
		doReturn(null).when(tabbox).getSelectedPanel();
		doReturn(null).when(tabbox).getSelectedTab();
	}

	private void givenTabSelected(final String tabId)
	{
		final Tabpanel panel = mock(Tabpanel.class, tabId + " panel");
		doReturn(panel).when(tabbox).getSelectedPanel();
		doReturn(tab(tabId)).when(tabbox).getSelectedTab();
	}

	private Tab tab(final String id)
	{
		final Tab tab = mock(Tab.class, id + " tab");
		doReturn(id).when(tab).getId();
		return tab;
	}
}