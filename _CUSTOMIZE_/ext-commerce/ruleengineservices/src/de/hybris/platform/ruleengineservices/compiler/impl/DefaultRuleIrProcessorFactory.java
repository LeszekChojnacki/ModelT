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
package de.hybris.platform.ruleengineservices.compiler.impl;

import de.hybris.platform.ruleengineservices.compiler.RuleIrProcessor;
import de.hybris.platform.ruleengineservices.compiler.RuleIrProcessorFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * Default implementation of {@link RuleIrProcessorFactory}.
 */
public class DefaultRuleIrProcessorFactory implements RuleIrProcessorFactory, ApplicationContextAware
{
	private List<RuleIrProcessor> ruleIrProcessors;
	private ApplicationContext applicationContext;

	@Override
	public List<RuleIrProcessor> getRuleIrProcessors()
	{
		return Collections.unmodifiableList(ruleIrProcessors);
	}

	@Override
	public void setApplicationContext(final ApplicationContext ctx)
	{
		this.applicationContext = ctx;
		this.ruleIrProcessors = loadRuleIrProcessors();
	}

	protected List<RuleIrProcessor> loadRuleIrProcessors()
	{
		final List<RuleIrProcessor> ruleIrProcessorList = new ArrayList<>();
		final Map<String, RuleIrProcessorDefinition> processorsDefinitionsMap = applicationContext
				.getBeansOfType(RuleIrProcessorDefinition.class);
		final List<RuleIrProcessorDefinition> processorDefinitions = new ArrayList<>(
				processorsDefinitionsMap.values());

		Collections.sort(processorDefinitions);
		processorDefinitions.forEach(definition -> ruleIrProcessorList.add(definition.getRuleIrProcessor()));

		return ruleIrProcessorList;
	}
}
