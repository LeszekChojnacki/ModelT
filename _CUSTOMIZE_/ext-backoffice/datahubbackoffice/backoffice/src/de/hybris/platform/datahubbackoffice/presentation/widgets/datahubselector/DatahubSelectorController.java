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

import de.hybris.platform.datahubbackoffice.exception.NoDataHubInstanceAvailableException;
import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServer;
import de.hybris.platform.datahubbackoffice.service.datahub.DataHubServerContextService;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.ListModelList;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.annotations.ViewEvent;
import com.hybris.cockpitng.util.DefaultWidgetController;

public class DatahubSelectorController extends DefaultWidgetController
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DatahubSelectorController.class);

	private static final String NO_DATAHUB_INSTANCES_AVAILABLE = "datahub.selector.error.no.instances";
	private static final String DATAHUB_INSTANCE_ENABLED = "instanceenabled.png";
	private static final String DATAHUB_INSTANCE_DISABLED = "instancedisabled.png";
	private static final String COMPONENT_DATAHUB_SELECTOR_LIST = "datahubSelectorList";
	private static final String SOCKET_OUT_ID = "datahubSelected";
	private static final String ON_DEFER_CREATION = "onDeferCreation";

	@WireVariable
	private transient DataHubServerContextService dataHubServerContext;
	@WireVariable
	private transient NotificationService notificationService;

	private Combobox datahubSelectorList;
	private ListModelList<DataHubServer> datahubSelectorModel;

	@Override
	public void initialize(final Component comp)
	{
		comp.addEventListener(Events.ON_CREATE, event -> Events.echoEvent(ON_DEFER_CREATION, comp, null));
		comp.addEventListener(ON_DEFER_CREATION, event -> selectDefaultDataHubInstance());
		datahubSelectorList.setItemRenderer(createComboItemRenderer());
		populateSelectorComponent();
	}

	protected ComboitemRenderer createComboItemRenderer()
	{
		return (comboitem, data, i) -> {
			if (data instanceof DataHubServer)
			{
				final DataHubServer server = (DataHubServer) data;
				final boolean enabled = server.isAccessibleWithTimeout();
				final String imgPath = enabled ? DATAHUB_INSTANCE_ENABLED : DATAHUB_INSTANCE_DISABLED;

				comboitem.setDisabled(!enabled);
				comboitem.setImage(String.format("%s/images/%s", getWidgetRoot(), imgPath));
				comboitem.setLabel(server.getName());
				comboitem.setValue(server);
			}
		};
	}

	protected void populateSelectorComponent()
	{
		datahubSelectorModel = new ListModelList<>(dataHubServerContext.getAllServers());
		datahubSelectorList.setModel(datahubSelectorModel);
	}

	protected void selectDefaultDataHubInstance()
	{
		try
		{
			final DataHubServer server = dataHubServerContext.getContextDataHubServer();
			if (server != null && server.isAccessibleWithTimeout())
			{
				datahubSelectorModel.setSelection(Collections.singleton(server));
				sendOutput(SOCKET_OUT_ID, server);
			}
		}
		catch (final NoDataHubInstanceAvailableException e)
		{
			LOGGER.trace(e.getMessage(), e);
			notificationService.notifyUser(notificationService.getWidgetNotificationSource(getWidgetInstanceManager()),
					NO_DATAHUB_INSTANCES_AVAILABLE, NotificationEvent.Level.FAILURE);
		}
	}

	@ViewEvent(eventName = Events.ON_CHANGE, componentID = COMPONENT_DATAHUB_SELECTOR_LIST)
	public void selectDataHubInstance(final InputEvent event)
	{
		DataHubServer server = null;
		final Set<DataHubServer> selectedInstances = datahubSelectorModel.getSelection();
		if (CollectionUtils.isNotEmpty(selectedInstances))
		{
			server = datahubSelectorModel.getSelection().iterator().next();
		}
		if (server != null)
		{
			sendOutput(SOCKET_OUT_ID, server);
		}
	}

	protected List<String> getSelectorContent()
	{
		return datahubSelectorList.getItems()
								  .stream()
								  .map(Comboitem::getLabel)
								  .collect(Collectors.toList());
	}

	protected Combobox getDatahubSelectorList()
	{
		return datahubSelectorList;
	}

	protected ListModelList<DataHubServer> getDatahubSelectorModel()
	{
		return datahubSelectorModel;
	}

	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}

	public NotificationService getNotificationService()
	{
		return this.notificationService;
	}
}
