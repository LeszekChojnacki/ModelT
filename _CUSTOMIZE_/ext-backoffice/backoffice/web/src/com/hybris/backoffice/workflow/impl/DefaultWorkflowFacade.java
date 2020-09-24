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
package com.hybris.backoffice.workflow.impl;


import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.workflow.WorkflowActionService;
import de.hybris.platform.workflow.WorkflowTemplateService;
import de.hybris.platform.workflow.model.WorkflowActionModel;
import de.hybris.platform.workflow.model.WorkflowModel;
import de.hybris.platform.workflow.model.WorkflowTemplateModel;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.workflow.WorkflowFacade;
import com.hybris.backoffice.workflow.WorkflowSearchData;
import com.hybris.backoffice.workflow.WorkflowsTypeFacade;
import com.hybris.cockpitng.dataaccess.context.impl.DefaultContext;
import com.hybris.cockpitng.dataaccess.facades.object.ObjectFacade;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectDeletionException;
import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectSavingException;
import com.hybris.cockpitng.dataaccess.facades.object.impl.DefaultObjectFacade;
import com.hybris.cockpitng.dataaccess.facades.type.TypeFacade;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.cockpitng.util.BackofficeSpringUtil;


/**
 * Default implementation of the WorkflowFacade.
 */
public class DefaultWorkflowFacade extends DefaultCoreWorkflowFacade implements WorkflowFacade
{
	protected static final String AD_HOC_WORKFLOW_DUMMY_NAME = "adHocWorkflow";
	private static final Logger LOG = LoggerFactory.getLogger(DefaultWorkflowFacade.class);
	private WorkflowActionService workflowActionService;
	private WorkflowTemplateService workflowTemplateService;
	private UserService userService;
	private TypeFacade typeFacade;
	private WorkflowsTypeFacade workflowsTypeFacade;
	private ObjectFacade objectFacade;

	@Override
	public List<WorkflowActionModel> getWorkflowActions()
	{
		return getWorkflowActionService()
				.getAllUserWorkflowActionsWithAttachments(workflowsTypeFacade.getAllAttachmentClassNames());
	}

	@Override
	public Pageable<WorkflowModel> getWorkflows(final WorkflowSearchData workflowSearchData)
	{
		return BackofficeSpringUtil.getBean("workflowSearchPageable", workflowSearchData);
	}

	@Override
	public List<WorkflowTemplateModel> getAllVisibleWorkflowTemplatesForCurrentUser()
	{
		return getWorkflowTemplateService().getAllVisibleWorkflowTemplatesForUser(getUserService().getCurrentUser());
	}

	@Override
	public Optional<WorkflowModel> createWorkflow(final WorkflowTemplateModel workflowTemplate,
			final Map<Locale, String> localizedName, final Map<Locale, String> localizedDesc, final List<ItemModel> attachments)
	{
		final WorkflowModel workflow = getWorkflowService().createWorkflow(workflowTemplate.getName(), workflowTemplate,
				assureAttachmentsWithoutDuplicates(attachments), getUserService().getCurrentUser());

		localizedName.forEach((loc, name) -> workflow.setName(name, loc));
		localizedDesc.forEach((loc, desc) -> workflow.setDescription(desc, loc));
		return persistWorkflow(workflow);
	}

	protected Optional<WorkflowModel> persistWorkflow(final WorkflowModel workflow)
	{
		try
		{
			final DefaultContext ctx = new DefaultContext();
			ctx.addAttribute(DefaultObjectFacade.CTX_PARAM_SUPPRESS_EVENT, Boolean.TRUE);
			return Optional.of(getObjectFacade().save(workflow, ctx));
		}
		catch (final ObjectSavingException ose)
		{
			LOG.warn("Could not save workflow", ose);
			return Optional.empty();
		}
	}

	@Override
	public Optional<WorkflowModel> createAdHocWorkflow(final PrincipalModel assignee, final Map<Locale, String> localizedName,
			final Map<Locale, String> localizedDesc, final List<ItemModel> attachments)
	{
		final WorkflowModel workflow = getWorkflowService().createAdhocWorkflow(AD_HOC_WORKFLOW_DUMMY_NAME,
				assureAttachmentsWithoutDuplicates(attachments), getUserService().getCurrentUser());
		if (getWorkflowService().assignUser(assignee, workflow))
		{
			localizedName.forEach((loc, name) -> workflow.setName(name, loc));
			localizedDesc.forEach((loc, desc) -> workflow.setDescription(desc, loc));
			return persistWorkflow(workflow);
		}
		return Optional.empty();
	}

	protected List<ItemModel> assureAttachmentsWithoutDuplicates(final List<ItemModel> attachments)
	{
		return attachments != null ? attachments.stream().distinct().collect(Collectors.toList()) : Collections.emptyList();
	}

	@Override
	public void deleteWorkflow(final WorkflowModel workflow) throws ObjectDeletionException
	{
		getObjectFacade().delete(workflow);
	}

	protected UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	protected TypeFacade getTypeFacade()
	{
		return typeFacade;
	}

	@Required
	public void setTypeFacade(final TypeFacade typeFacade)
	{
		this.typeFacade = typeFacade;
	}

	protected WorkflowActionService getWorkflowActionService()
	{
		return workflowActionService;
	}

	@Required
	public void setWorkflowActionService(final WorkflowActionService workflowActionService)
	{
		this.workflowActionService = workflowActionService;
	}

	protected WorkflowsTypeFacade getWorkflowsTypeFacade()
	{
		return workflowsTypeFacade;
	}

	public void setWorkflowsTypeFacade(final WorkflowsTypeFacade workflowsTypeFacade)
	{
		this.workflowsTypeFacade = workflowsTypeFacade;
	}

	public ObjectFacade getObjectFacade()
	{
		return objectFacade;
	}

	@Required
	public void setObjectFacade(final ObjectFacade objectFacade)
	{
		this.objectFacade = objectFacade;
	}

	@Override
	public WorkflowTemplateService getWorkflowTemplateService()
	{
		return workflowTemplateService;
	}

	@Override
	@Required
	public void setWorkflowTemplateService(final WorkflowTemplateService workflowTemplateService)
	{
		this.workflowTemplateService = workflowTemplateService;
	}
}
