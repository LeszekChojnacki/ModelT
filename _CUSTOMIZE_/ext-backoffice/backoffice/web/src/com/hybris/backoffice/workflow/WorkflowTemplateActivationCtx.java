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

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.workflow.model.WorkflowTemplateModel;

import java.util.Collections;
import java.util.Map;

import com.hybris.cockpitng.dataaccess.context.impl.DefaultContext;


/**
 * Context which holds information necessary to evaluate {@link WorkflowTemplateModel#getActivationScript()}.
 */
public class WorkflowTemplateActivationCtx extends DefaultContext
{
	private final ItemModel item;
	private final Map<String, Object> currentValues;
	private final Map<String, Object> initialValues;
	private final String workflowOperationType;

	/**
	 *
	 * @param item an item being saved
	 * @param currentValues new values which will be saved
	 * @param initialValues original values of modified attributes
	 * @param workflowOperationType one of given values
	 *           {@link de.hybris.platform.workflow.constants.WorkflowConstants.WorkflowActivationScriptActions}
	 */
	public WorkflowTemplateActivationCtx(final ItemModel item, final Map<String, Object> currentValues,
			final Map<String, Object> initialValues, final String workflowOperationType)
	{
		this.item = item;
		this.currentValues = currentValues;
		this.initialValues = initialValues;
		this.workflowOperationType = workflowOperationType;
	}

	/**
	 * This constructor should be used when item is being created.
	 * 
	 * @param item item being created.
	 * @param initialValues list of values used for object creation.
	 * @param workflowOperationType one of given values
	 *           {@link de.hybris.platform.workflow.constants.WorkflowConstants.WorkflowActivationScriptActions}
	 */
	public WorkflowTemplateActivationCtx(final ItemModel item, final Map<String, Object> initialValues,
			final String workflowOperationType)
	{
		this.item = item;
		this.currentValues = Collections.emptyMap();
		this.initialValues = initialValues;
		this.workflowOperationType = workflowOperationType;
	}

	public Map<String, Object> getCurrentValues()
	{
		return currentValues;
	}

	public Map<String, Object> getInitialValues()
	{
		return initialValues;
	}

	public ItemModel getItem()
	{
		return item;
	}

	public String getWorkflowOperationType()
	{
		return workflowOperationType;
	}
}
