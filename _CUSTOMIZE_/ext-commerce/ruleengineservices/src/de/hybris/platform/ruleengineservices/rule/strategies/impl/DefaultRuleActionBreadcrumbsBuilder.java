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

import de.hybris.platform.ruleengineservices.RuleEngineServiceException;
import de.hybris.platform.ruleengineservices.rule.data.RuleActionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleActionDefinitionData;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleActionBreadcrumbsBuilder;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;


public class DefaultRuleActionBreadcrumbsBuilder extends AbstractRuleBreadcrumbsBuilder implements RuleActionBreadcrumbsBuilder
{
	protected static final String DEFAULT_SEPARATOR = ", ";

	private I18NService i18NService;

	@Override
	public String buildActionBreadcrumbs(final List<RuleActionData> actions,
			final Map<String, RuleActionDefinitionData> actionDefinitions)
	{
		return buildBreadcrumbs(actions, actionDefinitions, false);
	}

	@Override
	public String buildStyledActionBreadcrumbs(final List<RuleActionData> actions,
			final Map<String, RuleActionDefinitionData> actionDefinitions)
	{
		return buildBreadcrumbs(actions, actionDefinitions, true);
	}

	protected String buildBreadcrumbs(final List<RuleActionData> actions,
			final Map<String, RuleActionDefinitionData> actionDefinitions, final boolean styled)
	{
		ServicesUtil.validateParameterNotNull(actions, "actions cannot be null");
		ServicesUtil.validateParameterNotNull(actionDefinitions, "action definitions cannot be null");

		final StringBuilder breadcrumbBuilder = new StringBuilder();
		final Locale locale = i18NService.getCurrentLocale();
		final String separator = buildSeparator(styled);

		int index = 0;
		for (final RuleActionData action : actions)
		{
			final RuleActionDefinitionData actionDefinition = actionDefinitions.get(action.getDefinitionId());
			if (actionDefinition == null)
			{
				throw new RuleEngineServiceException("No action definition found for id " + action.getDefinitionId());
			}

			if (StringUtils.isNotEmpty(actionDefinition.getBreadcrumb()))
			{
				if (index != 0)
				{
					breadcrumbBuilder.append(separator);
				}

				final String breadcrumb = formatBreadcrumb(actionDefinition.getBreadcrumb(), action.getParameters(), locale, styled,
						true);
				breadcrumbBuilder.append(breadcrumb);

				index++;
			}
		}

		return breadcrumbBuilder.toString();
	}

	protected String buildSeparator(final boolean styled)
	{
		return decorateValue(DEFAULT_SEPARATOR, SEPARATOR_CLASS, styled);
	}

	public I18NService getI18NService()
	{
		return i18NService;
	}

	@Required
	public void setI18NService(final I18NService i18NService)
	{
		this.i18NService = i18NService;
	}
}
