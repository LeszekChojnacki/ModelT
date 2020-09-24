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

import de.hybris.platform.ruleengineservices.rule.strategies.RuleConverterException;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterTypeFormatter;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueConverter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;


public class DefaultRuleParameterValueConverter implements RuleParameterValueConverter, InitializingBean
{
	protected static final Pattern ENUM_PATTERN = Pattern.compile("^Enum\\((.*)\\)");
	protected static final Pattern LIST_PATTERN = Pattern.compile("^List\\((.*)\\)");
	protected static final Pattern MAP_PATTERN = Pattern.compile("^Map\\((.+),\\s*(.+)\\)");

	private Set<String> supportedTypes;
	private RuleParameterTypeFormatter ruleParameterTypeFormatter;
	private boolean debugMode = false;

	private ObjectReader objectReader;
	private ObjectWriter objectWriter;

	public Set<String> getSupportedTypes()
	{
		return supportedTypes;
	}

	@Required
	public void setSupportedTypes(final Set<String> supportedTypes)
	{
		this.supportedTypes = supportedTypes;
	}

	public RuleParameterTypeFormatter getRuleParameterTypeFormatter()
	{
		return ruleParameterTypeFormatter;
	}

	@Required
	public void setRuleParameterTypeFormatter(final RuleParameterTypeFormatter ruleParameterTypeFormatter)
	{
		this.ruleParameterTypeFormatter = ruleParameterTypeFormatter;
	}

	public boolean isDebugMode()
	{
		return debugMode;
	}

	public void setDebugMode(final boolean debugMode)
	{
		this.debugMode = debugMode;
	}


	protected ObjectReader getObjectReader()
	{
		return objectReader;
	}

	protected ObjectWriter getObjectWriter()
	{
		return objectWriter;
	}

	@Override
	public void afterPropertiesSet()
	{
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, debugMode);
		objectMapper.configure(SerializationFeature.INDENT_OUTPUT, debugMode);
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		objectMapper.setSerializationInclusion(Include.NON_NULL);

		final Map<Object, Object> attributes = new HashMap<>();

		configureObjectMapper(objectMapper);
		configureAttributes(attributes);

		objectReader = objectMapper.reader().withAttributes(attributes);
		objectWriter = objectMapper.writer().withAttributes(attributes);
	}

	@SuppressWarnings("unused")
	protected void configureObjectMapper(final ObjectMapper objectMapper)
	{
		// default implementation
	}

	@SuppressWarnings("unused")
	protected void configureAttributes(final Map<Object, Object> attributes)
	{
		// default implementation
	}

	@Override
	public String toString(final Object value)
	{
		try
		{
			return getObjectWriter().writeValueAsString(value);
		}
		catch (final IOException e)
		{
			throw new RuleConverterException(e);
		}
	}

	@Override
	public Object fromString(final String value, final String type)
	{
		if (StringUtils.isEmpty(value))
		{
			return null;
		}

		try
		{
			final ObjectReader objReader = getObjectReader();
			final JavaType javaType = resolveJavaType(objReader.getTypeFactory(), type);
			return objReader.forType(javaType).readValue(value);
		}
		catch (final Exception e)
		{
			throw new RuleConverterException(e);
		}
	}

	protected JavaType resolveJavaType(final TypeFactory typeFactory, final String type)
			throws ClassNotFoundException
	{
		if (StringUtils.isEmpty(type))
		{
			throw new RuleConverterException("Type cannot be null");
		}

		final String valueType = ruleParameterTypeFormatter.formatParameterType(type);

		if (supportedTypes.contains(valueType))
		{
			final Class<?> typeClass = getClassForType(valueType);
			return typeFactory.constructType(typeClass);
		}

		final Matcher enumMatcher = ENUM_PATTERN.matcher(valueType);
		if (enumMatcher.matches())
		{
			final Class<?> enumClass = getClassForType(enumMatcher.group(1));
			return typeFactory.constructType(enumClass);
		}

		final Matcher listMatcher = LIST_PATTERN.matcher(valueType);
		if (listMatcher.matches())
		{
			final Class<?> elementClass = getClassForType(listMatcher.group(1));
			return typeFactory.constructCollectionType(List.class, elementClass);
		}

		final Matcher mapMatcher = MAP_PATTERN.matcher(valueType);
		if (mapMatcher.matches())
		{
			final Class<?> keyClass = getClassForType(mapMatcher.group(1));
			final Class<?> valueClass = getClassForType(mapMatcher.group(2));
			return typeFactory.constructMapType(Map.class, keyClass, valueClass);
		}

		throw new RuleConverterException("Type " + type + " is not supported");
	}

	protected Class<?> getClassForType(final String type) throws ClassNotFoundException
	{
		return Class.forName(type, true, getClass().getClassLoader());
	}
}
