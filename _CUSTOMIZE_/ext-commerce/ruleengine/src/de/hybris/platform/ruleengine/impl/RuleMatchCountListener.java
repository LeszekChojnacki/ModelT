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
package de.hybris.platform.ruleengine.impl;

import de.hybris.platform.ruleengine.RuleExecutionCountListener;
import de.hybris.platform.ruleengine.exception.DroolsRuleLoopException;

import org.drools.core.event.DefaultAgendaEventListener;
import org.kie.api.definition.rule.Rule;
import org.kie.api.event.rule.AfterMatchFiredEvent;

import com.google.common.util.concurrent.AtomicLongMap;


/**
 * RuleMatchCountListener counts the number (per rule) of match-fired events and throws a
 * {@link DroolsRuleLoopException} if the given threshold is exceeded, thereby preventing a potentially infinite loop.
 */
public class RuleMatchCountListener extends DefaultAgendaEventListener implements RuleExecutionCountListener
{
	private final AtomicLongMap<Rule> map;
	private long executionLimit;

	public RuleMatchCountListener()
	{
		map = AtomicLongMap.create();
		executionLimit = 0;
	}

	@Override
	public void afterMatchFired(final AfterMatchFiredEvent event)
	{
		final long currentCount = map.addAndGet(event.getMatch().getRule(), 1L);
		if (currentCount > executionLimit)
		{
			throw new DroolsRuleLoopException(executionLimit, map.asMap());
		}
	}

	@Override
	public void setExecutionLimit(final long max)
	{
		this.executionLimit = max;
	}

	protected long getExecutionLimit()
	{
		return executionLimit;
	}

}
