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
package de.hybris.platform.datahubbackoffice.service;

import de.hybris.platform.datahubbackoffice.daos.DataHubInstanceDAO;
import de.hybris.platform.datahubbackoffice.model.DataHubInstanceModelModel;
import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServerInfo;
import de.hybris.platform.datahubbackoffice.service.datahub.UserContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TypeSystemBasedDataHubNameServiceUnitTest
{
	private static final String TEST_NAME = "TEST_NAME";
	private static final String TEST_LOCATION = "TEST_LOCATION";
	private static final String TEST_NAME2 = "TEST_NAME2";
	private static final String TEST_LOCATION2 = "TEST_LOCATION2";
	private static final String IRRELEVANT_VALUE = "";

	@InjectMocks
	private TypeSystemBasedDataHubNameService service = new TypeSystemBasedDataHubNameService();
	@Mock
	private DataHubInstanceDAO dataHubInstanceDAO;
	@Mock
	private UserContext userContext;

	@Before
	public void setUp()
	{
		final DataHubInstanceModelModel dataHubInstance = new DataHubInstanceModelModel();
		dataHubInstance.setInstanceName(TEST_NAME);
		dataHubInstance.setInstanceLocation(TEST_LOCATION);
		final DataHubInstanceModelModel dataHubInstance2 = new DataHubInstanceModelModel();
		dataHubInstance2.setInstanceName(TEST_NAME2);
		dataHubInstance2.setInstanceLocation(TEST_LOCATION2);
		when(userContext.isUserDataHubAdmin()).thenReturn(true);
		when(dataHubInstanceDAO.findDataHubInstances()).thenReturn(Arrays.asList(dataHubInstance, dataHubInstance2));
	}

	@Test
	public void testGetAllServersWithNoServersReturned()
	{
		when(dataHubInstanceDAO.findDataHubInstances()).thenReturn(new ArrayList<>());
		final Collection<DataHubServerInfo> servers = service.getAllServers();

		assertThat(servers).isEmpty();
	}

	@Test
	public void testGetAllServersWithTwoServers()
	{
		final Collection<DataHubServerInfo> servers = service.getAllServers();

		final DataHubServerInfo expectedServer1 = new DataHubServerInfo(TEST_NAME, TEST_LOCATION, IRRELEVANT_VALUE, IRRELEVANT_VALUE);
		final DataHubServerInfo expectedServer2 = new DataHubServerInfo(TEST_NAME2, TEST_LOCATION2, IRRELEVANT_VALUE, IRRELEVANT_VALUE);

		assertThat(servers).hasSize(2)
						   .contains(expectedServer1, expectedServer2);
	}

	@Test
	public void testGetServer()
	{
		final DataHubServerInfo server = service.getServer(TEST_NAME);

		assertThat(server.getLocation()).isEqualTo(TEST_LOCATION);
	}
}
