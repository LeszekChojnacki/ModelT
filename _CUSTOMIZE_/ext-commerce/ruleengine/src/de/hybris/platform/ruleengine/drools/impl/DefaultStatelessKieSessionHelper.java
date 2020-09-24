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
package de.hybris.platform.ruleengine.drools.impl;

import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import de.hybris.platform.ruleengine.RuleEvaluationContext;
import de.hybris.platform.ruleengine.RuleExecutionCountListener;
import de.hybris.platform.ruleengine.drools.StatelessKieSessionHelper;
import de.hybris.platform.ruleengine.enums.DroolsSessionType;
import de.hybris.platform.ruleengine.exception.RuleEngineRuntimeException;
import de.hybris.platform.ruleengine.model.DroolsRuleEngineContextModel;

import org.drools.core.event.DebugAgendaEventListener;
import org.drools.core.event.DebugRuleRuntimeEventListener;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link StatelessKieSessionHelper}
 * @deprecated since 6.6. Will be replaced by {@link de.hybris.platform.ruleengine.drools.KieSessionHelper}
 */
@Deprecated
public class DefaultStatelessKieSessionHelper extends DefaultModuleReleaseIdAware implements StatelessKieSessionHelper
{

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultStatelessKieSessionHelper.class);

	private Class<? extends RuleExecutionCountListener> ruleExecutionCounterClass;

	@Override
	public StatelessKieSession initializeSession(final RuleEvaluationContext context, final KieContainer kieContainer)
	{
		final DroolsRuleEngineContextModel ruleEngineContext = super.validateRuleEvaluationContext(context);
		final StatelessKieSession session = kieContainer.newStatelessKieSession(ruleEngineContext.getKieSession().getName());

		if (nonNull(context.getGlobals()))
		{
			context.getGlobals().forEach(session::setGlobal);
		}
		registerSessionListeners(context, session, ruleEngineContext.getRuleFiringLimit());
		return session;
	}

	/**
	 * 
	 * @deprecated since 6.6. Will be replaced by similar method in {@link de.hybris.platform.ruleengine.drools.KieSessionHelper}
	 */
	@Deprecated
	protected void registerSessionListeners(final RuleEvaluationContext context, final StatelessKieSession session,
			final Long maximumExecutions)
	{
		if (isNotEmpty(context.getEventListeners()))
		{
			for (final Object listener : context.getEventListeners())
			{
				if (listener instanceof AgendaEventListener)
				{
					session.addEventListener((AgendaEventListener) listener);
				}
				else if (listener instanceof RuleRuntimeEventListener)
				{
					session.addEventListener((RuleRuntimeEventListener) listener);
				}
				else if (listener instanceof ProcessEventListener)
				{
					session.addEventListener((ProcessEventListener) listener);
				}
				else
				{
					throw new IllegalArgumentException("context.eventListeners attribute must only contain instances of the types "
							+ "org.kie.api.event.rule.AgendaEventListener, org.kie.api.event.process.ProcessEventListener or "
							+ "org.kie.api.event.rule.RuleRuntimeEventListener");
				}
			}
		}
		if (nonNull(getRuleExecutionCounterClass()) && nonNull(maximumExecutions))
		{
			final RuleExecutionCountListener listener = createRuleExecutionCounterListener();
			listener.setExecutionLimit(maximumExecutions);
			session.addEventListener(listener);
		}
		if (LOGGER.isDebugEnabled())
		{
			session.addEventListener(new DebugRuleRuntimeEventListener());
			session.addEventListener(new DebugAgendaEventListener());
		}
	}

	/**
	 * Determine if the current RuleEngineContext's KieSession is Stateless.
	 *
	 * @param ruleEngineContext
	 * 		the rule engine context
	 * @return true if stateless, false otherwise
	 * @deprecated since 6.6
	 */
	@Deprecated
	protected boolean isSessionStateless(final DroolsRuleEngineContextModel ruleEngineContext)
	{
		return DroolsSessionType.STATELESS.equals(ruleEngineContext.getKieSession().getSessionType());
	}

	protected RuleExecutionCountListener createRuleExecutionCounterListener()
	{
		try
		{
			return getRuleExecutionCounterClass().newInstance();
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			throw new RuleEngineRuntimeException(e);
		}
	}

	protected Class<? extends RuleExecutionCountListener> getRuleExecutionCounterClass()
	{
		return ruleExecutionCounterClass;
	}

	@Required
	public void setRuleExecutionCounterClass(final Class<? extends RuleExecutionCountListener> ruleExecutionCounterClass)
	{
		this.ruleExecutionCounterClass = ruleExecutionCounterClass;
	}


}
