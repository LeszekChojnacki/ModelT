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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.editor.commonreferenceeditor.ReferenceEditorSearchFacade;
import com.hybris.cockpitng.search.data.SearchQueryData;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.cockpitng.search.data.pageable.PageableList;


/**
 * Search provider for {@link WorkflowTemplateModel}. Returns templates of which visibility is controlled by
 * {@link WorkflowTemplateModel#getOwner()} and {@link WorkflowTemplateModel#getVisibleForPrincipals()}. Given search
 * text {@link SearchQueryData#getSearchQueryText()} will be used to filter templates by name
 * {@link WorkflowTemplateModel#getName()}
 */
public class WorkflowTemplateReferenceSearchFacade implements ReferenceEditorSearchFacade<WorkflowTemplateModel>
{

	private WorkflowFacade workflowFacade;

	@Override
	public Pageable<WorkflowTemplateModel> search(final SearchQueryData searchQueryData)
	{
		final List<WorkflowTemplateModel> visibleTemplates = workflowFacade.getAllVisibleWorkflowTemplatesForCurrentUser();
		final List<WorkflowTemplateModel> resultTemplates = new ArrayList<>(visibleTemplates);

		final WorkflowTemplateModel adHocWorkflowTemplate = workflowFacade.getAdHocWorkflowTemplate();
		if (adHocWorkflowTemplate != null)
		{
			resultTemplates.add(adHocWorkflowTemplate);
		}

		return new PageableList<>(filterTemplatesByName(resultTemplates, searchQueryData.getSearchQueryText()),
				searchQueryData.getPageSize(), WorkflowTemplateModel._TYPECODE);
	}

	protected List<WorkflowTemplateModel> filterTemplatesByName(final List<WorkflowTemplateModel> templates,
			final String searchText)
	{
		if (StringUtils.isNotEmpty(searchText))
		{
			return templates.stream().filter(template -> StringUtils.containsIgnoreCase(template.getName(), searchText))
					.collect(Collectors.toList());
		}
		return templates;
	}

	public WorkflowFacade getWorkflowFacade()
	{
		return workflowFacade;
	}

	@Required
	public void setWorkflowFacade(final WorkflowFacade workflowFacade)
	{
		this.workflowFacade = workflowFacade;
	}
}
