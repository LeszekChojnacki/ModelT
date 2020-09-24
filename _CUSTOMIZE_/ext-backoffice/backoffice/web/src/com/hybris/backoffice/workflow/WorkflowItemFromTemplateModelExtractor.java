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

import de.hybris.platform.workflow.model.WorkflowActionTemplateModel;
import de.hybris.platform.workflow.model.WorkflowTemplateModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.widgets.networkchart.context.NetworkChartContext;


/**
 * Extracts {@link WorkflowItem}s from the {@link WorkflowTemplateModel}
 */
public class WorkflowItemFromTemplateModelExtractor implements WorkflowItemExtractor
{
	private WorkflowItemModelFactory workflowItemModelFactory;

	@SafeVarargs // we only iterate over the elements
	private static <T> Stream<T> concatStreams(final Stream<T>... streams)
	{
		return Stream.of(streams) //
				.reduce(Stream::concat) //
				.orElseGet(Stream::empty);
	}

	@Override
	public Collection<WorkflowItem> extract(final NetworkChartContext context)
	{
		return context.getInitData(WorkflowTemplateModel.class) //
				.map(WorkflowTemplateModel::getActions) //
				.map(this::createNodes) //
				.orElseGet(ArrayList::new);
	}

	protected Collection<WorkflowItem> createNodes(final List<WorkflowActionTemplateModel> models)
	{
		final Stream<WorkflowItem> andLinksAsWorkflowItems = extractAndLinksAsWorkflowItems(models);
		final Stream<WorkflowItem> decisionsAsWorkflowItems = extractDecisionsAsWorkflowItems(models);
		final Stream<WorkflowItem> actionsAsWorkflowItems = extractActionsAsWorkflowItems(models);

		return concatStreams(andLinksAsWorkflowItems, decisionsAsWorkflowItems, actionsAsWorkflowItems) //
				.collect(Collectors.toList());
	}

	protected Stream<WorkflowItem> extractAndLinksAsWorkflowItems(final List<WorkflowActionTemplateModel> templates)
	{
		return templates.stream() //
				.flatMap(model -> model.getIncomingLinkTemplates().stream()) //
				.filter(WorkflowItemModelFactory::isAndConnectionTemplate) //
				.map(workflowItemModelFactory::create);
	}

	protected Stream<WorkflowItem> extractDecisionsAsWorkflowItems(final List<WorkflowActionTemplateModel> templates)
	{
		return templates.stream() //
				.map(WorkflowActionTemplateModel::getIncomingTemplateDecisions) //
				.flatMap(Collection::stream) //
				.map(workflowItemModelFactory::create);
	}

	protected Stream<WorkflowItem> extractActionsAsWorkflowItems(final List<WorkflowActionTemplateModel> templates)
	{
		return templates.stream() //
				.map(workflowItemModelFactory::create);
	}

	public WorkflowItemModelFactory getWorkflowItemModelFactory()
	{
		return workflowItemModelFactory;
	}

	@Required
	public void setWorkflowItemModelFactory(final WorkflowItemModelFactory workflowItemModelFactory)
	{
		this.workflowItemModelFactory = workflowItemModelFactory;
	}
}
