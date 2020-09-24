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

import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServer;
import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServerAware;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.spring.SpringUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.WireVariable;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.util.DefaultWidgetController;

public class DataHubSelectionToSpringAdapter extends DefaultWidgetController
{
	private static final String SPRING_BEAN_TO_UPDATE_KEY = "springBeanToUpdate";
	private static final String ERROR_NOT_CONFIGURED = "datahub.selection.adapter.error.not.configured";
	private static final String ERROR_BEAN_NOT_FOUND = "datahub.selection.adapter.error.bean.not.found";
	private static final String ERROR_BEAN_INVALID = "datahub.selection.adapter.error.invalid.bean";

	private transient Object springBean;

	@WireVariable
	private transient NotificationService notificationService;

	@Override
	public void initialize(final Component comp)
	{
		final String springBeanToUpdate = getWidgetSettings().getString(SPRING_BEAN_TO_UPDATE_KEY);

		if (StringUtils.isEmpty(springBeanToUpdate))
		{
			notificationService.notifyUser(getWidgetId(), ERROR_NOT_CONFIGURED, NotificationEvent.Level.WARNING, SPRING_BEAN_TO_UPDATE_KEY);
		}
		else
		{
			springBean = SpringUtil.getBean(springBeanToUpdate);
		}
	}

	@SocketEvent(socketId = "datahubSelected")
	public void updateTargetSpringBean(final DataHubServer dataHub)
	{
		if (springBean == null)
		{
			notificationService.notifyUser(getWidgetId(), ERROR_BEAN_NOT_FOUND, NotificationEvent.Level.FAILURE, SPRING_BEAN_TO_UPDATE_KEY);
		}
		else if (!(springBean instanceof DataHubServerAware))
		{
			notificationService.notifyUser(getWidgetId(), ERROR_BEAN_INVALID, NotificationEvent.Level.FAILURE, SPRING_BEAN_TO_UPDATE_KEY);
		}
		else
		{
			((DataHubServerAware) springBean).setDataHubServer(dataHub);
		}
	}

	public NotificationService getNotificationService()
	{
		return notificationService;
	}

	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}

	private String getWidgetId()
	{
		return notificationService.getWidgetNotificationSource(getWidgetInstanceManager());
	}
}
