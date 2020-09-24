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
import de.hybris.platform.ruleengineservices.rule.strategies.RuleParameterValueNormalizer;
import org.springframework.beans.factory.annotation.Required;


public class RuleParameterValueTypeDefinition
{
	private RuleParameterValueMapper mapper;
	private RuleParameterValueNormalizer normalizer;
	private String type;

	public RuleParameterValueMapper getMapper()
	{
		return mapper;
	}

	@Required
	public void setMapper(final RuleParameterValueMapper mapper)
	{
		this.mapper = mapper;
	}

	public RuleParameterValueNormalizer getNormalizer()
	{
		return normalizer;
	}

	public void setNormalizer(final RuleParameterValueNormalizer normalizer)
	{
		this.normalizer = normalizer;
	}

	public String getType()
	{
		return type;
	}

	@Required
	public void setType(final String type)
	{
		this.type = type;
	}
}
