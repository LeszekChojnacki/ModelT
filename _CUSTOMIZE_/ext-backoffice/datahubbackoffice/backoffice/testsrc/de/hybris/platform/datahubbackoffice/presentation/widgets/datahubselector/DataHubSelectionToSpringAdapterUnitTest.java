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

import org.mockito.InjectMocks;
import org.mockito.Mock;

import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServer;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.cockpitng.testing.AbstractWidgetUnitTest;
import com.hybris.cockpitng.testing.annotation.DeclaredInput;
import com.hybris.cockpitng.testing.annotation.NullSafeWidget;

@DeclaredInput(socketType = DataHubServer.class, value = "datahubSelected")
@NullSafeWidget
public class DataHubSelectionToSpringAdapterUnitTest extends AbstractWidgetUnitTest<DataHubSelectionToSpringAdapter>
{
	@InjectMocks
	private final DataHubSelectionToSpringAdapter controller = new DataHubSelectionToSpringAdapter();

	@Mock
	private NotificationService notificationService;

	@Override
	protected DataHubSelectionToSpringAdapter getWidgetController()
	{
		return controller;
	}
}