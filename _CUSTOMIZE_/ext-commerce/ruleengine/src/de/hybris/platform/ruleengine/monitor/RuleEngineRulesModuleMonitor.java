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
package de.hybris.platform.ruleengine.monitor;

import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;


/**
 * Interface for rule engine monitor functions
 */
public interface RuleEngineRulesModuleMonitor<T extends AbstractRulesModuleModel>
{

	/**
	 * Indicates whether the given module is actually deployed and running in the rule engine
	 *
	 * @param rulesModule
	 * 		instance of {@link AbstractRulesModuleModel} to check the deployment status for
	 * @return true if the specified module is currently running in the rule engine
	 */
	 boolean isRulesModuleDeployed(final T rulesModule);

}
