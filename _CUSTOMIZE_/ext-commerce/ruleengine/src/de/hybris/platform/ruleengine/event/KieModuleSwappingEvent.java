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
package de.hybris.platform.ruleengine.event;

import de.hybris.platform.servicelayer.event.ClusterAwareEvent;
import de.hybris.platform.servicelayer.event.PublishEventContext;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;

import java.util.Objects;


/**
 * The Event is fired on Rule Engine initialization to propagate the update to other nodes.
 *
 */
public class KieModuleSwappingEvent extends AbstractEvent implements ClusterAwareEvent
{
	private final String rulesModuleName;
	private final String deployedReleaseIdVersion;

	public KieModuleSwappingEvent(final String rulesModuleName, final String deployedReleaseIdVersion)
	{
		this.rulesModuleName = rulesModuleName;
		this.deployedReleaseIdVersion = deployedReleaseIdVersion;
	}

	@Override
	public boolean canPublish(final PublishEventContext publishEventContext)
	{
		Objects.requireNonNull(publishEventContext, "publishEventContext is required");
		return publishEventContext.getSourceNodeId() != publishEventContext.getTargetNodeId();
	}

	public String getRulesModuleName()
	{
		return rulesModuleName;
	}

	public String getDeployedReleaseIdVersion()
	{
		return deployedReleaseIdVersion;
	}
}
