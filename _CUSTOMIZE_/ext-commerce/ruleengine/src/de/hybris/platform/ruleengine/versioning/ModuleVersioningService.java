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
package de.hybris.platform.ruleengine.versioning;

import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;

import java.util.Optional;
import java.util.Set;


/**
 * Module versioning service
 */
public interface ModuleVersioningService
{

	/**
	 * Returns the current version of the rule module
	 *
	 * @param ruleModel
	 *           ruleModel - the AbstractRuleEngineRuleModel instance
	 * @return Long the module version if applicable, empty optional otherwise
	 */
	Optional<Long> getModuleVersion(final AbstractRuleEngineRuleModel ruleModel); // NOSONAR


	/**
	 * assert (and change if necessary) the version of the module of the rule
	 *
	 * @param ruleModel
	 *           - rule model
	 * @param rulesModule
	 *           instance of {@link AbstractRulesModuleModel} to be assigned as a module for this rule
	 */
	void assertRuleModuleVersion(AbstractRuleEngineRuleModel ruleModel, AbstractRulesModuleModel rulesModule);


	/**
	 * Given the module, assert it's version in accordance with the provided rules
	 *
	 * @param moduleModel
	 *           - rules module instance
	 * @param rules
	 *           - set of rules to assert the module version with
	 */
	void assertRuleModuleVersion(AbstractRulesModuleModel moduleModel, Set<AbstractRuleEngineRuleModel> rules);
	

	/**
	 * get the currently deployed (in the rule engine) version of the {@link AbstractRulesModuleModel}
	 *
	 * @param ruleCode
	 *           the rule code of the rule associated to the module
	 * @param moduleName
	 *           the rules module name
	 * @return the version as {@link Optional<Long>}
	 */
	@SuppressWarnings("javadoc")
	Optional<Long> getDeployedModuleVersionForRule(String ruleCode, String moduleName);

}
