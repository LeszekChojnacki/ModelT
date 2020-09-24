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

public class DataHubServerInfoUnitTest
{
	private static final String IRRELEVANT_VALUE = "does not matter";

	@Test
	public void testReadsBackName()
	{
		final DataHubServerInfo cfg = new DataHubServerInfo("Local DataHub", null, IRRELEVANT_VALUE, IRRELEVANT_VALUE);
		assertThat(cfg.getName()).isEqualTo("Local DataHub");
	}

	@Test
	public void testReadsBackLocation()
	{
		final DataHubServerInfo cfg = new DataHubServerInfo(null, "somewhere on the network", IRRELEVANT_VALUE, IRRELEVANT_VALUE);
		assertThat(cfg.getLocation()).isEqualTo("somewhere on the network");
	}

	@Test
	public void testEquals()
	{
		assertThat(new DataHubServerInfo("MyHub", "my.net", IRRELEVANT_VALUE, IRRELEVANT_VALUE))
				.isEqualTo(new DataHubServerInfo("MyHub", null, IRRELEVANT_VALUE, IRRELEVANT_VALUE));
		assertThat(new DataHubServerInfo(null, "my.net", IRRELEVANT_VALUE, IRRELEVANT_VALUE))
				.isEqualTo(new DataHubServerInfo(null, "other.net", IRRELEVANT_VALUE, IRRELEVANT_VALUE));
		assertThat(new DataHubServerInfo("MyHub", "my.net", IRRELEVANT_VALUE, IRRELEVANT_VALUE))
				.isNotEqualTo("MyHub");
		assertThat(new DataHubServerInfo("MyHub", "my.net", IRRELEVANT_VALUE, IRRELEVANT_VALUE))
				.isNotEqualTo(new DataHubServerInfo("OtherHub", "my.net", IRRELEVANT_VALUE, IRRELEVANT_VALUE));
		assertThat(new DataHubServerInfo(null, "my.net", IRRELEVANT_VALUE, IRRELEVANT_VALUE))
				.isNotEqualTo(new DataHubServerInfo("MyHub", "my.net", IRRELEVANT_VALUE, IRRELEVANT_VALUE));
		assertThat(new DataHubServerInfo("MyHub", "my.net", IRRELEVANT_VALUE, IRRELEVANT_VALUE))
				.isNotEqualTo(new DataHubServerInfo(null, "my.net", IRRELEVANT_VALUE, IRRELEVANT_VALUE));
	}

	@Test
	public void testHashCode()
	{
		assertThat(new DataHubServerInfo("MyHub", "my.net", IRRELEVANT_VALUE, IRRELEVANT_VALUE).hashCode())
				.isEqualTo(new DataHubServerInfo("MyHub", null, IRRELEVANT_VALUE, IRRELEVANT_VALUE).hashCode());
		assertThat(new DataHubServerInfo(null, "my.net", IRRELEVANT_VALUE, IRRELEVANT_VALUE).hashCode())
				.isEqualTo(new DataHubServerInfo(null, "other.net", IRRELEVANT_VALUE, IRRELEVANT_VALUE).hashCode());
		assertThat(new DataHubServerInfo("MyHub", "my.net", IRRELEVANT_VALUE, IRRELEVANT_VALUE).hashCode())
				.isNotEqualTo(new DataHubServerInfo("OtherHub", "my.net", IRRELEVANT_VALUE, IRRELEVANT_VALUE).hashCode());
		assertThat(new DataHubServerInfo("", "my.net", IRRELEVANT_VALUE, IRRELEVANT_VALUE).hashCode())
				.isNotEqualTo(new DataHubServerInfo(null, "my.net", IRRELEVANT_VALUE, IRRELEVANT_VALUE).hashCode());
	}
}