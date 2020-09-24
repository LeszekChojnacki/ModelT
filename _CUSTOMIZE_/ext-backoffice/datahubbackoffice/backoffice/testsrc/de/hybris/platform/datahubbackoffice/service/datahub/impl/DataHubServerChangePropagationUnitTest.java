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
package de.hybris.platform.datahubbackoffice.service.datahub.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServer;
import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServerAware;
import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServerInfo;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class DataHubServerChangePropagationUnitTest
{
	private DataHubServerChangePropagator propagator;

	@Before
	public void setUp()
	{
		propagator = new DataHubServerChangePropagator();
	}

	@Test
	public void noServicesToPropagateTheChangeTo()
	{
		final DataHubServer server = new DataHubServer(new DataHubServerInfo("local", "localhost", "", ""));

		propagator.setDataHubServer(server);
	}

	@Test
	public void thereAreServicesToPropagateTheChangeTo()
	{
		final DataHubServer server = new DataHubServer(new DataHubServerInfo("local", "localhost", "", ""));

		final DataHubServerAware service1 = mock(DataHubServerAware.class);
		final DataHubServerAware service2 = mock(DataHubServerAware.class);

		propagator.setServices(Arrays.asList(service1, service2));
		propagator.setDataHubServer(server);

		verify(service1).setDataHubServer(server);
		verify(service2).setDataHubServer(server);
	}
}
