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

import de.hybris.platform.ruleengine.enums.RuleType;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;

import java.util.List;


/**
 * Provides dao functionality for {@code AbstractRulesModuleModel}.
 */
public interface RulesModuleDao
{

	/**
	 * returns the active rules module for the given name or null
	 *
	 * @param name
	 *           the name
	 * @return the rules module for the given name or null
	 */
	<T extends AbstractRulesModuleModel> T findByName(String name);

	/**
	 * returns all (active) rules modules
	 *
	 * @return a list of all rules modules
	 */
	List<AbstractRulesModuleModel> findAll();

	/**
	 * returns the rules module for the given name and version or null
	 *
	 * @param name
	 *           the name
	 * @param version
	 *           the version of the module
	 * @return the rules module for the given name or null
	 */
	<T extends AbstractRulesModuleModel> T findByNameAndVersion(String name, long version);

	/**
	 * returns all (active) rules modules with the given ruleType
	 *
	 * @param ruleType
	 *           the rule type
	 * @return a list of all rules modules
	 */
	List<AbstractRulesModuleModel> findActiveRulesModulesByRuleType(RuleType ruleType);
}
