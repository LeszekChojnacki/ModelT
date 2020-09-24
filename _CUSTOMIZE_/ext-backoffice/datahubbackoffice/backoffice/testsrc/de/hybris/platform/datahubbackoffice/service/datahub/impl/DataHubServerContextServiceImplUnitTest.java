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

import static org.assertj.core.api.Assertions.assertThat;

import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServer;
import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServerInfo;

import org.junit.Before;
import org.junit.Test;

public class DataHubServerContextServiceImplUnitTest
{
	private static final String STAGING = "staging";
	private static final String BASE_URL = "locahost:9090";
	private static final DataHubServerInfo CONFIG = new DataHubServerInfo(STAGING, BASE_URL, "", "");

	private DataHubServerContextServiceImpl service;

	@Before
	public void setUp()
	{
		service = new DataHubServerContextServiceImpl();
	}

	@Test
	public void testGetContextServer()
	{
		final DataHubServer server = new DataHubServer(CONFIG);
		service.setDataHubServer(server);
		assertThat(service.getContextDataHubServer()).isEqualTo(server);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetDataHubConfigurationWhenNull()
	{
		service.setDataHubServer(null);
	}
}