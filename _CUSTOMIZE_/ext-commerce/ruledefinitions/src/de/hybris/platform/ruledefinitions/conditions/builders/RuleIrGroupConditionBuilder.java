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

import static com.google.common.collect.Lists.newArrayList;

import de.hybris.platform.ruleengineservices.compiler.RuleIrCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrGroupCondition;
import de.hybris.platform.ruleengineservices.compiler.RuleIrGroupOperator;

import java.util.List;


/**
 * Builder for {@link RuleIrGroupCondition} object
 */
public class RuleIrGroupConditionBuilder
{

	private static RuleIrGroupConditionBuilder self = new RuleIrGroupConditionBuilder();
	private RuleIrGroupCondition condition;

	public static RuleIrGroupConditionBuilder newGroupConditionOf(final RuleIrGroupOperator operator)
	{
		self.condition = new RuleIrGroupCondition();
		self.condition.setOperator(operator);
		self.condition.setChildren(newArrayList());
		return self;
	}

	public RuleIrGroupConditionBuilder withChildren(final List<RuleIrCondition> leafConditions)
	{
		self.condition.setChildren(leafConditions);
		return self;
	}

	public RuleIrGroupCondition build()
	{
		return condition;
	}

}
