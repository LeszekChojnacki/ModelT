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

import de.hybris.platform.ruleengine.evaluation.RuleAction;

/**
 * Strategy that encapsulates the logic in case rule action can't be executed.
 *
 */
public interface NotExecutableActionStrategy
{
	void handleNotExecutableAction(RuleAction action, Object context);
}
