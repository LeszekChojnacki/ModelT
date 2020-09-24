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
package de.hybris.platform.ruleengineservices.maintenance;

import de.hybris.platform.servicelayer.event.ClusterAwareEvent;
import de.hybris.platform.servicelayer.event.PublishEventContext;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;


/**
 * The event querying about the rules compilation status
 */
public class RulesCompilationInProgressQueryEvent extends AbstractEvent implements ClusterAwareEvent
{

	private final String moduleName;

	public RulesCompilationInProgressQueryEvent(final String moduleName)
	{
		this.moduleName = moduleName;
	}

	public String getModuleName()
	{
		return moduleName;
	}

	@Override
	public boolean canPublish(final PublishEventContext publishEventContext)
	{
		return true;
	}

}
