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

import java.util.Collection;

import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.widgets.networkchart.context.NetworkChartContext;
import com.hybris.backoffice.widgets.networkchart.handler.NetworkPopulator;
import com.hybris.cockpitng.components.visjs.network.data.Network;
import com.hybris.cockpitng.components.visjs.network.response.NetworkUpdates;


/**
 * Populates the Workflow diagram with data based on {@link NetworkChartContext}
 */
public class WorkflowPopulator implements NetworkPopulator
{
	private WorkflowNetworkFactory workflowNetworkFactory;
	private WorkflowItemExtractor workflowItemExtractor;

	@Override
	public Network populate(final NetworkChartContext context)
	{
		final Collection<WorkflowItem> items = workflowItemExtractor.extract(context);
		return workflowNetworkFactory.create(items);
	}

	@Override
	public NetworkUpdates update(final Object updatedObject, final NetworkChartContext context)
	{
		return NetworkUpdates.EMPTY;
	}

	public WorkflowNetworkFactory getWorkflowNetworkFactory()
	{
		return workflowNetworkFactory;
	}

	@Required
	public void setWorkflowNetworkFactory(final WorkflowNetworkFactory workflowNetworkFactory)
	{
		this.workflowNetworkFactory = workflowNetworkFactory;
	}

	public WorkflowItemExtractor getWorkflowItemExtractor()
	{
		return workflowItemExtractor;
	}

	@Required
	public void setWorkflowItemExtractor(final WorkflowItemExtractor workflowItemExtractor)
	{
		this.workflowItemExtractor = workflowItemExtractor;
	}
}
