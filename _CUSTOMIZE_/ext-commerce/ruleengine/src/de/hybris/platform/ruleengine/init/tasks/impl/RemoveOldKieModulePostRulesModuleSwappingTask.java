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
package de.hybris.platform.ruleengine.init.tasks.impl;

import de.hybris.platform.ruleengine.RuleEngineActionResult;
import de.hybris.platform.ruleengine.init.RuleEngineKieModuleSwapper;
import de.hybris.platform.ruleengine.init.tasks.PostRulesModuleSwappingTask;

import org.springframework.beans.factory.annotation.Required;


/**
 * Post rules module swapping task, removing the old rules module from the rule engine
 */
public class RemoveOldKieModulePostRulesModuleSwappingTask implements PostRulesModuleSwappingTask
{
	private RuleEngineKieModuleSwapper ruleEngineKieModuleSwapper;

	@Override
	public boolean execute(final RuleEngineActionResult result)
	{
		boolean removed = false;
		if (!result.isActionFailed())
		{
			removed = getRuleEngineKieModuleSwapper().removeOldKieModuleIfPresent(result);
		}
		return removed;
	}

	protected RuleEngineKieModuleSwapper getRuleEngineKieModuleSwapper()
	{
		return ruleEngineKieModuleSwapper;
	}

	@Required
	public void setRuleEngineKieModuleSwapper(final RuleEngineKieModuleSwapper ruleEngineKieModuleSwapper)
	{
		this.ruleEngineKieModuleSwapper = ruleEngineKieModuleSwapper;
	}

}
