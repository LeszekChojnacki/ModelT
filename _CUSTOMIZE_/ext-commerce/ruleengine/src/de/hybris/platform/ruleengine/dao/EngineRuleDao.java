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

import de.hybris.platform.ruleengine.model.AbstractRuleEngineRuleModel;
import de.hybris.platform.ruleengine.model.AbstractRulesModuleModel;
import de.hybris.platform.servicelayer.internal.dao.Dao;

import java.util.Collection;
import java.util.List;


/**
 * Data Access Object for Engine Rule Model.
 */
public interface EngineRuleDao extends Dao
{
	/**
	 * retrieve the instance of &lt; T extends {@link AbstractRuleEngineRuleModel} &gt; by UUID
	 * @param uuid
	 *           the rule uuid
	 * @return AbstractRuleEngineRuleModel by its uuid.
	 *         Finds AbstractRuleEngineRuleModel by its uuid.
	 */
	<T extends AbstractRuleEngineRuleModel> T getRuleByUuid(String uuid);

	/**
	 * @param ruleUuids
	 *           a collection of UUIDs to get the rules for
	 * @param <T>
	 *           type of the expected engine rule instance
	 * @return a Collection if rule models
	 */
	<T extends AbstractRuleEngineRuleModel> Collection<T> getRulesByUuids(Collection<String> ruleUuids);

	/**
	 * Finds AbstractRuleEngineRuleModels by their common code.
	 *
	 * @param code
	 *           the rule code
	 * @return list of AbstractRuleEngineRuleModel by its code.
	 */
	List<AbstractRuleEngineRuleModel> findRulesByCode(String code);

	/**
	 * Finds AbstractRuleEngineRuleModel by its code and module.
	 *
	 * @param code
	 *           the rule code
	 * @param moduleName
	 *           name of the module
	 * @return AbstractRuleEngineRuleModel by its code.
	 */
	AbstractRuleEngineRuleModel getRuleByCode(String code, String moduleName);

	/**
	 * Finds AbstractRuleEngineRuleModel by its code and maximum version.
	 *
	 * @param code
	 *           the rule code
	 * @param moduleName
	 *           the name of the rules module
	 * @param version
	 *           the maximum version of the rule
	 * @return AbstractRuleEngineRuleModel by its code and maximum version.
	 */
	AbstractRuleEngineRuleModel getRuleByCodeAndMaxVersion(String code, String moduleName, long version);

	/**
	 * Finds active {@link AbstractRuleEngineRuleModel} by its code and maximum version.
	 *
	 * @param code
	 *           the rule code
	 * @param moduleName
	 *           the rules module name
	 * @param version
	 *           the maximum version of the rule
	 * @return AbstractRuleEngineRuleModel with active=true by its code and maximum version.
	 */
	AbstractRuleEngineRuleModel getActiveRuleByCodeAndMaxVersion(String code, String moduleName, long version);

	/**
	 * Finds all currently active {@link AbstractRuleEngineRuleModel} for a given rules module.
	 *
	 * @param moduleName
	 *           the name of the rules module
	 * @return list of currently active AbstractRuleEngineRuleModel
	 */
	List<AbstractRuleEngineRuleModel> getActiveRules(String moduleName);

	/**
	 * Finds all currently active AbstractRuleEngineRuleModels for a given rule module.
	 *
	 * @param rulesModule
	 *           the rule module
	 * @return list of currently active AbstractRuleEngineRuleModel
	 */
	List<AbstractRuleEngineRuleModel> getActiveRules(AbstractRulesModuleModel rulesModule);

	/**
	 * Finds all rules for the rule module and a given version of knowledge base
	 *
	 * @param moduleName
	 *           the rules module name
	 * @param version
	 *           the KieBase version
	 * @return list of rules, valid for a given KieBase version
	 */
	<T extends AbstractRuleEngineRuleModel> List<T> getRulesForVersion(String moduleName, long version);

	/**
	 * Finds currently active rules for the rule module and a given version of knowledge base
	 *
	 * @param moduleName
	 *           the rules module name
	 * @param version
	 *           the KieBase version
	 * @return list of rules, valid for a given KieBase version
	 */
	<T extends AbstractRuleEngineRuleModel> List<T> getActiveRulesForVersion(String moduleName, long version);

	/**
	 * Returns the current rules snapshot version number for a given module
	 *
	 * @param rulesModule
	 *           the rule module
	 * @return maximum version number
	 */
	Long getCurrentRulesSnapshotVersion(AbstractRulesModuleModel rulesModule);

	/**
	 * Return the last version of a rule with a given code and given module
	 *
	 * @param code
	 *           the Rule code
	 * @param moduleName
	 *           the rules module name
	 * @return last version number of a rule. Null if not found
	 */
	Long getRuleVersion(String code, String moduleName);


}
