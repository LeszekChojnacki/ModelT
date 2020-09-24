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

import de.hybris.platform.workflow.model.WorkflowTemplateModel;


/**
 * Enum representation of
 * {@link de.hybris.platform.workflow.constants.WorkflowConstants.WorkflowActivationScriptActions}. It is used to
 * determine type of action for {@link WorkflowTemplateModel#getActivationScript()} evaluation.
 */
public enum WorkflowTemplateActivationAction
{
	CREATE, SAVE, REMOVE
}
