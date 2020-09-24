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
package de.hybris.platform.datahubbackoffice.service.datahub;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class DataHubServerUnitTest
{
	@Test
	public void testInstantiation()
	{
		final DataHubServer server = new DataHubServer(new DataHubServerInfo("name", "location", "", ""));

		assertThat(server.getName()).isEqualTo("name");
		assertThat(server.getLocation()).isEqualTo("location");
	}
}