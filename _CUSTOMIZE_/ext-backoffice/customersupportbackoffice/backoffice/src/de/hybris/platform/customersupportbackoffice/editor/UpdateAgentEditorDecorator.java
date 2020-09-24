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
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticket.service.TicketBusinessService;

import java.util.Collections;

import javax.annotation.Resource;

import org.zkoss.zk.ui.Component;

import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEventTypes;
import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.editors.EditorContext;
import com.hybris.cockpitng.editors.EditorListener;
import com.hybris.cockpitng.editors.impl.AbstractCockpitEditorRenderer;
import com.hybris.cockpitng.engine.WidgetInstanceManager;


public class UpdateAgentEditorDecorator extends AbstractCockpitEditorRenderer<String>
{
	@Resource
	protected TicketBusinessService ticketBusinessService;
	@Resource
	protected ModelService modelService;
	@Resource
	protected NotificationService notificationService;
	@Override
	public void render(final Component parent, final EditorContext<String> editorContext,
			final EditorListener<String> editorListener)
	{
		final Editor ancestorEditor = findAncestorEditor(parent);
		final WidgetInstanceManager wim = (WidgetInstanceManager) editorContext.getParameter(Editor.WIDGET_INSTANCE_MANAGER);
		final CsTicketModel ticket = (CsTicketModel) editorContext.getParameter("parentObject");
		ancestorEditor.addEventListener(Editor.ON_VALUE_CHANGED, event -> {
			final EmployeeModel agent = (EmployeeModel) event.getData();
			modelService.refresh(ticket);
			ticketBusinessService.assignTicketToAgent(ticket, agent);
			modelService.refresh(ticket);
			notificationService.notifyUser(notificationService.getWidgetNotificationSource(wim),
					NotificationEventTypes.EVENT_TYPE_OBJECT_UPDATE, NotificationEvent.Level.SUCCESS,
					Collections.singletonList(ticket));
			wim.getWidgetslot().updateView();
		});
	}
}
