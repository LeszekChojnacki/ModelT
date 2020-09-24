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
import de.hybris.platform.ticket.enums.CsTicketCategory;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticket.service.TicketBusinessService;
import de.hybris.platform.ticket.service.TicketException;

import java.util.Collections;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;

import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEventTypes;
import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.editor.defaultenum.DefaultEnumEditor;
import com.hybris.cockpitng.editors.EditorContext;
import com.hybris.cockpitng.editors.EditorListener;
import com.hybris.cockpitng.engine.WidgetInstanceManager;


public class TicketCategoryEditor extends DefaultEnumEditor
{
	@Resource
	protected TicketBusinessService ticketBusinessService;
	@Resource
	protected NotificationService notificationService;
	private static final Logger LOG = LoggerFactory.getLogger(TicketCategoryEditor.class);

	@Override
	public void render(final Component parent, final EditorContext<Object> context, final EditorListener<Object> listener)
	{
		context.setOptional(false);
		final CsTicketModel ticket = (CsTicketModel) context.getParameter("parentObject");
		final WidgetInstanceManager wim = (WidgetInstanceManager) context.getParameter(Editor.WIDGET_INSTANCE_MANAGER);
		super.render(parent, context, new EditorListener<Object>() //NOSONAR
		{

			@Override
			public void onValueChanged(final Object o)
			{
				ticket.setCategory((CsTicketCategory) o);
				try
				{
					ticketBusinessService.updateTicket(ticket);
					wim.getWidgetslot().updateView();
					notificationService.notifyUser(notificationService.getWidgetNotificationSource(wim),
							NotificationEventTypes.EVENT_TYPE_OBJECT_UPDATE, NotificationEvent.Level.SUCCESS,
							Collections.singletonList(ticket));
				}
				catch (final TicketException e)
				{
					LOG.error(e.getMessage(), e);
				}
				listener.onValueChanged(o);
			}

			@Override
			public void onEditorEvent(final String s)
			{
				listener.onEditorEvent(s);
			}

			@Override
			public void sendSocketOutput(final String s, final Object o)
			{
				listener.sendSocketOutput(s, o);
			}
		});
	}
}
