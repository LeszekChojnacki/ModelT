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
package de.hybris.platform.ruleengineservices.rule.evaluation.impl;

/**
 * RuleAndRuleGroupExecutionTracker is used for tracking rule and rule group executions. It provides methods for
 * checking whether the rule execution is allowed and for the actual tracking.
 */
public interface RuleAndRuleGroupExecutionTracker
{

	/**
	 * returns true if the rule is allowed to be executed, otherwise false
	 */
	boolean allowedToExecute(final Object rule);

	/**
	 * tracks that an action of the given rule code has been executed successfully
	 */
	void trackActionExecutionStarted(String ruleCode);

	/**
	 * tracks that the invoking rule (and its rule group) has been executed successfully
	 */
	void trackRuleExecution(final Object context);

}
