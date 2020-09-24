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
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterDefinitionData;
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterUuidGenerator;

import java.util.UUID;


/**
 * Default Implementation of {@link RuleParameterUuidGenerator}.
 */
public class DefaultRuleParameterUuidGenerator implements RuleParameterUuidGenerator
{
	@Override
	public String generateUuid(final RuleParameterData parameter, final RuleParameterDefinitionData parameterDefinition)
	{
		final UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}
}
