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

import de.hybris.platform.ruleengineservices.rule.evaluation.RuleActionContext;

import java.util.Map;


/**
 * abstract RAO action interface
 */
public interface RAOAction
{

	/**
	 * perform action on RAO objects
	 *
	 * @param context
	 *           - instance of RuleActionContext
	 * @param parameters
	 *           - map of named objects, creating the execution context for an action
	 *
	 * @deprecated since 6.6 Use the method w/o 'parameters' argument instead. {@link RuleActionContext} should
	 *             encapsulate this information.
	 */
	@Deprecated
	void performAction(RuleActionContext context, Map<String, Object> parameters);


	/**
	 * perform action on RAO objects
	 *
	 * @param context
	 *           - instance of RuleActionContext
	 */

	void performAction(RuleActionContext context);

}
