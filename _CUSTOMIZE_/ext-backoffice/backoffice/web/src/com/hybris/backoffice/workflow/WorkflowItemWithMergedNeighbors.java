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
import java.util.HashSet;
import java.util.Set;

import com.hybris.cockpitng.components.visjs.network.data.Node;

class WorkflowItemWithMergedNeighbors extends WorkflowItem
{
	private final WorkflowItem target;
	private final WorkflowItem source;

	public WorkflowItemWithMergedNeighbors(final WorkflowItem target, final WorkflowItem source)
	{
		super(target.getId(), target.getType(), target.isEnd());
		this.target = target;
		this.source = source;
	}

	@Override
	public Collection<String> getNeighborsIds()
	{
		final Set<String> neighborsIds = new HashSet<>(target.getNeighborsIds());
		neighborsIds.addAll(source.getNeighborsIds());
		return neighborsIds;
	}

	@Override
	public Node createNode()
	{
		return target.createNode();
	}

	@Override
	public Integer getLevel()
	{
		return target.getLevel();
	}

	@Override
	public void setLevel(final Integer level)
	{
		target.setLevel(level);
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
