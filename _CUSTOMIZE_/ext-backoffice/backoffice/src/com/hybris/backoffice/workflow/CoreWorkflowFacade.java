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
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.workflow.WorkflowStatus;
import de.hybris.platform.workflow.model.WorkflowActionModel;
import de.hybris.platform.workflow.model.WorkflowItemAttachmentModel;
import de.hybris.platform.workflow.model.WorkflowModel;
import de.hybris.platform.workflow.model.WorkflowTemplateModel;

import java.util.Date;
import java.util.List;


/**
 * Facade to handle core workflow functionality (independent of the web context)
 */
public interface CoreWorkflowFacade
{

	/**
	 * Adds items to workflow as an attachment
	 *
	 * @param workflow
	 *           to which items should be added
	 * @param itemsToAdd
	 *           items to add
	 * @return attachments
	 */
	List<WorkflowItemAttachmentModel> addItems(WorkflowModel workflow, List<? extends ItemModel> itemsToAdd);

	/**
	 * Loads {@link WorkflowTemplateModel} by given code.
	 *
	 * @param code
	 *           code of the {@link WorkflowTemplateModel} to be loaded
	 * @return {@link WorkflowTemplateModel} instance or null if the implementation cannot find appropriate
	 *         {@link WorkflowTemplateModel}
	 */
	WorkflowTemplateModel getWorkflowTemplateForCode(final String code);

	/**
	 * Gets adHoc workflow template defined for the system.
	 * {@link de.hybris.platform.workflow.WorkflowTemplateService#getAdhocWorkflowTemplate()}
	 *
	 * @return adHoc workflow template.
	 */
	WorkflowTemplateModel getAdHocWorkflowTemplate();

	/**
	 * Creates an instance of {@link WorkflowModel} with given name, template, attached items and user.
	 *
	 * @param name
	 *           name of the workflow
	 * @param template
	 *           {@link WorkflowTemplateModel} to be used
	 * @param itemsToAdd
	 *           item to be added to the workflow
	 * @param owner
	 *           owner of the workflow
	 * @return {@link WorkflowModel} based on the given arguments
	 */
	WorkflowModel createWorkflow(String name, WorkflowTemplateModel template, List<ItemModel> itemsToAdd, UserModel owner);

	/**
	 * Starts given workflow
	 *
	 * @param workflow
	 *           workflow to start
	 * @return true if workflow has been started.
	 */
	boolean startWorkflow(WorkflowModel workflow);

	/**
	 * Tells if workflow can be started - has assigned users to all actions etc.
	 * {@link de.hybris.platform.workflow.WorkflowService#canBeStarted(WorkflowModel)}
	 *
	 * @param workflow
	 *           workflow to be started
	 * @return true if workflow can be started.
	 */
	boolean canBeStarted(WorkflowModel workflow);

	/**
	 * Tells is given template is adHocTemplate
	 * {@link de.hybris.platform.workflow.WorkflowService#isAdhocWorkflow(WorkflowModel)}.
	 *
	 * @param template
	 *           template to be checked.
	 * @return true if the template is adHocTemplate.
	 */
	boolean isAdHocTemplate(WorkflowTemplateModel template);

	/**
	 * Tells if given principal can be assigned to a adHoc template. It cannot be
	 * {@link de.hybris.platform.workflow.WorkflowTemplateService#getAdhocWorkflowTemplateDummyOwner()}
	 *
	 * @param adHocAssignedUser
	 *           user to be verified if can be assigned to an adHoc template
	 * @return true if given principal can be assigned to an adHoc template.
	 */
	boolean isCorrectAdHocAssignee(PrincipalModel adHocAssignedUser);

	/**
	 * Checks {@link WorkflowStatus} of workflow model
	 *
	 * @param workflowModel
	 *           model of workflow
	 * @return status of workflow or <code>null</code> if it was impossible to determine
	 */
	WorkflowStatus getWorkflowStatus(final WorkflowModel workflowModel);

	/**
	 * Terminates given workflow
	 *
	 * @param workflow
	 *           workflow to terminate
	 * @return true if workflow has been terminated.
	 */
	boolean terminateWorkflow(final WorkflowModel workflow);

	/**
	 * Gets all WorkflowAction items with status {@link de.hybris.platform.workflow.enums.WorkflowActionStatus#IN_PROGRESS}.
	 *
	 * @param workflowModel
	 *           workflow to check.
	 * @return all WorkflowAction items in progress.
	 */
	List<WorkflowActionModel> getCurrentTasks(final WorkflowModel workflowModel);

	/**
	 * Counts all decisions to make in all actions of the workflow.
	 *
	 * @param workflowModel
	 *           workflow to check.
	 * @return all decisions to make in all actions of the workflow
	 */
	int countDecisions(final WorkflowModel workflowModel);

	/**
	 * Returns start time of given workflow.
	 *
	 * @param workflow
	 *           workflow to check.
	 * @return start time of given workflow.
	 */
	Date getWorkflowStartTime(final WorkflowModel workflow);

}
