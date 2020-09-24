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

import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.ruleengineservices.definitions.RuleParameterEnum;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleMessageFormatStrategy;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleMessageParameterDecorator;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueNormalizerStrategy;
import de.hybris.platform.servicelayer.i18n.L10NService;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.Nonnull;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.ChoiceFormat;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.text.DateFormat.getDateInstance;
import static java.text.DateFormat.getTimeInstance;


public class DefaultRuleMessageFormatStrategy implements RuleMessageFormatStrategy
{
	protected static final String UNKNOWN_PARAMETER = "?";

	protected static final String DEFAULT_FORMAT_STYLE = "default";
	@SuppressWarnings("findsecbugs:REDOS")
	protected static final Pattern PARAMETER_PATTERN = Pattern.compile("(\\{([^,\\}]*)(,([^,\\}]*))?(,([^,\\}]*))?\\})");

	protected static final Pattern LIST_PATTERN = Pattern.compile("^List\\((.*)\\)");
	protected static final Pattern MAP_PATTERN = Pattern.compile("^Map\\((.+),\\s*(.+)\\)");

	private L10NService l10NService;
	private EnumerationService enumerationService;
	private RuleParameterValueNormalizerStrategy ruleParameterValueNormalizerStrategy;

	@Override
	public String format(final String message, final Map<String, RuleParameterData> parameters, final Locale locale)
	{
		return format(message, parameters, locale, null);
	}

	@Override
	public String format(final String message, final Map<String, RuleParameterData> parameters, final Locale locale,
			final RuleMessageParameterDecorator parameterDecorator)
	{
		final StringBuffer fmtMessage = new StringBuffer();  // NOSONAR
		final List<RuleMessageParameterData> fmtMessageParameters = new ArrayList<>();

		int matcherIndex = 0;
		final Matcher matcher = PARAMETER_PATTERN.matcher(message);

		while (matcher.find())
		{
			final String variable = matcher.group(2);
			String replacement = null;

			final RuleParameterData parameter = parameters.get(variable);
			if (parameter != null)
			{
				final StringBuilder newVariable = new StringBuilder();
				newVariable.append('{');
				newVariable.append(matcherIndex);
				newVariable.append('}');

				Object value = resolveValue(parameter, locale);
				Format format = null;

				if (value == null)
				{
					value = sanitizeValue(parameter);
				}
				else
				{
					format = resolveFormat(matcher.group(4), matcher.group(6), locale);
				}

				if (parameterDecorator != null)
				{
					format = new DecoratorFormat(format, parameter, parameterDecorator);
				}

				final RuleMessageParameterData fmtMessageParameter = new RuleMessageParameterData();
				fmtMessageParameter.setValue(value);
				fmtMessageParameter.setFormat(format);

				fmtMessageParameters.add(fmtMessageParameter);
				replacement = newVariable.toString();

				matcherIndex++;
			}
			else
			{
				replacement = UNKNOWN_PARAMETER;
			}

			matcher.appendReplacement(fmtMessage, Matcher.quoteReplacement(replacement));
		}

		matcher.appendTail(fmtMessage);

		final int size = fmtMessageParameters.size();
		final Object[] messageParameters = new Object[size];
		final Format[] messageFormats = new Format[size];

		int index = 0;
		for (final RuleMessageParameterData fmtMessageParameter : fmtMessageParameters)
		{
			messageParameters[index] = fmtMessageParameter.getValue();
			messageFormats[index] = fmtMessageParameter.getFormat();
			index++;
		}

		final MessageFormat messageFormat = new MessageFormat(fmtMessage.toString(), locale);
		messageFormat.setFormats(messageFormats);
		return messageFormat.format(messageParameters);
	}

	protected Object resolveValue(final RuleParameterData parameter, final Locale locale)
	{
		final Object value = getRuleParameterValueNormalizerStrategy().normalize(parameter.getValue(), parameter.getType());

		if (value == null)
		{
			return null;
		}

		if (value instanceof RuleParameterEnum)
		{
			final String localizationKey = value.getClass().getName() + "." + value + ".name";
			return l10NService.getLocalizedString(localizationKey.toLowerCase(Locale.ENGLISH));
		}
		else if (value instanceof HybrisEnumValue)
		{
			return enumerationService.getEnumerationName((HybrisEnumValue) value, locale);
		}

		return value;
	}

	protected Object sanitizeValue(final RuleParameterData parameter)
	{
		final Matcher listMatcher = LIST_PATTERN.matcher(parameter.getType());
		if (listMatcher.matches())
		{
			return Collections.emptyList();
		}

		final Matcher mapMatcher = MAP_PATTERN.matcher(parameter.getType());
		if (mapMatcher.matches())
		{
			return Collections.emptyMap();
		}

		return UNKNOWN_PARAMETER;
	}

	protected Format resolveFormat(final String name, final String arguments, final Locale locale)
	{
		if (name == null)
		{
			return null;
		}

		Format format = null;

		switch (name)
		{
			case "number":
				format = createNumberFormat(arguments, locale);
				break;
			case "date":
				format = createDateFormat(arguments, locale);
				break;
			case "time":
				format = createTimeFormat(arguments, locale);
				break;
			case "choice":
				format = createChoiceFormat(arguments);
				break;
			default:
				throw new IllegalArgumentException("unknown format type: " + name);
		}

		return format;
	}

	protected NumberFormat createNumberFormat(final String arguments, final Locale locale)
	{
		NumberFormat format;
		String formatStyle;
		String formatMultiplier;

		if (arguments == null)
		{
			formatStyle = DEFAULT_FORMAT_STYLE;
			formatMultiplier = null;
		}
		else
		{
			final int separatorIndex = arguments.indexOf('*');

			if (separatorIndex >= 0)
			{
				formatStyle = arguments.substring(0, separatorIndex);
				formatMultiplier = arguments.substring(separatorIndex + 1, arguments.length());
			}
			else
			{
				formatStyle = arguments;
				formatMultiplier = null;
			}
		}

		switch (formatStyle)
		{
			case DEFAULT_FORMAT_STYLE:
				format = NumberFormat.getInstance(locale);
				break;
			case "currency":
				format = NumberFormat.getCurrencyInstance(locale);
				break;
			case "percent":
				format = NumberFormat.getPercentInstance(locale);
				break;
			case "integer":
				format = NumberFormat.getIntegerInstance(locale);
				break;
			default:
				format = new DecimalFormat(formatStyle, DecimalFormatSymbols.getInstance(locale));
				break;
		}

		if (formatMultiplier != null && format instanceof DecimalFormat)
		{
			final int multiplier = Integer.parseInt(formatMultiplier);
			((DecimalFormat) format).setMultiplier(multiplier);
		}

		return format;
	}

	protected DateFormat createDateFormat(final String arguments, final Locale locale)
	{
		final String formatStyle = arguments == null ? DEFAULT_FORMAT_STYLE : arguments;

		DateFormat dateFormat = new SimpleDateFormat(formatStyle, locale);
		final int style = decodeDateFormatStyle(formatStyle);
		if (style > -1)
		{
			dateFormat = getDateInstance(style, locale);
		}
		return dateFormat;
	}

	protected DateFormat createTimeFormat(final String arguments, final Locale locale)
	{
		final String formatStyle = arguments == null ? DEFAULT_FORMAT_STYLE : arguments;

		DateFormat dateFormat = new SimpleDateFormat(formatStyle, locale);
		final int style = decodeDateFormatStyle(formatStyle);
		if (style > -1)
		{
			dateFormat = getTimeInstance(style, locale);
		}
		return dateFormat;

	}

	protected int decodeDateFormatStyle(final String formatStyle)
	{
		int style = -1;
		if (DEFAULT_FORMAT_STYLE.equals(formatStyle))
		{
			style = DateFormat.DEFAULT;
		}
		else if ("short".equals(formatStyle))
		{
			style = DateFormat.SHORT;
		}
		else if ("medium".equals(formatStyle))
		{
			style = DateFormat.MEDIUM;
		}
		else if ("long".equals(formatStyle))
		{
			style = DateFormat.LONG;
		}
		else if ("full".equals(formatStyle))
		{
			style = DateFormat.FULL;
		}
		return style;
	}

	protected NumberFormat createChoiceFormat(final String arguments)
	{
		try
		{
			return new ChoiceFormat(arguments);
		}
		catch (final Exception e)
		{
			throw new IllegalArgumentException("incorrect choice pattern", e);
		}
	}

	@Required
	public L10NService getL10NService()
	{
		return l10NService;
	}

	public void setL10NService(final L10NService l10NService)
	{
		this.l10NService = l10NService;
	}

	public EnumerationService getEnumerationService()
	{
		return enumerationService;
	}

	@Required
	public void setEnumerationService(final EnumerationService enumerationService)
	{
		this.enumerationService = enumerationService;
	}

	protected RuleParameterValueNormalizerStrategy getRuleParameterValueNormalizerStrategy()
	{
		return ruleParameterValueNormalizerStrategy;
	}

	@Required
	public void setRuleParameterValueNormalizerStrategy(
			final RuleParameterValueNormalizerStrategy ruleParameterValueNormalizerStrategy)
	{
		this.ruleParameterValueNormalizerStrategy = ruleParameterValueNormalizerStrategy;
	}

	protected static class RuleMessageParameterData
	{
		private Object value;
		private Format format;

		public Object getValue()
		{
			return value;
		}

		public void setValue(final Object value)
		{
			this.value = value;
		}

		public Format getFormat()
		{
			return format;
		}

		public void setFormat(final Format format)
		{
			this.format = format;
		}
	}

	protected static class DecoratorFormat extends java.text.Format
	{
		private final Format delegate;
		private final RuleParameterData parameter;
		private final RuleMessageParameterDecorator decorator;

		public DecoratorFormat(final Format delegate, final RuleParameterData parameter,
				final RuleMessageParameterDecorator decorator)
		{
			this.delegate = delegate;
			this.parameter = parameter;
			this.decorator = decorator;
		}

		protected Format getDelegate()
		{
			return delegate;
		}

		public RuleParameterData getParameter()
		{
			return parameter;
		}

		protected RuleMessageParameterDecorator getDecorator()
		{
			return decorator;
		}

		@Override
		public StringBuffer format(final Object object, @Nonnull final StringBuffer buffer, @Nonnull final FieldPosition position)
		{
			Object formattedObject = object;
			if (delegate != null)
			{
				formattedObject = delegate.format(object, new StringBuffer(), position);
			}

			buffer.append(decorator.decorate(formattedObject.toString(), parameter));
			return buffer;
		}

		@Override
		public AttributedCharacterIterator formatToCharacterIterator(@Nonnull final Object object)
		{
			final String string = super.format(object);
			final AttributedString attributedString = new AttributedString(string);
			return attributedString.getIterator();
		}

		@Override
		public Object parseObject(final String source, @Nonnull final ParsePosition position)
		{
			if (delegate == null)
			{
				return source;
			}

			return delegate.parseObject(source, position);
		}
	}
}
