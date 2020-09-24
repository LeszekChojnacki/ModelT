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

import de.hybris.platform.workflow.model.WorkflowActionModel;
import de.hybris.platform.workflow.model.WorkflowModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.widgets.networkchart.context.NetworkChartContext;


/**
 * Extracts {@link WorkflowItem}s from the {@link WorkflowModel}
 */
public class WorkflowItemFromModelExtractor implements WorkflowItemExtractor
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
		return context.getInitData(WorkflowModel.class) //
				.map(WorkflowModel::getActions) //
				.map(this::createNodes) //
				.orElseGet(ArrayList::new);
	}

	protected Collection<WorkflowItem> createNodes(final List<WorkflowActionModel> models)
	{
		final Stream<WorkflowItem> andLinksAsWorkflowItems = extractAndLinksAsWorkflowItems(models);
		final Stream<WorkflowItem> decisionsAsWorkflowItems = extractDecisionsAsWorkflowItems(models);
		final Stream<WorkflowItem> actionsAsWorkflowItems = extractActionsAsWorkflowItems(models);

		return concatStreams(andLinksAsWorkflowItems, decisionsAsWorkflowItems, actionsAsWorkflowItems) //
				.collect(Collectors.toList());
	}

	protected Stream<WorkflowItem> extractAndLinksAsWorkflowItems(final List<WorkflowActionModel> models)
	{
		return models.stream() //
				.flatMap(model -> model.getIncomingLinks().stream()) //
				.filter(WorkflowItemModelFactory::isAndConnection) //
				.map(workflowItemModelFactory::create);
	}

	protected Stream<WorkflowItem> extractDecisionsAsWorkflowItems(final List<WorkflowActionModel> models)
	{
		return models.stream() //
				.map(WorkflowActionModel::getIncomingDecisions) //
				.flatMap(Collection::stream) //
				.map(workflowItemModelFactory::create);
	}

	protected Stream<WorkflowItem> extractActionsAsWorkflowItems(final List<WorkflowActionModel> models)
	{
		return models.stream() //
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

	@Override
	public boolean equals(final Object o)
	{
		return super.equals(o);
	}

	@Override
	public int hashCode()
	{
		return super.hashCode();
	}
}
