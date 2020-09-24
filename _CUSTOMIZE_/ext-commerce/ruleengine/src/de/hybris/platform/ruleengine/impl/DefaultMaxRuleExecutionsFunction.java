/*
 * [y] hybris Platform
 *
 * Copyright (c) 2019 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.ruleengine.impl;

 import de.hybris.platform.ruleengine.RuleEvaluationContext;
import de.hybris.platform.ruleengineservices.rao.CartRAO;

import java.util.function.Function;

 /**
 * Function that returns a number that represents maximum rule executions during a single evaluation of a context with
 * {@link CartRAO} present. This is a simple implementation that returns value of -1 that translates to 'no max limit'
 */
public class DefaultMaxRuleExecutionsFunction implements Function<RuleEvaluationContext, Integer>
{
	@Override
	public Integer apply(final RuleEvaluationContext context)
	{
		return -1;
	}
}