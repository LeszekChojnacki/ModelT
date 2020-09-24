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
package com.hybris.backoffice.workflow;

import de.hybris.platform.workflow.model.WorkflowActionModel;
import de.hybris.platform.workflow.model.WorkflowDecisionModel;

import com.hybris.cockpitng.engine.WidgetInstanceManager;


public interface WorkflowDecisionMaker
{

	/**
	 * Sets selected decision for given workflow action
	 *
	 * @param workflowAction
	 *           workflow action to decide on
	 * @param selectedDecision
	 *           decision to be made
	 * @param widgetInstanceManager
	 */
	void makeDecision(final WorkflowActionModel workflowAction, final WorkflowDecisionModel selectedDecision,
			final WidgetInstanceManager widgetInstanceManager);

}
