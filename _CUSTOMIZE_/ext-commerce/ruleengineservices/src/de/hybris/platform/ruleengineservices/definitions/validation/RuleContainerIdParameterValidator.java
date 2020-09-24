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
package de.hybris.platform.ruleengineservices.definitions.validation;

import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblem;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerProblemFactory;
import de.hybris.platform.ruleengineservices.compiler.RuleParameterValidator;
import de.hybris.platform.ruleengineservices.rule.data.AbstractRuleDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterDefinitionData;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;


public class RuleContainerIdParameterValidator implements RuleParameterValidator
{
	protected static final String MESSAGE_KEY = "rule.validation.error.containerid.invalid";
	protected static final Pattern CONTAINER_ID_PATTERN = Pattern.compile("[a-zA-Z0-9_-]*$");

	private RuleCompilerProblemFactory ruleCompilerProblemFactory;

	@Override
	public void validate(final RuleCompilerContext context, final AbstractRuleDefinitionData ruleDefinition,
			final RuleParameterData parameter, final RuleParameterDefinitionData parameterDefinition)
	{
		if (parameter == null || StringUtils.isBlank((String) parameter.getValue()))
		{
			return;
		}

		final String containerID = parameter.getValue();
		// Check if the container ID is valid
		if (!CONTAINER_ID_PATTERN.matcher(containerID).matches())
		{
			context.addProblem(ruleCompilerProblemFactory.createParameterProblem(RuleCompilerProblem.Severity.ERROR, MESSAGE_KEY,
					parameter, parameterDefinition, containerID, parameterDefinition.getName()));
		}
	}

	public RuleCompilerProblemFactory getRuleCompilerProblemFactory()
	{
		return ruleCompilerProblemFactory;
	}

	@Required
	public void setRuleCompilerProblemFactory(final RuleCompilerProblemFactory ruleCompilerProblemFactory)
	{
		this.ruleCompilerProblemFactory = ruleCompilerProblemFactory;
	}
}
