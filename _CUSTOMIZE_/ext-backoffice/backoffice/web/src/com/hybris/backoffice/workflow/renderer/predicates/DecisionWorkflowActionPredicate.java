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
package com.hybris.backoffice.workflow.renderer.predicates;

import de.hybris.platform.workflow.enums.WorkflowActionStatus;
import de.hybris.platform.workflow.model.WorkflowActionModel;

import java.util.function.Predicate;

public class DecisionWorkflowActionPredicate implements Predicate<WorkflowActionModel>
{
	@Override
	public boolean test(final WorkflowActionModel workflowActionModel) {
		return WorkflowActionStatus.IN_PROGRESS.equals(workflowActionModel.getStatus());
	}
}
