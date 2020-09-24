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
package de.hybris.platform.datahubbackoffice.datahub.dashboard;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;

public class ComposeDashboardRowRendererUnitTest extends AbstractDashboardRowRendererUnitTest
{
	@InjectMocks
	private final ComposeDashboardRowRenderer composeRenderer = new ComposeDashboardRowRenderer();

	@Before
	@Override
	public void setUp()
	{
		super.setUp();
		when(statusCountClient.getCanonicalItemStatusCount()).thenReturn(canonicalStatusCountData);
		when(statusCountClient.getCanonicalItemStatusCount(anyString())).thenReturn(canonicalStatusCountData);
	}

	@Test
	public void testStatusCountClientIsCalledToGetCanonicalItems()
	{
		composeRenderer.renderDashboardRow(parent, context);

		verify(statusCountClient).getCanonicalItemStatusCount();
	}

	@Test
	public void testClientGetsCountForPoolWhenPoolIsSetOnContext()
	{
		whenPoolWithNameSelected("MyPool");
		composeRenderer.renderDashboardRow(parent, context);

		verify(statusCountClient).getCanonicalItemStatusCount("MyPool");

		whenNoPoolSelected();
		composeRenderer.renderDashboardRow(parent, context);

		verify(statusCountClient).getCanonicalItemStatusCount();
	}

	@Test
	public void testGettingCorrectStatusCountForComposeRenderer()
	{
		composeRenderer.renderDashboardRow(parent, context);

		verify(canonicalStatusCountData).getArchivedCount();
		verify(canonicalStatusCountData).getSuccessCount();
		verify(canonicalStatusCountData).getDeletedCount();
		verify(canonicalStatusCountData).getErrorCount();
	}
}