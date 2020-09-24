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
package com.hybris.backoffice.widgets.workflowdetails.renderer;

import de.hybris.platform.workflow.model.WorkflowModel;

import java.util.Date;

import org.springframework.beans.factory.annotation.Required;
import org.zkoss.zk.ui.Component;

import com.hybris.backoffice.workflow.WorkflowFacade;
import com.hybris.cockpitng.components.Editor;
import com.hybris.cockpitng.core.model.WidgetModel;
import com.hybris.cockpitng.editors.EditorContext;
import com.hybris.cockpitng.editors.EditorListener;
import com.hybris.cockpitng.editors.impl.AbstractCockpitEditorRenderer;
import com.hybris.cockpitng.widgets.baseeditorarea.DefaultEditorAreaController;


public class DefaultWorkflowStartedTimeEditorRenderer extends AbstractCockpitEditorRenderer<Date>
{
	private WorkflowFacade workflowFacade;

	@Override
	public void render(final Component parent, final EditorContext<Date> context, final EditorListener<Date> listener)
	{
		final Editor ancestorEditor = findAncestorEditor(parent);

		final WidgetModel widgetModel = ancestorEditor.getWidgetInstanceManager().getModel();
		if (widgetModel != null)
		{
			final WorkflowModel workflow = widgetModel.getValue(DefaultEditorAreaController.MODEL_CURRENT_OBJECT,
					WorkflowModel.class);
			final Date startDate = workflowFacade.getWorkflowStartTime(workflow);
			if (startDate != null)
			{
				ancestorEditor.setInitialValue(startDate);
			}
		}
	}

	public WorkflowFacade getWorkflowFacade()
	{
		return workflowFacade;
	}

	@Required
	public void setWorkflowFacade(final WorkflowFacade workflowFacade)
	{
		this.workflowFacade = workflowFacade;
	}
}
