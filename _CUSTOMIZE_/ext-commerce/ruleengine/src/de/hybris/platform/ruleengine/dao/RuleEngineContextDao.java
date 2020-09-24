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
package de.hybris.platform.ruleengine.dao;

import de.hybris.platform.ruleengine.model.AbstractRuleEngineContextModel;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;

import java.util.List;



/**
 * Provides dao functionality for {@code AbstractRuleEngineContextModel}.
 */
public interface RuleEngineContextDao
{

	/**
	 * returns the rule engine context for the given name or null
	 *
	 * @param name
	 *           the name
	 * @return the rule engine context for the given name or null
	 */
	AbstractRuleEngineContextModel findRuleEngineContextByName(String name);

	/**
	 * returns the rule engine contexts for the given ruleModule
	 *
	 * @param rulesModule
	 *           the rules module
	 * @return list of rule engine contexts for the given ruleModule
	 */
	<T extends AbstractRuleEngineContextModel> List<T> findRuleEngineContextByRulesModule(AbstractRulesModuleModel rulesModule);
}
