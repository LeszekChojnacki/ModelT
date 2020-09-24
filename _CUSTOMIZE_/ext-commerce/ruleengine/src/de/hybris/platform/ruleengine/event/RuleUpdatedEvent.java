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

import de.hybris.platform.servicelayer.event.events.AbstractEvent;


/**
 * The Event is fired when a Rule gets updated
 */
public class RuleUpdatedEvent extends AbstractEvent
{
	private final String ruleCode;

	public RuleUpdatedEvent(final String ruleCode)
	{
		this.ruleCode = ruleCode;
	}

	@Override
	public String toString()
	{
		return "RuleUpdatedEvent{" +
				"ruleCode='" + ruleCode + "\'}";
	}

	public String getRuleCode()
	{
		return ruleCode;
	}
}
