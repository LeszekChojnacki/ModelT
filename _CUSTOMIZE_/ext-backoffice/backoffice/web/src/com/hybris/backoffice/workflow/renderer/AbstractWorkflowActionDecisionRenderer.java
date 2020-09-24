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

import de.hybris.platform.workflow.model.WorkflowActionModel;
import de.hybris.platform.workflow.model.WorkflowDecisionModel;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.workflow.impl.DefaultWorkflowDecisionMaker;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.labels.LabelService;
import com.hybris.cockpitng.widgets.common.AbstractWidgetComponentRenderer;


public abstract class AbstractWorkflowActionDecisionRenderer<PARENT, CONFIG, DATA>
		extends AbstractWidgetComponentRenderer<PARENT, CONFIG, DATA>
{

	private LabelService labelService;

	private DefaultWorkflowDecisionMaker workflowDecisionMaker;


	protected void makeDecision(final WorkflowActionModel workflowAction, final WorkflowDecisionModel selectedDecision,
			final WidgetInstanceManager widgetInstanceManager)
	{
		getWorkflowDecisionMaker().makeDecision(workflowAction, selectedDecision, widgetInstanceManager);
	}

	protected String getDecisionLabel(final WorkflowDecisionModel workflowDecision)
	{
		return getLabelService().getObjectLabel(workflowDecision);
	}

	protected LabelService getLabelService()
	{
		return labelService;
	}

	@Required
	public void setLabelService(final LabelService labelService)
	{
		this.labelService = labelService;
	}

	protected DefaultWorkflowDecisionMaker getWorkflowDecisionMaker()
	{
		return workflowDecisionMaker;
	}

	@Required
	public void setWorkflowDecisionMaker(final DefaultWorkflowDecisionMaker workflowDecisionMaker)
	{
		this.workflowDecisionMaker = workflowDecisionMaker;
	}
}
