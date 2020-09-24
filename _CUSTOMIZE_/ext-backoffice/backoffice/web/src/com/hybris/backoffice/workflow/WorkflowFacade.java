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
import de.hybris.platform.workflow.model.WorkflowActionModel;
import de.hybris.platform.workflow.model.WorkflowModel;
import de.hybris.platform.workflow.model.WorkflowTemplateModel;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.hybris.cockpitng.dataaccess.facades.object.exceptions.ObjectDeletionException;
import com.hybris.cockpitng.search.data.pageable.Pageable;


/**
 * Facade used to fetch {@link WorkflowModel}s.
 */
public interface WorkflowFacade extends CoreWorkflowFacade
{

	/**
	 * Gets workflows based on given search data.
	 *
	 * @param workflowSearchData
	 *           search data which allows to specify search criteria.
	 * @return pageable with workflows {@link Pageable}
	 */
	Pageable<WorkflowModel> getWorkflows(WorkflowSearchData workflowSearchData);

	/**
	 * Gets workflow actions for the current user.
	 *
	 * @return workflow actions.
	 */
	List<WorkflowActionModel> getWorkflowActions();


	/**
	 * Gets workflow templates visible for current user.
	 *
	 * @return list of workflow templates
	 */
	List<WorkflowTemplateModel> getAllVisibleWorkflowTemplatesForCurrentUser();


	/**
	 * Creates workflow from given template for current user.
	 *
	 * @param workflowTemplate
	 *           template to start.
	 * @param localizedName
	 *           localized name.
	 * @param localizedDesc
	 *           localized workflow description.
	 * @param attachments
	 *           list of items to be attached to the workflow.
	 * @return created workflow.
	 */
	Optional<WorkflowModel> createWorkflow(WorkflowTemplateModel workflowTemplate, Map<Locale, String> localizedName,
			Map<Locale, String> localizedDesc, List<ItemModel> attachments);

	/**
	 * Creates adHocWorkflow from given template for current user.
	 *
	 * @param assignee
	 *           adHocTemplate assignee.
	 * @param localizedName
	 *           localized name.
	 * @param localizedDesc
	 *           localized workflow description.
	 * @param attachments
	 *           list of items to be attached to the workflow.
	 * @return if workflow is created with given assignee then it will return create workflow.
	 */
	Optional<WorkflowModel> createAdHocWorkflow(PrincipalModel assignee, Map<Locale, String> localizedName,
			Map<Locale, String> localizedDesc, List<ItemModel> attachments);

	/**
	 * Deletes given workflow
	 *
	 * @param workflow
	 *           workflow to delete
	 * @throws ObjectDeletionException
	 *            when object cannot be deleted
	 */
	void deleteWorkflow(final WorkflowModel workflow) throws ObjectDeletionException;

}
