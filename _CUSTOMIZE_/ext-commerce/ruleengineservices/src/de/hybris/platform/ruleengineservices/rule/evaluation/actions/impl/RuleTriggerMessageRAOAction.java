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

import de.hybris.platform.ruleengineservices.rao.CartRAO;
import de.hybris.platform.ruleengineservices.rao.DisplayMessageRAO;
import de.hybris.platform.ruleengineservices.rao.RuleEngineResultRAO;
import de.hybris.platform.ruleengineservices.rule.evaluation.RuleActionContext;
import de.hybris.platform.ruleengineservices.rule.evaluation.actions.AbstractRuleExecutableSupport;

import java.util.Map;


public class RuleTriggerMessageRAOAction extends AbstractRuleExecutableSupport
{

	@Override
	public boolean performActionInternal(final RuleActionContext context)
	{
		final DisplayMessageRAO message = performDisplayMessageAction(context);
		postProcessAction(message, context);
		return true;
	}

	@Override
	protected void validateParameters(final Map<String, Object> parameters) //NOSONAR
	{
		// turn off parameters validation for this action
	}

	protected DisplayMessageRAO performDisplayMessageAction(final RuleActionContext context)
	{
		final RuleEngineResultRAO result = context.getRuleEngineResultRao();
		final CartRAO cartRAO = context.getCartRao();
		final DisplayMessageRAO messageRao = new DisplayMessageRAO();
		getRaoUtils().addAction(cartRAO, messageRao);
		result.getActions().add(messageRao);
		setRAOMetaData(context, messageRao);
		context.scheduleForUpdate(cartRAO, result);
		context.insertFacts(messageRao);
		return messageRao;
	}

}
