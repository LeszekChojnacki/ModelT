/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package com.hybris.backoffice.widgets.actions.workflow;

import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.workflow.model.WorkflowModel;

import javax.annotation.Resource;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;

import com.hybris.backoffice.workflow.WorkflowsTypeFacade;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionListener;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.actions.impl.DefaultActionRenderer;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.util.UITools;


/**
 * Action which allows to add attachments to the workflow.
 */
public class AddWorkflowAttachmentActionRenderer extends DefaultActionRenderer<WorkflowModel, Object>
{
	protected static final String NO_ICON_MENU = " ";
	protected static final String SCLASS_YA_ATTACHMENT_TYPE_SLECTOR = "ya-attachment-type-selector";
	protected static final String SCLASS_YW_POINTER_MENUPOPUP = "yw-pointer-menupopup";
	@Resource
	private WorkflowsTypeFacade workflowsTypeFacade;
	@Resource
	private LabelService labelService;

	@Override
	protected EventListener<? extends Event> createEventListener(final CockpitAction<WorkflowModel, Object> action,
			final ActionContext<WorkflowModel> context, final ActionListener<Object> listener)
	{
		final EventListener performEventListener = super.createEventListener(action, context, listener);
		return event -> {
			final Menupopup typeMenu = getOrCreateTypeMenu(event.getTarget(), context, performEventListener);
			typeMenu.open(event.getTarget(), "after_start");
		};
	}

	protected Menupopup getOrCreateTypeMenu(final Component parent, final ActionContext<WorkflowModel> context,
			final EventListener<Event> performListener)
	{
		for (final Component component : parent.getChildren())
		{
			if (component instanceof Menupopup)
			{
				return (Menupopup) component;
			}
		}

		final Menupopup popup = createAttachmentTypePopup(context, performListener);
		parent.appendChild(popup);
		return popup;
	}

	protected Menupopup createAttachmentTypePopup(final ActionContext<WorkflowModel> context,
			final EventListener<Event> performListener)
	{
		final Menupopup menu = new Menupopup();
		UITools.addSClass(menu, SCLASS_YA_ATTACHMENT_TYPE_SLECTOR);
		UITools.addSClass(menu, SCLASS_YW_POINTER_MENUPOPUP);
		workflowsTypeFacade.getSupportedAttachmentTypes().stream().map(type -> createTypeMenuEntry(context, performListener, type))
				.forEach(menu::appendChild);

		return menu;
	}

	protected Menuitem createTypeMenuEntry(final ActionContext<WorkflowModel> context, final EventListener<Event> performListener,
			final ComposedTypeModel type)
	{
		final Menuitem menuitem = new Menuitem(labelService.getShortObjectLabel(type));
		menuitem.setIconSclass(NO_ICON_MENU);
		menuitem.addEventListener(Events.ON_CLICK, event -> {
			context.setParameter(AddWorkflowAttachmentAction.PARAM_ATTACHMENT_TYPE, type.getCode());
			performListener.onEvent(event);
		});
		return menuitem;
	}

	public LabelService getLabelService()
	{
		return labelService;
	}

	public WorkflowsTypeFacade getWorkflowsTypeFacade()
	{
		return workflowsTypeFacade;
	}
}
