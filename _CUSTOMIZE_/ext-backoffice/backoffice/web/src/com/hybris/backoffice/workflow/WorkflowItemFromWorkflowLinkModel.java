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

import de.hybris.platform.core.model.link.LinkModel;

import java.util.Collection;

import com.google.common.collect.Lists;
import com.hybris.cockpitng.components.visjs.network.data.Node;

class WorkflowItemFromWorkflowLinkModel extends WorkflowItem
{
	private static final String AND_LABEL = "AND";
	private final LinkModel link;

	public WorkflowItemFromWorkflowLinkModel(final LinkModel link)
	{
		super(String.valueOf(link.getPk()), Type.AND_LINK, false);
		this.link = link;
	}

	@Override
	public Node createNode()
	{
		return new Node.Builder() //
				.withId(AND_LABEL + link.getTarget().getPk().toString()) //
				.withLabel(AND_LABEL) //
				.withLevel(getLevel()) //
				.withGroup(WorkflowItemModelFactory.PROPERTY_AND_CONNECTION) //
				.build();
	}

	@Override
	public Collection<String> getNeighborsIds()
	{
		return Lists.newArrayList(String.valueOf(link.getSource().getPk()));
	}

	@Override
	public boolean equals(final Object o) {
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
