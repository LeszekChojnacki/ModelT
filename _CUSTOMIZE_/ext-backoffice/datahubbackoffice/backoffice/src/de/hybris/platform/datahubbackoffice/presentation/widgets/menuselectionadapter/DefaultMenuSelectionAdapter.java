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
package de.hybris.platform.datahubbackoffice.presentation.widgets.menuselectionadapter;
import com.hybris.backoffice.navigation.NavigationNode;
import com.hybris.backoffice.navigation.NavigationNodeDecorator;
import com.hybris.backoffice.navigation.impl.SimpleNode;
import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.core.user.CockpitUserService;
import com.hybris.cockpitng.util.DefaultWidgetController;

import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServer;

import org.springframework.util.ObjectUtils;
import org.zkoss.zk.ui.select.annotation.WireVariable;

public class DefaultMenuSelectionAdapter extends DefaultWidgetController
{
	private static final String SELECT_DATAHUB_INSTANCE_MESSAGE = "please.select.datahub.instance";
	private static final String DEFAULT_ADMIN_MENU = "Datahub_Dashboard";
	private static final String SOCKET_IN = "input";
	private static final String SOCKET_OUT = "output";
	private static final String SOCKET_DATAHUB_IN = "datahubSelected";

	@WireVariable
	private transient CockpitUserService cockpitUserService;
	@WireVariable
	private transient NotificationService notificationService;

	private NavigationNode menuSelected;
	private DataHubServer datahubSelected;

	@SocketEvent(socketId = SOCKET_IN)
	public void nodeSelected(final NavigationNode in)
	{
		if (menuSelectionChanged(in))
		{
			menuSelected = in;

			if (datahubSelected == null)
			{
				notificationService.notifyUser(notificationService.getWidgetNotificationSource(getWidgetInstanceManager()),
						SELECT_DATAHUB_INSTANCE_MESSAGE, NotificationEvent.Level.FAILURE);
			}
			else
			{
				sendOutput(SOCKET_OUT, menuSelected);
			}
		}
	}

	private boolean menuSelectionChanged(final NavigationNode currentSelection)
	{
		return ! ObjectUtils.nullSafeEquals(menuSelected, currentSelection);
	}

	@SocketEvent(socketId = SOCKET_DATAHUB_IN)
	public void datahubSelected(final DataHubServer selected)
	{
		datahubSelected = selected;
		if (datahubSelected != null)
		{
			refreshMenuSelection();
		}
	}

	private void refreshMenuSelection()
	{
		final NavigationNode itemToReselect = menuSelected != null ? menuSelected : defaultMenuSelection();
		if (itemToReselect != null)
		{
			sendOutput(SOCKET_OUT, itemToReselect);
		}
	}

	private NavigationNode defaultMenuSelection()
	{
		final String currentUser = cockpitUserService.getCurrentUser();
		if (cockpitUserService.isAdmin(currentUser))
		{
			final NavigationNode defaultNode = new SimpleNode(DEFAULT_ADMIN_MENU);
			return new NavigationNodeDecorator(defaultNode);
		}
		return null;
	}
}
