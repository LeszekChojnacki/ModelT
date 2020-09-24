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

import de.hybris.platform.workflow.model.WorkflowModel;

import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Messagebox;

import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListColumn;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.util.MessageboxUtils;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;


public class WorkflowTerminateActionRenderer implements WidgetComponentRenderer<Menupopup, ListColumn, WorkflowModel>
{
	protected static final String LABEL_WORKFLOWS_ACTION_TERMINATE = "workflow.action.terminate";
	protected static final String LABEL_WORKFLOWS_ACTION_CONFIRMATION_MESSAGE_TERMINATE = "workflow.action.confirmation.message.terminate";
	protected static final String LABEL_WORKFLOWS_ACTION_CONFIRMATION_TITLE_TERMINATE = "workflow.action.confirmation.title.terminate";
	protected static final String NO_ICON = " ";

	private Function<WorkflowModel, Boolean> workflowTerminateActionExecutor;
	private Predicate<WorkflowModel> actionPredicate;
	private LabelService labelService;
	private boolean needsConfirmation = true;

	@Override
	public void render(final Menupopup menupopup, final ListColumn listColumn, final WorkflowModel workflowModel,
			final DataType dataType, final WidgetInstanceManager widgetInstanceManager)
	{
		if (actionPredicate.negate().test(workflowModel))
		{
			return;
		}

		final Menuitem menuitem = new Menuitem();
		menuitem.setIconSclass(NO_ICON);
		menuitem.setLabel(Labels.getLabel(LABEL_WORKFLOWS_ACTION_TERMINATE));
		menuitem.addEventListener(Events.ON_CLICK, e -> messageBoxPerform(workflowModel));
		menupopup.appendChild(menuitem);
	}

	protected void messageBoxPerform(final WorkflowModel workflowModel)
	{
		if (isNeedsConfirmation())
		{
			Messagebox.show(Labels.getLabel(LABEL_WORKFLOWS_ACTION_CONFIRMATION_MESSAGE_TERMINATE, new String[]
			{ getLabelService().getObjectLabel(workflowModel) }),
					Labels.getLabel(LABEL_WORKFLOWS_ACTION_CONFIRMATION_TITLE_TERMINATE),
					MessageboxUtils.order(Messagebox.Button.CANCEL, Messagebox.Button.YES), null, Messagebox.QUESTION, null,
					clickEvent -> {
						if (Messagebox.Button.YES.equals(clickEvent.getButton()))
						{
							perform(workflowModel);
						}
					});
		}
		else
		{
			perform(workflowModel);
		}
	}

	protected void perform(final WorkflowModel workflowModel)
	{
		workflowTerminateActionExecutor.apply(workflowModel);
	}

	public Function<WorkflowModel, Boolean> getWorkflowTerminateActionExecutor()
	{
		return workflowTerminateActionExecutor;
	}

	@Required
	public void setWorkflowTerminateActionExecutor(final Function<WorkflowModel, Boolean> workflowTerminateActionExecutor)
	{
		this.workflowTerminateActionExecutor = workflowTerminateActionExecutor;
	}

	public Predicate<WorkflowModel> getActionPredicate()
	{
		return actionPredicate;
	}

	@Required
	public void setActionPredicate(final Predicate<WorkflowModel> actionPredicate)
	{
		this.actionPredicate = actionPredicate;
	}

	public boolean isNeedsConfirmation()
	{
		return needsConfirmation;
	}

	public void setNeedsConfirmation(final boolean needsConfirmation)
	{
		this.needsConfirmation = needsConfirmation;
	}

	public LabelService getLabelService()
	{
		return labelService;
	}

	@Required
	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}
}
