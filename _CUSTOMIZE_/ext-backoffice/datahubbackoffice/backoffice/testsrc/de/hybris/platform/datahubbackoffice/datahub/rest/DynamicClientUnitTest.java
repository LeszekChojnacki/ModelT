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
package de.hybris.platform.datahubbackoffice.datahub.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServer;
import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServerContextService;

import com.hybris.datahub.client.ClientConfiguration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DynamicClientUnitTest
{
	private static final ClientConfiguration config = new ClientConfiguration();
	private static final String BASE_URL = "/datahub/base/url/for/upload";

	@InjectMocks
	private final DynamicRestClient client = createClient(config);
	@Mock
	private DataHubServerContextService service;

	@Before
	public void setUp()
	{
		final DataHubServer contextServer = createServerFor(BASE_URL);
		doReturn(contextServer).when(service).getContextDataHubServer();
	}

	private DataHubServer createServerFor(final String url)
	{
		final DataHubServer server = mock(DataHubServer.class);
		doReturn(url).when(server).getLocation();
		return server;
	}

	@Test
	public void testDefaultContextServerForBaseApiUrl()
	{
		final String url = client.getBaseApiUrl();

		assertThat(url).isEqualTo(BASE_URL);
	}

	@Test
	public void testSpecifyCustomServerForBaseApiUrl()
	{
		final String customUrl = "/customer/server/path";
		client.useServer(createServerFor(customUrl));

		final String url = client.getBaseApiUrl();

		assertThat(url).isEqualTo(customUrl);
	}

	@Test
	public void testResetPreviouslySpecifiedCustomerServerForBaseApiUrl()
	{
		final String customUrl = "/customer/server/path";
		client.useServer(createServerFor(customUrl));
		client.useContextServer();

		final String url = client.getBaseApiUrl();

		assertThat(url).isEqualTo(BASE_URL);
	}

	protected DynamicRestClient createClient(final ClientConfiguration cfg)
	{
		return new DynamicCsvImportClient(cfg);
	}
}
