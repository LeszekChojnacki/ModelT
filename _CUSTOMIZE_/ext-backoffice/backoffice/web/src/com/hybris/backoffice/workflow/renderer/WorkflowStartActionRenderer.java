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

import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListColumn;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;


public class WorkflowStartActionRenderer implements WidgetComponentRenderer<Menupopup, ListColumn, WorkflowModel>
{
	private static final String LABEL_WORKFLOWS_ACTION_START = "workflow.action.start";
	protected static final String NO_ICON = " ";

	private Predicate<WorkflowModel> actionPredicate;
	private Function<WorkflowModel, Boolean> workflowStartActionExecutor;

	@Override
	public void render(final Menupopup menupopup, final ListColumn listColumn, final WorkflowModel workflowModel,
			final DataType dataType, final WidgetInstanceManager widgetInstanceManager)
	{
		if (getActionPredicate().negate().test(workflowModel))
		{
			return;
		}

		final Menuitem menuitem = new Menuitem();
		menuitem.setIconSclass(NO_ICON);
		menuitem.setLabel((Labels.getLabel(LABEL_WORKFLOWS_ACTION_START)));
		menuitem.addEventListener(Events.ON_CLICK, e -> getWorkflowStartActionExecutor().apply(workflowModel));
		menupopup.appendChild(menuitem);
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

	public Function<WorkflowModel, Boolean> getWorkflowStartActionExecutor()
	{
		return workflowStartActionExecutor;
	}

	@Required
	public void setWorkflowStartActionExecutor(final Function<WorkflowModel, Boolean> workflowStartActionExecutor)
	{
		this.workflowStartActionExecutor = workflowStartActionExecutor;
	}
}
