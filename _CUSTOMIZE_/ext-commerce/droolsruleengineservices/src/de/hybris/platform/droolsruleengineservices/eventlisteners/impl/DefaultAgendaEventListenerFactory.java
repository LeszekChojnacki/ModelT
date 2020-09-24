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
package de.hybris.platform.droolsruleengineservices.eventlisteners.impl;

import de.hybris.platform.droolsruleengineservices.eventlisteners.AgendaEventListenerFactory;
import de.hybris.platform.ruleengine.impl.RuleMatchCountListener;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineContextModel;
import de.hybris.platform.ruleengine.model.DroolsRuleEngineContextModel;

import java.util.LinkedHashSet;
import java.util.Set;

import org.kie.api.event.rule.AgendaEventListener;

import com.google.common.base.Preconditions;


public class DefaultAgendaEventListenerFactory implements AgendaEventListenerFactory
{

	@Override
	public Set<AgendaEventListener> createAgendaEventListeners(final AbstractRuleEngineContextModel ctx)
	{
		Preconditions.checkArgument(ctx instanceof DroolsRuleEngineContextModel,
				"context must be of type de.hybris.platform.droolsruleengineservices.model.DroolsRuleEngineContextModel");

		final RuleMatchCountListener listener = createRuleMatchCountListener((DroolsRuleEngineContextModel) ctx);
		final Set<AgendaEventListener> listeners = new LinkedHashSet<>();
		if (listener != null)
		{
			listeners.add(listener);
		}
		return listeners;
	}

	/**
	 * creates a RuleMatchCountListener based on the {@code ctx.ruleFiringLimit} attribute.
	 *
	 * @param ctx
	 *           the context to use
	 * @return a new RuleMatchCountListener or null if the {@code ctx.ruleFiringLimit} attribute is null
	 */
	protected RuleMatchCountListener createRuleMatchCountListener(final DroolsRuleEngineContextModel ctx)
	{
		final Long firingLimit = ctx.getRuleFiringLimit();
		if (firingLimit != null)
		{
			final RuleMatchCountListener listener = new RuleMatchCountListener();
			listener.setExecutionLimit(firingLimit.longValue());
			return listener;
		}
		return null;
	}
}
