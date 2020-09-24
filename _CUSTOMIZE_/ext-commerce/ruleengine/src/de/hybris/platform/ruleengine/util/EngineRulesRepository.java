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
package de.hybris.platform.ruleengine.util;

import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;

import java.util.Collection;


/**
 * Repository of deployed engine rules
 */
public interface EngineRulesRepository
{

	/**
	 * Checks whether the given engine rule is deployed or would be deployed on startup as part of the given module
	 *
	 * @param engineRule
	 * 		instance of {@link AbstractRuleEngineRuleModel} to check the deployment status for
	 * @param moduleName
	 * 		name of the module to check the deployment status against
	 * @return true if the given rule is deployed or could be deployed on startup as part of the given module
	 */
	<T extends AbstractRuleEngineRuleModel> boolean checkEngineRuleDeployedForModule(T engineRule, String moduleName);

	/**
	 * Get engine rules, deployed for a given module
	 *
	 * @param moduleName
	 * 		the name of the rules module
	 * @return collection of engine rules (instances of {@link AbstractRuleEngineRuleModel}), deployed to a module
	 */
	<T extends AbstractRuleEngineRuleModel> Collection<T> getDeployedEngineRulesForModule(String moduleName);

	/**
	 * Get number of deployed rules for a rules module
	 *
	 * @param moduleName
	 * 		the name of the rules module
	 * @return number of deployed rules
	 */
	long countDeployedEngineRulesForModule(String moduleName);

}
