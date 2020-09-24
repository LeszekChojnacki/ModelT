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
package de.hybris.platform.ruleengineservices.rule.dao;

import de.hybris.platform.ruleengine.enums.RuleType;
import de.hybris.platform.ruleengineservices.enums.RuleStatus;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;

import java.util.List;
import java.util.Optional;


public interface RuleDao
{
	/**
	 * Finds all rules with latest version
	 * @return List of {@link AbstractRuleModel}s
	 */
	<T extends AbstractRuleModel> List<T> findAllRules();

	/**
	 * Finds all rules by a specified type
	 * @param type
	 *           - type of the rule. Must extend {@link AbstractRuleModel}
	 * @return list of all rules of specified type
	 */
	<T extends AbstractRuleModel> List<T> findAllRulesByType(Class<T> type);

	/**
	 * Finds all active {@link AbstractRuleModel}s
	 * @return List of {@link AbstractRuleModel}s
	 */
	<T extends AbstractRuleModel> List<T> findAllActiveRules();

	/**
	 * Finds all active rules by a specified type
	 * @param type
	 *           - type of the rule. Must extend {@link AbstractRuleModel}
	 * @return list of all active rules of specified type
	 */
	<T extends AbstractRuleModel> List<T> findAllActiveRulesByType(Class<T> type);

	/**
	 * Finds all rules to be published {@link AbstractRuleModel}s
	 * @return List of {@link AbstractRuleModel}s
	 *
	 * @deprecated since 1811
	 */
	@Deprecated
	<T extends AbstractRuleModel> List<T> findAllToBePublishedRules();

	/**
	 * Finds all rules to be published by a specified type
	 * @param type
	 *           - type of the rule. Must extend {@link AbstractRuleModel}
	 * @return list of all rules to be published of specified type
	 *
	 * @deprecated since 1811
	 */
	@Deprecated
	<T extends AbstractRuleModel> List<T> findAllToBePublishedRulesByType(Class<T> type);

	/**
	 * Finds the latest version of {@link AbstractRuleModel} by code.
	 * @param code
	 *           - code of the rule
	 * @return a rule, extending {@link AbstractRuleModel} or null if not found
	 */
	<T extends AbstractRuleModel> T findRuleByCode(String code);

	/**
	 * Finds all versions of {@link AbstractRuleModel} by code.
	 * @param code
	 *           - code of the rule
	 * @return list of rules, extending {@link AbstractRuleModel}
	 */
	<T extends AbstractRuleModel> List<T> findAllRuleVersionsByCode(String code);

	/**
	 * Finds {@link AbstractRuleModel} by code.
	 * @param code
	 *           - code of the rule
	 * @param type
	 *           - type of the rule. Must extend {@link AbstractRuleModel}
	 * @return {@link AbstractRuleModel}
	 */
	<T extends AbstractRuleModel> T findRuleByCodeAndType(String code, Class<T> type);

	/**
	 * Finds engine rule type for given rule type.
	 * @param type
	 *           - type of the rule.
	 * @return RuleType of the engine rule
	 */
	RuleType findEngineRuleTypeByRuleType(Class<?> type);

	/**
	 * Return the last version of a rule with a given code
	 * @param code
	 *           the Rule code
	 * @return last version number of a rule. Null if not found
	 */
	Long getRuleVersion(String code);

	/**
	 * Return a rule, having latest version with the status
	 * @param code
	 *           the Rule code
	 * @param ruleStatus
	 *           the rule status
	 * @return an optional of the rule with latest version with the specified status
	 */
	Optional<AbstractRuleModel> findRuleByCodeAndStatus(String code, RuleStatus ruleStatus);

	/**
	 * Return all rule versions with a given code and status
	 * @param code
	 *           the Rule code
	 * @param ruleStatus
	 *           the rule status 
	 * @return list of rules extending {@link AbstractRuleModel}
	 */
	<T extends AbstractRuleModel> List<T> findAllRuleVersionsByCodeAndStatus(String code, RuleStatus ruleStatus);

	/**
	 * Return all rule versions with a given code and one of the statuses listed in ruleStatuses param. If no status is provided,
	 * the method fallback to {@link #findAllRuleVersionsByCode(String)}
	 * @param code
	 *           the Rule code
	 * @param ruleStatuses
	 *           the rule status array
	 * @return list of rules extending {@link AbstractRuleModel}
	 */
	<T extends AbstractRuleModel> List<T> findAllRuleVersionsByCodeAndStatuses(final String code, final RuleStatus... ruleStatuses);

	/**
	 * Return all rule versions with one of the statuses listed in ruleStatuses param
	 * @param ruleStatuses
	 *           the rule status array
	 * @return list of rules extending {@link AbstractRuleModel}
	 */
	<T extends AbstractRuleModel> List<T> findAllRulesWithStatuses(final RuleStatus... ruleStatuses);

	/**
	 * Return all rules within statuses and version
	 * @param version
	 *           the rule version
	 * @param ruleStatuses
	 *           the rule statuses
	 * @return list of rules extending {@link AbstractRuleModel}
	 */
	<T extends AbstractRuleModel> List<T> findByVersionAndStatuses(Long version, RuleStatus... ruleStatuses);

	/**
	 * Return a rule with a specified version
	 * @param code
	 *           the Rule code
	 * @param version
	 *           the version number
	 * @return an optional of the rule with code and version
	 */
	Optional<AbstractRuleModel> findRuleByCodeAndVersion(String code, Long version);

}
