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
package de.hybris.platform.ruledefinitions.actions;

import de.hybris.platform.ruleengineservices.rule.evaluation.RuleActionContext;
import de.hybris.platform.ruleengineservices.rule.evaluation.RuleExecutableAction;
import de.hybris.platform.ruleengineservices.rule.evaluation.actions.RAOAction;

import java.util.Map;

import org.springframework.beans.factory.annotation.Required;


/**
 * RuleExecutableAction to RAOAction redirection class. Creates a link betweeen rule compiler and RAOAction interface
 */
public abstract class AbstractRAOActionAware implements RuleExecutableAction
{

	private RAOAction raoAction;

	/**
	 * Execute abstract action. Concrete implementation depends on the RaoAction implementation class
	 *
	 * @param context
	 *           - instance of RuleActionContext
	 * @param parameters
	 *           - a map of rule-defined input parameters for the action
	 *
	 */
	@Override
	public void executeAction(final RuleActionContext context, final Map<String, Object> parameters)
	{
		context.setParameters(parameters);
		getRaoAction().performAction(context);

	}

	protected RAOAction getRaoAction()
	{
		return raoAction;
	}

	@Required
	public void setRaoAction(final RAOAction raoAction)
	{
		this.raoAction = raoAction;
	}

}
