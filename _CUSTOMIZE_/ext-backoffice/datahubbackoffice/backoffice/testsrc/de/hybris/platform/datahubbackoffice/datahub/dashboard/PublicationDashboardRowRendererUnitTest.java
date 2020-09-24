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

public class PublicationDashboardRowRendererUnitTest extends AbstractDashboardRowRendererUnitTest
{
	@InjectMocks
	private final PublicationDashboardRowRenderer publicationRenderer = new PublicationDashboardRowRenderer();

	@Before
	@Override
	public void setUp()
	{
		super.setUp();
		when(statusCountClient.getCanonicalPublicationStatusCount()).thenReturn(canonicalPublicationStatusCountData);
		when(statusCountClient.getCanonicalPublicationStatusCount(anyString())).thenReturn(canonicalPublicationStatusCountData);
	}

	@Test
	public void testStatusCountClientIsCalledToGetCanonicalAndCanonicalPublicationPublicationItems()
	{
		publicationRenderer.renderDashboardRow(parent, context);

		verify(statusCountClient).getCanonicalPublicationStatusCount();
	}

	@Test
	public void testClientGetsCountForPoolWhenPoolIsSetOnContext()
	{
		whenPoolWithNameSelected("MyPool");
		publicationRenderer.renderDashboardRow(parent, context);

		verify(statusCountClient).getCanonicalPublicationStatusCount("MyPool");

		whenNoPoolSelected();
		publicationRenderer.renderDashboardRow(parent, context);

		verify(statusCountClient).getCanonicalPublicationStatusCount();
	}

	@Test
	public void testGettingCorrectStatusCountForPublicationRenderer()
	{
		publicationRenderer.renderDashboardRow(parent, context);

		verify(canonicalPublicationStatusCountData).getSuccessCount();
		verify(canonicalPublicationStatusCountData).getIgnoredCount();
		verify(canonicalPublicationStatusCountData).getExternalErrorCount();
		verify(canonicalPublicationStatusCountData).getInternalErrorCount();
	}
}