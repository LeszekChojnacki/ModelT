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
package de.hybris.platform.ruleengineservices.rule.evaluation.actions;

import de.hybris.platform.ruleengineservices.rao.AbstractRuleActionRAO;
import de.hybris.platform.ruleengineservices.rule.evaluation.RuleActionContext;


/**
 * Strategy for supplementing of {@link AbstractRuleActionRAO}.
 *
 */
@SuppressWarnings("squid:S1214")
public interface ActionSupplementStrategy
{
	String UUID_SUFFIX = "_uuid";

	/**
	 * Checks if the action result is proper to be processed by the strategy
	 *
	 * @param actionRao
	 *           - action result
	 * @param context
	 *           - instance of RuleActionContext
	 */
	boolean isActionProperToHandle(final AbstractRuleActionRAO actionRao, final RuleActionContext context);

	/**
	 * Post process of {@link AbstractRuleActionRAO}.
	 *
	 * @param actionRao
	 *           - action result
	 * @param context
	 *           - instance of RuleActionContext
	 */
	void postProcessAction(final AbstractRuleActionRAO actionRao, final RuleActionContext context);
}
