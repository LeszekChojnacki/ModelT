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
package de.hybris.platform.ruleengineservices.rule.evaluation;

import java.util.Map;


/**
 * Interface for all actions, supported by rule engine
 */
public interface RuleExecutableAction
{
	/**
	 * execute rule consequence
	 * 
	 * @param context
	 *           - instance of RuleActionContext
	 * @param parameters
	 *           - map of rule facts
	 * @throws {@link RuleEvaluationException}
	 */
	void executeAction(RuleActionContext context, Map<String, Object> parameters);
}
