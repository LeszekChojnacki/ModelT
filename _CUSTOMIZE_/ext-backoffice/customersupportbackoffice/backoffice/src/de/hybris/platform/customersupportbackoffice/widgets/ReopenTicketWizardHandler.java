/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.customersupportbackoffice.widgets;

import de.hybris.platform.customersupportbackoffice.constants.CustomersupportbackofficeConstants;
import de.hybris.platform.customersupportbackoffice.data.CsReopenTicketForm;
import de.hybris.platform.ticket.enums.CsEventReason;
import de.hybris.platform.ticket.enums.CsInterventionType;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticket.service.TicketBusinessService;
import de.hybris.platform.ticket.service.TicketException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zul.Combobox;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEventTypes;
import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.config.jaxb.wizard.CustomType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandler;
import com.hybris.cockpitng.widgets.configurableflow.FlowActionHandlerAdapter;


/**
 * Handler for reopen ticket wizard.
 */
public class ReopenTicketWizardHandler implements FlowActionHandler
{
	private TicketBusinessService ticketBusinessService;
	private NotificationService notificationService;
	private static final Logger LOG = LoggerFactory.getLogger(ReopenTicketWizardHandler.class);

	@Override
	public void perform(final CustomType customType, final FlowActionHandlerAdapter adapter, final Map<String, String> parameters)
	{
		final CsTicketModel ticket = (CsTicketModel) adapter.getWidgetInstanceManager().getModel()
				.getValue("currentContext", HashMap.class).get("parentObject");
		final CsReopenTicketForm reopenTicketForm = adapter.getWidgetInstanceManager().getModel()
				.getValue("customersupport_backoffice_reopenTicketForm", CsReopenTicketForm.class);
		final WidgetInstanceManager wim = (WidgetInstanceManager) adapter.getWidgetInstanceManager().getModel()
				.getValue("currentContext", HashMap.class).get(Editor.WIDGET_INSTANCE_MANAGER);

		final Combobox contactTypeCombo = adapter.getWidgetInstanceManager().getModel()
				.getValue(CustomersupportbackofficeConstants.REPLY_TYPE, Combobox.class);
		final CsInterventionType contactType = contactTypeCombo.getSelectedItem().getValue();

		try
		{
			getTicketBusinessService().unResolveTicket(ticket, contactType, CsEventReason.UPDATE, reopenTicketForm.getMessage());

			getNotificationService().notifyUser(getNotificationService().getWidgetNotificationSource(wim),
					NotificationEventTypes.EVENT_TYPE_OBJECT_UPDATE, NotificationEvent.Level.SUCCESS,
					Collections.singletonList(ticket));

			adapter.done();
			wim.getWidgetslot().updateView();
		}
		catch (final TicketException e)
		{
			LOG.warn("Exception on reopening ticket " + ticket.getTicketID(), e);
		}
	}

	protected TicketBusinessService getTicketBusinessService()
	{
		return ticketBusinessService;
	}

	@Required
	public void setTicketBusinessService(final TicketBusinessService ticketBusinessService)
	{
		this.ticketBusinessService = ticketBusinessService;
	}

	protected NotificationService getNotificationService()
	{
		return notificationService;
	}

	@Required
	public void setNotificationService(final NotificationService notificationService)
	{
		this.notificationService = notificationService;
	}
}
