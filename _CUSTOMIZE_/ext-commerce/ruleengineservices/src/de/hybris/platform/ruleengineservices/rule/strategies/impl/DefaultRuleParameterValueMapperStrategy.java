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

import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueMapper;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueMapperException;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueMapperStrategy;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DefaultRuleParameterValueMapperStrategy
		implements RuleParameterValueMapperStrategy, ApplicationContextAware, InitializingBean
{
	protected static final Pattern LIST_PATTERN = Pattern.compile("^List\\((.*)\\)");
	protected static final Pattern MAP_PATTERN = Pattern.compile("^Map\\((.+),\\s*(.+)\\)");

	private Set<String> supportedTypes;
	private ApplicationContext applicationContext;

	private final Map<String, RuleParameterValueMapper> mappers = new HashMap<>();

	@Override
	public void afterPropertiesSet()
	{
		final Map<String, RuleParameterValueMapperDefinition> deprecatedBeans = getApplicationContext()
				.getBeansOfType(RuleParameterValueMapperDefinition.class);
		deprecatedBeans.values().forEach(definition -> mappers.put(definition.getType(), definition.getMapper()));

		final Map<String, RuleParameterValueTypeDefinition> beans = getApplicationContext()
				.getBeansOfType(RuleParameterValueTypeDefinition.class);
		beans.values().forEach(definition -> mappers.put(definition.getType(), definition.getMapper()));
	}

	@Override
	public Object toRuleParameter(final Object value, final String type)
	{
		if (value == null)
		{
			return null;
		}

		ServicesUtil.validateParameterNotNull(type, "Type parameter cannot be null");

		if (getSupportedTypes().contains(type) || (value instanceof Enum))
		{
			return value;
		}

		final RuleParameterValueMapper<Object> mapper = getMappers().get(type);
		if (mapper != null)
		{
			return mapper.toString(value);
		}

		if (value instanceof List)
		{
			return getAsList((List<Object>)value, type, this::toRuleParameter);
		}

		if (value instanceof Map)
		{
			return getAsMap((Map<Object, Object>)value, type, this::toRuleParameter);
		}

		throw new RuleParameterValueMapperException("Type is not supported: [type=" + type + "]");
	}

	protected Map<Object, Object> getAsMap(final Map<Object, Object> value, final String type,
			final BiFunction<Object, String, Object> valueSupplier)
	{
		final Matcher listMatcher = MAP_PATTERN.matcher(type);
		if (!listMatcher.matches())
		{
			throw new RuleParameterValueMapperException(
					"Value is instance of Map but type is not Map(.*,.*): [type=" + type + "]");
		}

		final String mapKeyType = listMatcher.group(1);
		final String mapValueType = listMatcher.group(2);
		final Map<Object, Object> newValue = new HashMap<>();

		for (final Entry<Object, Object> entry : value.entrySet())
		{
			if (entry.getKey() != null)
			{
				newValue.put(valueSupplier.apply(entry.getKey(), mapKeyType), valueSupplier.apply(entry.getValue(), mapValueType));
			}
		}

		return newValue;
	}

	protected List<Object> getAsList(final List<Object> value, final String type, final BiFunction<Object, String, Object> valueSupplier)
	{
		final Matcher listMatcher = LIST_PATTERN.matcher(type);
		if (!listMatcher.matches())
		{
			throw new RuleParameterValueMapperException(
					"Value is instance of List but type is not List(.*): [type=" + type + "]");
		}

		final String listElementType = listMatcher.group(1);
		final List<Object> newValue = new ArrayList<>();

		for (final Object listValue : value)
		{
			newValue.add(valueSupplier.apply(listValue, listElementType));
		}

		return newValue;
	}

	@Override
	public Object fromRuleParameter(final Object value, final String type)
	{
		if (value == null)
		{
			return null;
		}

		ServicesUtil.validateParameterNotNull(type, "Type parameter cannot be null");

		if (supportedTypes.contains(type) || (value instanceof Enum))
		{
			return value;
		}


		final RuleParameterValueMapper<Object> mapper = getMappers().get(type);
		if (mapper != null)
		{
			if (!(value instanceof String))
			{
				throw new RuleParameterValueMapperException(
						"Value is not instance of String: [class=" + value.getClass().getName() + ", type=" + type + "]");
			}

			return mapper.fromString((String) value);
		}

		if (value instanceof List)
		{
			return getAsList((List<Object>)value, type, this::fromRuleParameter);
		}

		if (value instanceof Map)
		{
			return getAsMap((Map<Object, Object>)value, type, this::fromRuleParameter);
		}

		throw new RuleParameterValueMapperException("Type is not supported: [type=" + type + "]");
	}

	protected Set<String> getSupportedTypes()
	{
		return supportedTypes;
	}

	@Required
	public void setSupportedTypes(final Set<String> supportedTypes)
	{
		this.supportedTypes = supportedTypes;
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

	protected Map<String, RuleParameterValueMapper> getMappers()
	{
		return mappers;
	}
}
