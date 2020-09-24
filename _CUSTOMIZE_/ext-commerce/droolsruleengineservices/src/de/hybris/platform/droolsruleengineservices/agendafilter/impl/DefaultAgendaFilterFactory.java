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
package de.hybris.platform.droolsruleengineservices.agendafilter.impl;

import de.hybris.platform.droolsruleengineservices.agendafilter.AgendaFilterCreationStrategy;
import de.hybris.platform.droolsruleengineservices.agendafilter.AgendaFilterFactory;
import de.hybris.platform.droolsruleengineservices.agendafilter.CompoundAgendaFilter;
import de.hybris.platform.ruleengine.model.AbstractRuleEngineContextModel;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.kie.api.runtime.rule.AgendaFilter;


/**
 * The DefaultAgendaFilterFactory is the default implementation for {@link AgendaFilterFactory}. It uses
 * {@code AgendaFilterCreationStrategy}s to create agenda filters and a {@link CompoundAgendaFilter} to chain multiple
 * filters together.
 */
public class DefaultAgendaFilterFactory implements AgendaFilterFactory
{
	private Class<? extends CompoundAgendaFilter> targetClass;

	private List<AgendaFilterCreationStrategy> strategies;

	private boolean forceAllEvaluations = false;

	@Override
	public AgendaFilter createAgendaFilter(final AbstractRuleEngineContextModel context)
	{
		if (CollectionUtils.isEmpty(getStrategies()))
		{
			return null;
		}

		final List<AgendaFilter> agendaFilters = new ArrayList<>();
		for (final AgendaFilterCreationStrategy strategy : getStrategies())
		{
			agendaFilters.add(strategy.createAgendaFilter(context));
		}
		final CompoundAgendaFilter result = createFromClass();
		result.setAgendaFilters(agendaFilters);
		result.setForceAllEvaluations(isForceAllEvaluations());
		return result;
	}

	protected List<AgendaFilterCreationStrategy> getStrategies()
	{
		return strategies;
	}

	public void setStrategies(final List<AgendaFilterCreationStrategy> strategies)
	{
		this.strategies = strategies;
	}

	protected boolean isForceAllEvaluations()
	{
		return forceAllEvaluations;
	}

	public void setForceAllEvaluations(final boolean forceAllEvaluations)
	{
		this.forceAllEvaluations = forceAllEvaluations;
	}

	public void setTargetClass(final Class<? extends CompoundAgendaFilter> targetClass)
	{
		this.targetClass = targetClass;

		if (targetClass != null)
		{
			createFromClass();
		}
	}

	protected CompoundAgendaFilter createFromClass()
	{
		try
		{
			return targetClass.newInstance();
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			throw new IllegalArgumentException(
					"Cannot instantiate target class, it has no zero arguments constructor:" + targetClass.getSimpleName(), e);
		}
	}
}
