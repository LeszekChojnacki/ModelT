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
package de.hybris.platform.ruleengineservices.rule.evaluation.actions.impl;

import de.hybris.platform.ruleengineservices.rule.evaluation.RuleActionContext;
import de.hybris.platform.ruleengineservices.rule.evaluation.actions.AbstractRuleExecutableSupport;

import java.util.Map;


public class RuleHaltRAOAction extends AbstractRuleExecutableSupport
{

	@Override
	public boolean performActionInternal(final RuleActionContext context)
	{
		context.halt();
		return true;
	}

	@Override
	protected void validateParameters(final Map<String, Object> parameters) //NOSONAR
	{
		// turn off parameters validation for this action
	}

	@Override
	protected boolean allowedByRuntimeConfiguration(final RuleActionContext context)
	{
		return true;
	}
}
