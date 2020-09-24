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
package de.hybris.platform.ruleengine.strategies;

import de.hybris.platform.ruleengine.enums.RuleType;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;

import java.util.List;


/**
 * The lookup interface for resolving the default rules module name by rule type
 */
public interface RulesModuleResolver
{
	/**
	 * Looks up for the rules module name, corresponding to the rule type
	 *
	 * @param ruleType
	 *           rule type
	 * @return qualifying module name
	 */
	String lookupForModuleName(RuleType ruleType);

	/**
	 * Looks up for the rules module, corresponding to the rule type
	 *
	 * @param ruleType
	 *           rule type
	 * @return qualifying module
	 */
	<T extends AbstractRulesModuleModel> T lookupForRulesModule(RuleType ruleType);

	/**
	 * Looks up for the rules modules corresponding to the given rule
	 * 
	 * @param rule
	 *           rule
	 * @return list of qualifying rule modules
	 */
	<T extends AbstractRulesModuleModel> List<T> lookupForRulesModules(AbstractRuleModel rule);
}
