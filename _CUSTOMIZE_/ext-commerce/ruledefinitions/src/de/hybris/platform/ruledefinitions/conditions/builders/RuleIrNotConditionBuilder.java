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
import de.hybris.platform.ruleengineservices.compiler.RuleIrLocalVariablesContainer;
import de.hybris.platform.ruleengineservices.compiler.RuleIrNotCondition;

import java.util.List;


/**
 * Builder for {@link RuleIrNotCondition} object
 */
public class RuleIrNotConditionBuilder
{

	private static RuleIrNotConditionBuilder self = new RuleIrNotConditionBuilder();
	private RuleIrNotCondition condition;

	public static RuleIrNotConditionBuilder newNotCondition()
	{
		self.condition = new RuleIrNotCondition();
		self.condition.setChildren(newArrayList());
		return self;
	}

	public RuleIrNotConditionBuilder withVariablesContainer(final RuleIrLocalVariablesContainer container)
	{
		self.condition.setVariablesContainer(container);
		return self;
	}

	public RuleIrNotConditionBuilder withChildren(final List<RuleIrCondition> leafConditions)
	{
		self.condition.setChildren(leafConditions);
		return self;
	}

	public RuleIrNotCondition build()
	{
		return condition;
	}

}
