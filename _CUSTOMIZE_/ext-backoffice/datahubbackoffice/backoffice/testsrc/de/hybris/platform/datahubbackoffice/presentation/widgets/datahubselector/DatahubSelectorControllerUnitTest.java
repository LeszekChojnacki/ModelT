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
package de.hybris.platform.datahubbackoffice.presentation.widgets.datahubselector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServer;
import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServerContextService;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.cockpitng.testing.AbstractWidgetUnitTest;
import com.hybris.cockpitng.testing.annotation.DeclaredViewEvent;
import com.hybris.datahub.client.ClientConfiguration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.ListModelList;

@DeclaredViewEvent(componentID = "datahubSelectorList", eventName = Events.ON_CHANGE)
public class DatahubSelectorControllerUnitTest extends AbstractWidgetUnitTest<DatahubSelectorController>
{
	private static final Div parent = Mockito.mock(Div.class);

	@InjectMocks
	private DatahubSelectorController controller = new DatahubSelectorController();

	@Mock
	private DataHubServerContextService dataHubServerContext;
	@Mock
	private ClientConfiguration clientConfiguration;
	@Mock
	private Combobox datahubSelectorList;
	@Mock
	private NotificationService notificationService;
	@Mock
	private ListModelList<DataHubServer> datahubSelectorModel;

	@Test
	public void testSelectorPopulatedUponInitialization()
	{
		simulateDataHubInstances("Local", "Remote");
		controller.initialize(parent);
		verifySelectorContent("Local", "Remote");
	}

	@Test
	public void testExplicitDataHubInstanceSelection()
	{
		simulateDataHubInstances("Local", "Remote");
		final DataHubServer selected = setCurrentSelection("Remote");

		executeViewEvent("datahubSelectorList", Events.ON_CHANGE, new InputEvent("On-Change", null, "Remote", ""));

		assertSocketOutput("datahubSelected", selected);
		reset(datahubSelectorModel);
	}

	private DataHubServer setCurrentSelection(final String name)
	{
		final DataHubServer server = findServerInstance(name);
		when(datahubSelectorModel.getSelection()).thenReturn(Collections.singleton(server));
		return server;
	}

	private DataHubServer findServerInstance(final String name)
	{
		return dataHubServerContext.getAllServers()
								   .stream()
								   .filter(server -> name.equals(server.getName()))
								   .findFirst()
								   .orElse(null);
	}

	private void verifySelectorContent(final String... names)
	{
		final String[] currentNames = controller.getDatahubSelectorModel()
												.getInnerList()
												.stream()
												.map(DataHubServer::getName)
												.toArray(String[]::new);

		assertThat(names).isEqualTo(currentNames);
	}

	private void simulateDataHubInstances(final String... hubs)
	{
		final List<DataHubServer> servers = Arrays.stream(hubs).map(this::dataHubServer).collect(Collectors.toList());
		doReturn(servers).when(dataHubServerContext).getAllServers();
	}

	private DataHubServer dataHubServer(final String name)
	{
		final DataHubServer server = mock(DataHubServer.class);
		doReturn(name).when(server).getName();
		doReturn(true).when(server).isAccessibleWithTimeout();
		return server;
	}

	@Override
	protected DatahubSelectorController getWidgetController()
	{
		return controller;
	}
}