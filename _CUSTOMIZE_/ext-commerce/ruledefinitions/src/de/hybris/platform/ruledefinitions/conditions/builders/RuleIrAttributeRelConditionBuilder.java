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
package de.hybris.platform.ruledefinitions.conditions.builders;

import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeOperator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeRelCondition;


/**
 * Builder for {@link RuleIrAttributeRelCondition} object
 */
public class RuleIrAttributeRelConditionBuilder
{

	private static RuleIrAttributeRelConditionBuilder self = new RuleIrAttributeRelConditionBuilder();
	private RuleIrAttributeRelCondition condition;

	public static RuleIrAttributeRelConditionBuilder newAttributeRelationConditionFor(final String variableName)
	{
		self.condition = new RuleIrAttributeRelCondition();
		self.condition.setVariable(variableName);
		return self;
	}

	public RuleIrAttributeRelConditionBuilder withAttribute(final String attribute)
	{
		self.condition.setAttribute(attribute);
		return self;
	}

	public RuleIrAttributeRelConditionBuilder withOperator(final RuleIrAttributeOperator operator)
	{
		self.condition.setOperator(operator);
		return self;
	}

	public RuleIrAttributeRelConditionBuilder withTargetVariable(final String value)
	{
		self.condition.setTargetVariable(value);
		return self;
	}

	public RuleIrAttributeRelConditionBuilder withTargetAttribute(final String targetAttribute)
	{
		self.condition.setTargetAttribute(targetAttribute);
		return self;
	}

	public RuleIrAttributeRelCondition build()
	{
		return condition;
	}

}
