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

public class LoadDashboardRowRendererUnitTest extends AbstractDashboardRowRendererUnitTest
{
	@InjectMocks
	private final LoadDashboardRowRenderer loadRenderer = new LoadDashboardRowRenderer();

	@Before
	@Override
	public void setUp()
	{
		super.setUp();
		when(statusCountClient.getRawItemStatusCount()).thenReturn(rawStatusCountData);
		when(statusCountClient.getRawItemStatusCount(anyString())).thenReturn(rawStatusCountData);
	}

	@Test
	public void testStatusCountClientIsCalledToGetRawItems()
	{
		loadRenderer.renderDashboardRow(parent, context);

		verify(statusCountClient).getRawItemStatusCount();
	}

	@Test
	public void testClientGetsCountForPoolWhenPoolIsSetOnContext()
	{
		whenPoolWithNameSelected("MyPool");
		loadRenderer.renderDashboardRow(parent, context);

		verify(statusCountClient).getRawItemStatusCount("MyPool");

		whenNoPoolSelected();
		loadRenderer.renderDashboardRow(parent, context);

		verify(statusCountClient).getRawItemStatusCount();
	}

	@Test
	public void testGettingCorrectStatusCountForLoadRenderer()
	{
		loadRenderer.renderDashboardRow(parent, context);

		verify(rawStatusCountData).getIgnoredCount();
		verify(rawStatusCountData).getPendingCount();
		verify(rawStatusCountData).getProcessedCount();
	}
}