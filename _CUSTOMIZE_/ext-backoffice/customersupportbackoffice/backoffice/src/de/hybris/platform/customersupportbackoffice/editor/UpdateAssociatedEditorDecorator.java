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
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.QuoteModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.ticket.model.CsTicketModel;
import de.hybris.platform.ticket.service.TicketBusinessService;

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.zkoss.zk.ui.Component;

import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEventTypes;
import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.editors.EditorContext;
import com.hybris.cockpitng.editors.EditorListener;
import com.hybris.cockpitng.editors.impl.AbstractCockpitEditorRenderer;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.engine.impl.AbstractComponentWidgetAdapterAware;


public class UpdateAssociatedEditorDecorator extends AbstractCockpitEditorRenderer<String>
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
		final Object object = editorContext.getParameter("parentObject");
		if (object instanceof CsTicketModel)
		{
			final CsTicketModel ticket = (CsTicketModel) editorContext.getParameter("parentObject");
			ancestorEditor.addEventListener(Editor.ON_VALUE_CHANGED, event -> { //NOSONAR
				final AbstractOrderModel association = (AbstractOrderModel) event.getData();
				ticket.setOrder(association);
				ticketBusinessService.updateTicket(ticket);
				modelService.refresh(ticket);

				final List<Editor> editors = findEmbeddedEditors(parent);
				editors.forEach(editor -> {
					if ("com.hybris.cockpitng.editor.asmdeeplinkreferenceeditor".equalsIgnoreCase(editor.getComponentID())
							&& (editor.getEditorRenderer() instanceof AbstractComponentWidgetAdapterAware))
					{
						((AbstractComponentWidgetAdapterAware) editor.getEditorRenderer()).sendOutput("itemSelectedForDecorator",
								ticket);
					}
				});

				notificationService.notifyUser(notificationService.getWidgetNotificationSource(wim),
						NotificationEventTypes.EVENT_TYPE_OBJECT_UPDATE, NotificationEvent.Level.SUCCESS,
						Collections.singletonList(ticket));

				wim.getWidgetslot().updateView();
			});
		}
		else if (object instanceof QuoteModel)
		{
			final QuoteModel quote = (QuoteModel) object;
			final List<Editor> editors = findEmbeddedEditors(parent);
			editors.forEach(editor -> {
				if ("com.hybris.cockpitng.editor.asmdeeplinkreferenceeditor".equalsIgnoreCase(editor.getComponentID())
						&& (editor.getEditorRenderer() instanceof AbstractComponentWidgetAdapterAware))
				{
					((AbstractComponentWidgetAdapterAware) editor.getEditorRenderer()).sendOutput("itemSelectedForDecorator", quote);
				}
			});
		}
	}
}
