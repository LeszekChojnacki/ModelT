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

import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.workflow.model.WorkflowTemplateModel;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.cockpitng.editor.commonreferenceeditor.ReferenceEditorSearchFacade;
import com.hybris.cockpitng.search.data.SearchQueryData;
import com.hybris.cockpitng.search.data.pageable.Pageable;
import com.hybris.cockpitng.search.data.pageable.PageableList;


/**
 * Search provider for supported workflow attachment type. Given search text
 * {@link SearchQueryData#getSearchQueryText()} will be used to filter types by name {@link ComposedTypeModel#getName()}
 */
public class WorkflowAttachmentTypeReferenceSearchFacade implements ReferenceEditorSearchFacade<ComposedTypeModel>
{

	private WorkflowsTypeFacade workflowsTypeFacade;

	@Override
	public Pageable<ComposedTypeModel> search(final SearchQueryData searchQueryData)
	{

		final List<ComposedTypeModel> composedTypes = workflowsTypeFacade.getSupportedAttachmentTypes();

		return new PageableList<>(filterTypesByNames(composedTypes, searchQueryData.getSearchQueryText()),
				searchQueryData.getPageSize(), WorkflowTemplateModel._TYPECODE);
	}

	protected List<ComposedTypeModel> filterTypesByNames(final List<ComposedTypeModel> types, final String searchText)
	{
		if (StringUtils.isNotEmpty(searchText))
		{
			return types.stream().filter(type -> StringUtils.containsIgnoreCase(type.getName(), searchText))
					.collect(Collectors.toList());
		}
		return types;
	}

	@Required
	public void setWorkflowsTypeFacade(final WorkflowsTypeFacade workflowsTypeFacade)
	{
		this.workflowsTypeFacade = workflowsTypeFacade;
	}

	public WorkflowsTypeFacade getWorkflowsTypeFacade()
	{
		return workflowsTypeFacade;
	}
}
