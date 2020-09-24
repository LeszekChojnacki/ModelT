/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousing.taskassignment.strategy;

import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.workflow.model.WorkflowModel;


/**
 * Strategy to define which user {@link UserModel} to get in order to give ownership of a {@link de.hybris.platform.workflow.model.WorkflowActionModel}
 */
public interface UserSelectionStrategy
{
	/**
	 * Gets the {@link UserModel} to which a consignment should be assigned
	 *
	 * @param workflow
	 * 		the {@link WorkflowModel} to which the {@link de.hybris.platform.ordersplitting.model.ConsignmentModel} is attached
	 * @return the {@link UserModel} to which the workflow should be assigned
	 */
	UserModel getUserForConsignmentAssignment(WorkflowModel workflow);
}
