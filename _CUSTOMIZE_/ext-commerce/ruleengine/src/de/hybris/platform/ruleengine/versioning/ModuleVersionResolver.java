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

import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;

import java.util.Optional;


/**
 * Helper utility that provides deployed version for a module in compliance with deployed version format convention (see
 * the {@link de.hybris.platform.ruleengine.init.impl.DefaultRuleEngineKieModuleSwapper})
 */
public interface ModuleVersionResolver<T extends AbstractRulesModuleModel>
{
	/**
	 * Extracts module version for a given module
	 *
	 * @param rulesModule
	 *           module name
	 * @return Optional of deployed module version
	 */
	Optional<Long> getDeployedModuleVersion(T rulesModule);

	/**
	 * Extracts module version from provided deployed maven version for a given module
	 *
	 * @param moduleName
	 *           module name
	 * @param deployedMvnVersion
	 *           module's deployed maven version
	 * @return parsed module version
	 */
	Long extractModuleVersion(String moduleName, String deployedMvnVersion);
}
