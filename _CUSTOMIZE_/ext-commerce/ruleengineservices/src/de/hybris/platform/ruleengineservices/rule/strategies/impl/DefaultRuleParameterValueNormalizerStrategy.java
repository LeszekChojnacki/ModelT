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
package de.hybris.platform.ruleengineservices.rule.strategies.impl;

import static java.util.Objects.nonNull;

import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueNormalizer;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueNormalizerStrategy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.Maps;


/**
 * Default implementation of {@link RuleParameterValueNormalizerStrategy}
 */
public class DefaultRuleParameterValueNormalizerStrategy implements RuleParameterValueNormalizerStrategy, ApplicationContextAware,
		InitializingBean
{
	private final Map<String, RuleParameterValueNormalizer> valueNormalizers = new HashMap<>();

	private ApplicationContext applicationContext;

	@Override
	public Object normalize(final Object value, final String type)
	{
		Object result = value;

		final Collection<RuleParameterValueNormalizer> normalizers = Maps.filterKeys(getValueNormalizers(), k -> type.contains(k))
				.values();

		if (!normalizers.isEmpty())
		{
			for (final RuleParameterValueNormalizer n : normalizers)
			{
				result = n.normalize(result);
			}
		}

		return result;
	}

	@Override
	public void afterPropertiesSet()
	{
		final Map<String, RuleParameterValueTypeDefinition> beans = getApplicationContext()
				.getBeansOfType(RuleParameterValueTypeDefinition.class);

		beans.entrySet().stream().filter(e -> nonNull(e.getValue().getNormalizer())).map(e -> e.getValue())
				.forEach(definition -> valueNormalizers.put(definition.getType(), definition.getNormalizer()));
	}

	protected Map<String, RuleParameterValueNormalizer> getValueNormalizers()
	{
		return valueNormalizers;
	}

	protected ApplicationContext getApplicationContext()
	{
		return applicationContext;
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext)
	{
		this.applicationContext = applicationContext;
	}
}
