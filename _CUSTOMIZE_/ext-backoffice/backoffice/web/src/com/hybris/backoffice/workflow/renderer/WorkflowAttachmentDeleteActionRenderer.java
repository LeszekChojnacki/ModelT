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
package com.hybris.backoffice.workflow.renderer;

import de.hybris.platform.workflow.model.WorkflowItemAttachmentModel;

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;

import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListColumn;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;


public class WorkflowAttachmentDeleteActionRenderer
		implements WidgetComponentRenderer<Menupopup, ListColumn, WorkflowItemAttachmentModel>
{
	protected static final String LABEL_WORKFLOW_ATTACHMENT_ACTION_DELETE = "workflows.attachments.action.delete";
	protected static final String NO_ICON = " ";

	private Predicate<WorkflowItemAttachmentModel> actionPredicate;
	private Consumer<WorkflowItemAttachmentModel> workflowAttachmentDeleteActionExecutor;


	@Override
	public void render(final Menupopup menupopup, final ListColumn configuration,
			final WorkflowItemAttachmentModel workflowAttachment, final DataType dataType,
			final WidgetInstanceManager widgetInstanceManager)
	{
		if (getActionPredicate().negate().test(workflowAttachment))
		{
			return;
		}

		final Menuitem menuitem = new Menuitem();
		menuitem.setIconSclass(NO_ICON);
		menuitem.setLabel((widgetInstanceManager.getLabel(LABEL_WORKFLOW_ATTACHMENT_ACTION_DELETE)));
		menuitem.addEventListener(Events.ON_CLICK, e -> getWorkflowAttachmentDeleteActionExecutor().accept(workflowAttachment));
		menupopup.appendChild(menuitem);

	}

	protected Predicate<WorkflowItemAttachmentModel> getActionPredicate()
	{
		return actionPredicate;
	}

	@Required
	public void setActionPredicate(final Predicate<WorkflowItemAttachmentModel> actionPredicate)
	{
		this.actionPredicate = actionPredicate;
	}

	public Consumer getWorkflowAttachmentDeleteActionExecutor()
	{
		return workflowAttachmentDeleteActionExecutor;
	}

	@Required
	public void setWorkflowAttachmentDeleteActionExecutor(final Consumer workflowAttachmentDeleteActionExecutor)
	{
		this.workflowAttachmentDeleteActionExecutor = workflowAttachmentDeleteActionExecutor;
	}
}
