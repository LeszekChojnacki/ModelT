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

import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleMessageFormatStrategy;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleMessageParameterDecorator;
import de.hybris.platform.security.XssEncodeService;

import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;

public class AbstractRuleBreadcrumbsBuilder
{
	protected static final String PARAMETER_CLASS = "rule-parameter";
	protected static final String SEPARATOR_CLASS = "rule-separator";

	private RuleMessageFormatStrategy ruleMessageFormatStrategy;
	private XssEncodeService xssEncodeService;

	protected String formatBreadcrumb(final String breadcrumb, final Map<String, RuleParameterData> parameters,
			final Locale locale, final boolean styled, final boolean decorated)
	{
		if (StringUtils.isBlank(breadcrumb))
		{
			return StringUtils.EMPTY;
		}

		final RuleMessageParameterDecorator parameterDecorator = (formattedValue, parameter) -> {
			if (styled)
			{
				final String escapedValue = getXssEncodeService().encodeHtml(formattedValue);
				if (decorated)
				{
					return decorateValue(escapedValue, PARAMETER_CLASS, true);
				}
				else
				{
					return escapedValue;
				}
			}
			else
			{
				return formattedValue;
			}
		};

		return ruleMessageFormatStrategy.format(breadcrumb, parameters, locale, parameterDecorator);
	}

	protected String decorateValue(final String value, final String styleClass, final boolean styled)
	{
		if (styled)
		{
			return "<span class=\"" + styleClass + "\">" + value + "</span>";
		}

		return value;
	}

	public RuleMessageFormatStrategy getRuleMessageFormatStrategy()
	{
		return ruleMessageFormatStrategy;
	}

	@Required
	public void setRuleMessageFormatStrategy(final RuleMessageFormatStrategy ruleMessageFormatStrategy)
	{
		this.ruleMessageFormatStrategy = ruleMessageFormatStrategy;
	}
	
	protected XssEncodeService getXssEncodeService()
	{
		return xssEncodeService;
	}

	@Required
	public void setXssEncodeService(final XssEncodeService xssEncodeService)
	{
		this.xssEncodeService = xssEncodeService;
	}
}
