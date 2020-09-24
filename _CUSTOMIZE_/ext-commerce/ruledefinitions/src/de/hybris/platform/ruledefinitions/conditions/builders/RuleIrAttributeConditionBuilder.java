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

import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrAttributeOperator;


/**
 * Builder for {@link RuleIrAttributeCondition} object
 */
public class RuleIrAttributeConditionBuilder
{

	private static RuleIrAttributeConditionBuilder self = new RuleIrAttributeConditionBuilder();
	private RuleIrAttributeCondition condition;

	public static RuleIrAttributeConditionBuilder newAttributeConditionFor(final String variableName)
	{
		self.condition = new RuleIrAttributeCondition();
		self.condition.setVariable(variableName);
		return self;
	}

	public RuleIrAttributeConditionBuilder withAttribute(final String attribute)
	{
		self.condition.setAttribute(attribute);
		return self;
	}

	public RuleIrAttributeConditionBuilder withOperator(final RuleIrAttributeOperator operator)
	{
		self.condition.setOperator(operator);
		return self;
	}

	public RuleIrAttributeConditionBuilder withValue(final Object value)
	{
		self.condition.setValue(value);
		return self;
	}

	public RuleIrAttributeCondition build()
	{
		return condition;
	}

}
