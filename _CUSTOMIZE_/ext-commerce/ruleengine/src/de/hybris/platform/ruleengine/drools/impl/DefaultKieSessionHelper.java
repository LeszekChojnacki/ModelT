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

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import de.hybris.platform.ruleengine.RuleEvaluationContext;
import de.hybris.platform.ruleengine.RuleExecutionCountListener;
import de.hybris.platform.ruleengine.drools.KieSessionHelper;
import de.hybris.platform.ruleengine.enums.DroolsSessionType;
import de.hybris.platform.ruleengine.exception.RuleEngineRuntimeException;
import de.hybris.platform.ruleengine.model.DroolsKIESessionModel;
import de.hybris.platform.ruleengine.model.DroolsRuleEngineContextModel;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;

import org.drools.core.event.DebugAgendaEventListener;
import org.drools.core.event.DebugRuleRuntimeEventListener;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionsPool;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * Default implementation of {@link KieSessionHelper}
 */
public class DefaultKieSessionHelper<T> extends DefaultModuleReleaseIdAware implements KieSessionHelper<T>
{

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultKieSessionHelper.class);

	private Class<? extends RuleExecutionCountListener> ruleExecutionCounterClass;

	private final Map<KieContainer, KieSessionsPool> kieContainerSessionsPools = new ConcurrentHashMap<>();

	private int kieSessionPoolInitialCapacity;

	boolean keepOnlyOneSessionPoolVersion = true;

	boolean kieSessionPoolEnabled = true;


	@Override
	public T initializeSession(final Class<T> kieSessionClass, final RuleEvaluationContext context,
			final KieContainer kieContainer)
	{
		assertKieSessionClass(kieSessionClass);
		final DroolsRuleEngineContextModel ruleEngineContext = validateRuleEvaluationContext(context);

		return KieSession.class.isAssignableFrom(kieSessionClass) ?
				(T) initializeKieSessionInternal(context, ruleEngineContext, kieContainer) :
				(T) initializeStatelessKieSessionInternal(context, ruleEngineContext, kieContainer);
	}

	protected KieSession initializeKieSessionInternal(final RuleEvaluationContext context,
			final DroolsRuleEngineContextModel ruleEngineContext, final KieContainer kieContainer)
	{
		final DroolsKIESessionModel kieSession = ruleEngineContext.getKieSession();
		assertSessionIsStateful(kieSession);
		final KieSession session = isKieSessionPoolEnabled() ? getKieContainerSessionsPool(kieContainer, false).newKieSession()
				: kieContainer.newKieSession(kieSession.getName());

		if (nonNull(context.getGlobals()))
		{
			context.getGlobals().forEach(session::setGlobal);
		}
		registerKieSessionListeners(context, session, ruleEngineContext.getRuleFiringLimit());
		return session;
	}

	protected StatelessKieSession initializeStatelessKieSessionInternal(final RuleEvaluationContext context,
			final DroolsRuleEngineContextModel ruleEngineContext, final KieContainer kieContainer)
	{
		final DroolsKIESessionModel kieSession = ruleEngineContext.getKieSession();
		assertSessionIsStateless(kieSession);
		final StatelessKieSession session = isKieSessionPoolEnabled()
				? getKieContainerSessionsPool(kieContainer, true).newStatelessKieSession()
				: kieContainer.newStatelessKieSession(kieSession.getName());

		if (nonNull(context.getGlobals()))
		{
			context.getGlobals().forEach(session::setGlobal);
		}
		registerStatelessKieSessionListeners(context, session, ruleEngineContext.getRuleFiringLimit());
		return session;
	}

	protected void assertKieSessionClass(final Class<T> kieSessionClass)
	{
		checkArgument(
				KieSession.class.isAssignableFrom(kieSessionClass) || StatelessKieSession.class.isAssignableFrom(kieSessionClass),
				"No other session types other than KieSession and StatelessKieSession are supported");
	}

	protected void assertSessionIsStateless(final DroolsKIESessionModel kieSession)
	{
		checkArgument(kieSession.getSessionType().equals(DroolsSessionType.STATELESS),
				"Expected STATELESS session type here. Check the invocation parameters");
	}

	protected void assertSessionIsStateful(final DroolsKIESessionModel kieSession)
	{
		checkArgument(kieSession.getSessionType().equals(DroolsSessionType.STATEFUL),
				"Expected STATEFUL session type here. Check the invocation parameters");
	}

	protected void registerKieSessionListeners(final RuleEvaluationContext context, final KieSession session,
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

	protected void registerStatelessKieSessionListeners(final RuleEvaluationContext context, final StatelessKieSession session,
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

	@PreDestroy
	protected void teardownSessionsPools()
	{
		getKieSessionsPools().values().forEach(KieSessionsPool::shutdown);
	}

	protected KieSessionsPool getKieContainerSessionsPool(final KieContainer kieContainer, final boolean stateless)
	{
		final KieSessionsPool result = getKieSessionsPools().computeIfAbsent(kieContainer, container -> {
			return stateless ? container.getKieBase().newKieSessionsPool(getKieSessionPoolInitialCapacity())
					: container.newKieSessionsPool(getKieSessionPoolInitialCapacity());
		});
		return result;
	}

	/**
	 * removes all old kieContainers (same artifactId but other versions) after successful swapping
	 */
	@Override
	public void shutdownKieSessionPools(final String moduleName, final String version)
	{
		if (!isKeepOnlyOneSessionPoolVersion())
		{
			return;
		}

		LOGGER.debug("removing old kie session pools for kie container with artifactId:{} version:{}", moduleName, version);
		final boolean removed = false;
		for (final Iterator it = getKieSessionsPools().entrySet().iterator(); it.hasNext();)
		{
			final Entry<KieContainer, KieSessionsPool> e = (Entry<KieContainer, KieSessionsPool>) it.next();
			final KieContainer c = e.getKey();
			final boolean match = c.getReleaseId().getArtifactId().equals(moduleName)
					&& !c.getReleaseId().getVersion().equals(version);
			if (match)
			{
				final KieSessionsPool pool = e.getValue();
				// remove and shutdown the session pool
				it.remove();
				pool.shutdown();
			}
		}
		if (removed)
		{
			LOGGER.debug("removal of old kie session pools successful");
		}
	}

	protected Class<? extends RuleExecutionCountListener> getRuleExecutionCounterClass()
	{
		return ruleExecutionCounterClass;
	}

	protected Map<KieContainer, KieSessionsPool> getKieSessionsPools()
	{
		return kieContainerSessionsPools;
	}

	@Required
	public void setRuleExecutionCounterClass(final Class<? extends RuleExecutionCountListener> ruleExecutionCounterClass)
	{
		this.ruleExecutionCounterClass = ruleExecutionCounterClass;
	}

	protected int getKieSessionPoolInitialCapacity()
	{
		return kieSessionPoolInitialCapacity;
	}

	@Required
	public void setKieSessionPoolInitialCapacity(final int kieSessionPoolInitialCapacity)
	{
		this.kieSessionPoolInitialCapacity = kieSessionPoolInitialCapacity;
	}

	protected boolean isKeepOnlyOneSessionPoolVersion()
	{
		return keepOnlyOneSessionPoolVersion;
	}

	@Required
	public void setKeepOnlyOneSessionPoolVersion(final boolean keepOnlyOneSessionPoolVersion)
	{
		this.keepOnlyOneSessionPoolVersion = keepOnlyOneSessionPoolVersion;
	}

	protected boolean isKieSessionPoolEnabled()
	{
		return kieSessionPoolEnabled;
	}

	@Required
	public void setKieSessionPoolEnabled(final boolean kieSessionPoolEnabled)
	{
		this.kieSessionPoolEnabled = kieSessionPoolEnabled;
	}

}
