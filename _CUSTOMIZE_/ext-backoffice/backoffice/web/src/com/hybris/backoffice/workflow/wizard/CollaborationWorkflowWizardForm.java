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
package com.hybris.backoffice.workflow.wizard;


import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.workflow.model.WorkflowTemplateModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Pojo for workflow wizard creation.
 */
public class CollaborationWorkflowWizardForm
{
	private Map<Locale, String> name = new HashMap<>();
	private Map<Locale, String> description = new HashMap<>();
	private List<ItemModel> attachments = new ArrayList<>();
	private WorkflowTemplateModel workflowTemplate;
	private PrincipalModel assignee;

	public Map<Locale, String> getName()
	{
		return name;
	}

	public void setName(final Map<Locale, String> name)
	{
		this.name = name;
	}

	public Map<Locale, String> getDescription()
	{
		return description;
	}

	public void setDescription(final Map<Locale, String> description)
	{
		this.description = description;
	}

	public List<ItemModel> getAttachments()
	{
		return attachments;
	}

	public void setAttachments(final List<ItemModel> attachments)
	{
		this.attachments = attachments;
	}

	public WorkflowTemplateModel getWorkflowTemplate()
	{
		return workflowTemplate;
	}

	public void setWorkflowTemplate(final WorkflowTemplateModel workflowTemplate)
	{
		this.workflowTemplate = workflowTemplate;
	}

	public PrincipalModel getAssignee()
	{
		return assignee;
	}

	public void setAssignee(final PrincipalModel assignee)
	{
		this.assignee = assignee;
	}
}
