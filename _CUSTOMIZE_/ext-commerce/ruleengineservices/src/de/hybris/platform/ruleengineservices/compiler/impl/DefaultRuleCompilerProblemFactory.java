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

import de.hybris.platform.ruleengineservices.compiler.RuleCompilerParameterProblem;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblem;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblem.Severity;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblemFactory;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterDefinitionData;
import de.hybris.platform.servicelayer.i18n.L10NService;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link RuleCompilerProblemFactory}.
 */
public class DefaultRuleCompilerProblemFactory implements RuleCompilerProblemFactory
{
	private L10NService l10nService;

	@Override
	public RuleCompilerProblem createProblem(final Severity severity, final String messageKey, final Object... parameters)
	{
		return new DefaultRuleCompilerProblem(severity, l10nService.getLocalizedString(messageKey, parameters));
	}

	@Override
	public RuleCompilerParameterProblem createParameterProblem(final Severity severity, final String messageKey,
			final RuleParameterData parameterData, final RuleParameterDefinitionData parameterDefinitionData, final Object... parameters)
	{
		return new DefaultRuleCompilerParameterProblem(severity, l10nService.getLocalizedString(messageKey, parameters), parameterData, parameterDefinitionData);
	}

	public L10NService getL10nService()
	{
		return l10nService;
	}

	@Required
	public void setL10nService(final L10NService l10nService)
	{
		this.l10nService = l10nService;
	}
}
