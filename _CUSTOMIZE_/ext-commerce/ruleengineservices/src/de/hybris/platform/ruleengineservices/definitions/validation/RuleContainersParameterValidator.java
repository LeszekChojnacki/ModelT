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
import de.hybris.platform.ruleengineservices.compiler.RuleIrVariablesContainer;
import de.hybris.platform.ruleengineservices.compiler.RuleParameterValidator;
import de.hybris.platform.ruleengineservices.rule.data.AbstractRuleDefinitionData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterDefinitionData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;


public class RuleContainersParameterValidator implements RuleParameterValidator
{
	private static final String INVALID_MESSAGE_KEY = "rule.validation.error.containerids.invalid";
	private static final String NOT_EXIST_MESSAGE_KEY = "rule.validation.error.containerids.notexist";
	protected static final Pattern CONTAINER_ID_PATTERN = Pattern.compile("[a-zA-Z0-9_-]*$");

	private RuleCompilerProblemFactory ruleCompilerProblemFactory;

	@Override
	public void validate(final RuleCompilerContext context, final AbstractRuleDefinitionData ruleDefinition,
			final RuleParameterData parameter, final RuleParameterDefinitionData parameterDefinition)
	{
		if (parameter == null)
		{
			return;
		}

		final Map<String, Integer> qualifyingContainers = parameter.getValue();

		if (MapUtils.isNotEmpty(qualifyingContainers))
		{
			final List<String> invalidContainerIds = new ArrayList<>();
			final List<String> notExistContainerIds = new ArrayList<>();
			for (final String containerId : qualifyingContainers.keySet())
			{
				if (!isValidContainerId(containerId))
				{
					invalidContainerIds.add(containerId);
				}
				if (!isContainerExists(context, containerId))
				{
					notExistContainerIds.add(containerId);
				}
			}

			if (CollectionUtils.isNotEmpty(invalidContainerIds))
			{
				context.addProblem(ruleCompilerProblemFactory.createParameterProblem(RuleCompilerProblem.Severity.ERROR,
						INVALID_MESSAGE_KEY, parameter, parameterDefinition, parameterDefinition.getName(), invalidContainerIds));
			}

			if (CollectionUtils.isNotEmpty(notExistContainerIds))
			{
				context.addProblem(ruleCompilerProblemFactory.createParameterProblem(RuleCompilerProblem.Severity.ERROR,
						NOT_EXIST_MESSAGE_KEY, parameter, parameterDefinition, parameterDefinition.getName(), notExistContainerIds));
			}
		}
	}

	protected boolean isValidContainerId(final String containerId)
	{
		if (StringUtils.isBlank(containerId))
		{
			return false;
		}
		return CONTAINER_ID_PATTERN.matcher(containerId).matches();
	}

	protected boolean isContainerExists(final RuleCompilerContext context, final String containerId)
	{
		final RuleIrVariablesContainer rootContainer = context.getVariablesGenerator().getRootContainer();
		return rootContainer.getChildren().containsKey(containerId);
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
