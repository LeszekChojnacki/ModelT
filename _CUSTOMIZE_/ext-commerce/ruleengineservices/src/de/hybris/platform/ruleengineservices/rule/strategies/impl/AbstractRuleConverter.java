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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.hybris.platform.ruleengineservices.rule.data.AbstractRuleData;
import de.hybris.platform.ruleengineservices.rule.data.AbstractRuleDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterDefinitionData;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleConverterException;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterUuidGenerator;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueConverter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public abstract class AbstractRuleConverter implements InitializingBean
{
	protected static final String RULE_PARAMETER_VALUE_CONVERTER_KEY = "ruleParameterValueConverter";

	private RuleParameterValueConverter ruleParameterValueConverter;
	private RuleParameterUuidGenerator ruleParameterUuidGenerator;

	private boolean debugMode = false;

	private ObjectReader objectReader;
	private ObjectWriter objectWriter;

	@Override
	public void afterPropertiesSet()
	{
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, debugMode);
		objectMapper.configure(SerializationFeature.INDENT_OUTPUT, false);
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		objectMapper.addMixIn(RuleParameterData.class, RuleParameterDataMixIn.class);

		final Map<Object, Object> attributes = new HashMap<>();
		attributes.put(RULE_PARAMETER_VALUE_CONVERTER_KEY, ruleParameterValueConverter);

		configureObjectMapper(objectMapper);
		configureAttributes(attributes);

		objectReader = objectMapper.reader().withAttributes(attributes);
		objectWriter = objectMapper.writer().withAttributes(attributes);
	}

	protected void convertParameters(final AbstractRuleData ruleData, final AbstractRuleDefinitionData ruleDefinitionData)
	{
		for (final Map.Entry<String, RuleParameterDefinitionData> entry : ruleDefinitionData.getParameters().entrySet())
		{
			final String parameterId = entry.getKey();
			final RuleParameterDefinitionData parameterDefinition = entry.getValue();

			RuleParameterData parameter = ruleData.getParameters().get(parameterId);
			if (parameter == null)
			{
				parameter = new RuleParameterData();
				parameter.setValue(parameterDefinition.getDefaultValue());
				ruleData.getParameters().put(parameterId, parameter);
			}
			else
			{
				final Object value = getRuleParameterValueConverter().fromString((String) parameter.getValue(),
						parameterDefinition.getType());

				parameter.setValue(value);
			}

			parameter.setType(parameterDefinition.getType());

			if (StringUtils.isBlank(parameter.getUuid()))
			{
				parameter.setUuid(getRuleParameterUuidGenerator().generateUuid(parameter, parameterDefinition));
			}
		}
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

	protected RuleParameterValueConverter getRuleParameterValueConverter()
	{
		return ruleParameterValueConverter;
	}

	@Required
	public void setRuleParameterValueConverter(final RuleParameterValueConverter ruleParameterValueConverter)
	{
		this.ruleParameterValueConverter = ruleParameterValueConverter;
	}

	protected RuleParameterUuidGenerator getRuleParameterUuidGenerator()
	{
		return ruleParameterUuidGenerator;
	}

	@Required
	public void setRuleParameterUuidGenerator(final RuleParameterUuidGenerator ruleParameterUuidGenerator)
	{
		this.ruleParameterUuidGenerator = ruleParameterUuidGenerator;
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

	protected static class RuleParameterValueSerializer extends JsonSerializer<Object>
	{
		@Override
		public void serialize(final Object value, final JsonGenerator generator, final SerializerProvider provider)
				throws IOException
		{
			try
			{
				final RuleParameterValueConverter ruleParameterValueConverter = (RuleParameterValueConverter) provider
						.getAttribute(RULE_PARAMETER_VALUE_CONVERTER_KEY);
				final String parameterValue = ruleParameterValueConverter.toString(value);
				generator.writeRawValue(parameterValue);
			}
			catch (final RuleConverterException e)
			{
				throw new JsonGenerationException(e);
			}
		}
	}

	protected static class RuleParameterValueDeserializer extends JsonDeserializer<Object>
	{
		@Override
		public Object deserialize(final JsonParser parser, final DeserializationContext context) throws IOException
		{
			final TreeNode tree = parser.getCodec().readTree(parser);
			return tree.toString();
		}
	}

	protected static class RuleParameterDataMixIn
	{
		@JsonSerialize(using = RuleParameterValueSerializer.class)
		@JsonDeserialize(using = RuleParameterValueDeserializer.class)
		@SuppressWarnings("PMD")
		public Object value;
	}
	
}
