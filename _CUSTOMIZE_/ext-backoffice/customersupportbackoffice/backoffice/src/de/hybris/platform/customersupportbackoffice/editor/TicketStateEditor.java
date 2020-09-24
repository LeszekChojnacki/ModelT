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
package de.hybris.platform.customersupportbackoffice.editor;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.ticket.enums.CsTicketState;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticket.service.TicketBusinessService;
import de.hybris.platform.ticket.service.TicketException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ListModelList;

import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEventTypes;
import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.editors.CockpitEditorRenderer;
import com.hybris.cockpitng.editors.EditorContext;
import com.hybris.cockpitng.editors.EditorListener;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.widgets.configurableflow.ConfigurableFlowContextParameterNames;


public class TicketStateEditor extends AbstractComponentWidgetAdapterAware implements CockpitEditorRenderer<Object>
{
	private static final Logger LOG = Logger.getLogger(TicketStateEditor.class);

	@Resource
	protected TicketBusinessService ticketBusinessService;

	@Resource
	protected UserService userService;

	@Resource
	protected ModelService modelService;

	@Resource
	protected LabelService labelService;

	@Resource
	protected NotificationService notificationService;

	private static final Object CLOSE_STATE = new Object();
	private static final Object REOPEN_STATE = new Object();
	private static final Object OPEN_STATE = new Object();


	public static final String OUTPUT_SOCKET = "state";
	public static final String TICKET_CLOSE_WIZARD = "csTicketCloseWizard";
	public static final String TICKET_REOPEN_WIZARD = "csTicketReopenWizard";

	@Override
	public void render(final Component parent, final EditorContext<Object> context, final EditorListener<Object> listener)
	{
		final CsTicketModel ticket = (CsTicketModel) context.getParameter("parentObject");
		final WidgetInstanceManager wim = (WidgetInstanceManager) context.getParameter(Editor.WIDGET_INSTANCE_MANAGER);

		// assign ticket to current Agent in case it's new
		if (context.getInitialValue().equals(CsTicketState.NEW) && userService.getCurrentUser() instanceof EmployeeModel
				&& ticket.getAssignedAgent() == null)
		{
			try
			{
				if (ticket.getAssignedAgent() == null)
				{
					ticketBusinessService.assignTicketToAgent(ticket, (EmployeeModel) userService.getCurrentUser());
					notificationService.notifyUser(notificationService.getWidgetNotificationSource(wim),
							NotificationEventTypes.EVENT_TYPE_OBJECT_UPDATE, NotificationEvent.Level.SUCCESS,
							Collections.singletonList(ticket));
				}

				ticketBusinessService.setTicketState(ticket, CsTicketState.OPEN);
				modelService.refresh(ticket);
			}
			catch (final TicketException e)
			{
				LOG.warn(e.getMessage(), e);
			}
		}

		final Combobox box = new Combobox();
		parent.appendChild(box);
		final ListModelList model = new ListModelList();
		model.add(ticket.getState());

		model.setSelection(Collections.singletonList(ticket.getState()));
		if (ticketBusinessService.isTicketClosed(ticket))
		{
			model.add(REOPEN_STATE);
		}
		else
		{
			model.addAll(ticketBusinessService.getTicketNextStates(ticket));
			model.add(CLOSE_STATE);
		}

		box.setModel(model);
		box.setReadonly(true);
		box.setAutodrop(true);
		box.setItemRenderer((item, data, index) -> //NOSONAR
		{
			if (TicketStateEditor.CLOSE_STATE.equals(data))
			{
				item.setLabel(Labels.getLabel("customersupport_backoffice_tickets_state_close"));
				item.setSclass("close");
				item.setValue(CLOSE_STATE);
			}
			else if (TicketStateEditor.REOPEN_STATE.equals(data))
			{
				item.setLabel(Labels.getLabel("customersupport_backoffice_tickets_state_reopen"));
				item.setSclass("reopen");
				item.setValue(REOPEN_STATE);
			}
			else if (TicketStateEditor.OPEN_STATE.equals(data))
			{
				item.setLabel(Labels.getLabel("customersupport_backoffice_tickets_state_open"));
				item.setSclass("reopen");
				item.setValue(OPEN_STATE);
			}
			else
			{
				item.setValue(data);
				String label = Labels.getLabel("customersupport_backoffice_tickets_state_" + data.toString().toLowerCase());
				if (StringUtils.isBlank(label))
				{
					label = String.valueOf(data);
				}
				item.setLabel(label);
			}

		});
		box.addEventListener("onSelect", event -> { //NOSONAR
			final Comboitem selectedItem = box.getSelectedItem();
			if (selectedItem != null)
			{
				if (CLOSE_STATE.equals(selectedItem.getValue()))
				{
					sendOutput(OUTPUT_SOCKET, createWizardContext(TICKET_CLOSE_WIZARD, ticket, wim));
				}
				else if (REOPEN_STATE.equals(selectedItem.getValue()))
				{
					sendOutput(OUTPUT_SOCKET, createWizardContext(TICKET_REOPEN_WIZARD, ticket, wim));
				}
				else
				{
					ticketBusinessService.setTicketState(ticket, selectedItem.getValue());
					notificationService.notifyUser(notificationService.getWidgetNotificationSource(wim),
							NotificationEventTypes.EVENT_TYPE_OBJECT_UPDATE, NotificationEvent.Level.SUCCESS,
							Collections.singletonList(ticket));
				}
				wim.getWidgetslot().updateView();
			}
		});
	}

	protected Map<String, Object> createWizardContext(final String typeName, final CsTicketModel ticket,
			final WidgetInstanceManager wim)
	{
		final Map<String, Object> wizardInput = new HashMap<>();
		wizardInput.put(ConfigurableFlowContextParameterNames.TYPE_CODE.getName(), typeName);
		wizardInput.put(ConfigurableFlowContextParameterNames.PARENT_OBJECT.getName(), ticket);
		wizardInput.put(ConfigurableFlowContextParameterNames.PARENT_OBJECT_TYPE.getName(), typeName);
		wizardInput.put(Editor.WIDGET_INSTANCE_MANAGER, wim);
		return wizardInput;
	}
}
