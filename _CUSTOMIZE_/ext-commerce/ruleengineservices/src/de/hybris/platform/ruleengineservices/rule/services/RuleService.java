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
package de.hybris.platform.ruleengineservices.rule.services;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.ruleengine.enums.RuleType;
import de.hybris.platform.ruleengineservices.enums.RuleStatus;
import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.model.AbstractRuleTemplateModel;

import java.util.List;


/**
 * The interface provides with method for fetching all rules available.
 */
public interface RuleService
{
	/**
	 * Gets all {@link AbstractRuleModel}s
	 *
	 * @return List of {@link AbstractRuleModel}s
	 */
	<T extends AbstractRuleModel> List<T> getAllRules();

	/**
	 * Gets all {@link AbstractRuleModel}s by a specific type
	 *
	 * @param ruleType
	 *  	Defines which type of rules should be included in the results
	 *
	 * @return List of {@link AbstractRuleModel}s
	 */
	<T extends AbstractRuleModel> List<T> getAllRulesForType(Class ruleType);

	/**
	 * Gets all active {@link AbstractRuleModel}s
	 *
	 * @return List of {@link AbstractRuleModel}s
	 */
	<T extends AbstractRuleModel> List<T> getAllActiveRules();

	/**
	 * Gets all active {@link AbstractRuleModel}s by a specific type
	 *
	 * @param ruleType
	 * 	Defines which type of rules should be included in the results
	 *
	 * @return List of {@link AbstractRuleModel}s
	 */
	<T extends AbstractRuleModel> List<T> getAllActiveRulesForType(Class ruleType);

	/**
	 * Gets all rules to be published {@link AbstractRuleModel}s
	 *
	 * @return List of {@link AbstractRuleModel}s
	 *
	 * @deprecated since 1811
	 */
	@Deprecated
	<T extends AbstractRuleModel> List<T> getAllToBePublishedRules();

	/**
	 * Gets all rules to be published {@link AbstractRuleModel}s by a specific type
	 *
	 * @param ruleType
	 *		Defines which type of rules should be included in the results
	 * @return List of {@link AbstractRuleModel}s
	 *
	 * @deprecated since 1811
	 */
	@Deprecated
	<T extends AbstractRuleModel> List<T> getAllToBePublishedRulesForType(Class ruleType);

	/**
	 * Gets active {@link AbstractRuleModel}s by catalog version and rule type
	 *
	 * @param catalogVersion A catalog version which is used to filter out results
	 * @param ruleType A rule type which is used to filter out results
	 * @param <T> Specifies type which will get returned by method
	 * @return List of {@link AbstractRuleModel}s or empty list if not found
	 */
	<T extends AbstractRuleModel> List<T> getActiveRulesForCatalogVersionAndRuleType(CatalogVersionModel catalogVersion,
			RuleType ruleType);

	/**
	 * Gets the latest version of {@link AbstractRuleModel} by code.
	 *
	 * @param code Rule's code to find
	 *
	 * @return {@link AbstractRuleModel} or null if not found
	 *
	 */
	AbstractRuleModel getRuleForCode(String code);

	/**
	 * Gets all versions of {@link AbstractRuleModel} by code.
	 *
	 * @param code Rule's code to find
	 *
	 * @return List of {@link AbstractRuleModel}s
	 *
	 */
	<T extends AbstractRuleModel> List<T> getAllRulesForCode(String code);

	/**
	 * Gets all versions of {@link AbstractRuleModel} by code and status
	 *
	 * @param code Rule's code to find
	 * @param ruleStatuses Rules statuses that are allowed in the results
	 *
	 * @return List of {@link AbstractRuleModel}s
	 *
	 */
	<T extends AbstractRuleModel> List<T> getAllRulesForCodeAndStatus(String code, RuleStatus... ruleStatuses);

	/**
	 * Gets all versions of {@link AbstractRuleModel} by status
	 *
	 * @param ruleStatuses Rules statuses that are allowed in the results
	 *
	 * @return List of {@link AbstractRuleModel}s
	 *
	 */
	<T extends AbstractRuleModel> List<T> getAllRulesForStatus(final RuleStatus... ruleStatuses);

	/**
	 * Creates new rule from given rule template.
	 *
	 * @param ruleTemplate
	 *           - rule template to clone to new rule
	 *
	 * @return the new rule, which is a copy of the given template.
	 */
	<T extends AbstractRuleModel> T createRuleFromTemplate(AbstractRuleTemplateModel ruleTemplate);
	
	/**
	 * Creates new rule from given rule template.
	 * 
	 * @param newRuleCode
	 *           - new rule code
	 *           
	 * @param ruleTemplate
	 *           - rule template to clone to new rule
	 *
	 * @return the new rule, which is a copy of the given template.
	 */
	<T extends AbstractRuleModel> T createRuleFromTemplate(String newRuleCode, AbstractRuleTemplateModel ruleTemplate);

	/**
	 * Clones given rule.
	 *
	 * @param source
	 *           - rule to clone
	 *
	 * @return target - cloned rule
	 */
	AbstractRuleModel cloneRule(AbstractRuleModel source);

	/**
	 * Clones given rule with the specified code for new rule.
	 * 
	 * @param newRuleCode
	 *           - new rule code
	 * 
	 * @param source
	 *           - rule to clone
	 * 
	 * @return target - cloned rule
	 */
	AbstractRuleModel cloneRule(String newRuleCode, AbstractRuleModel source);

	/**
	 * Finds and returns rule type model class based on the template type
	 *
	 * @param templateType
	 *           - template type
	 *
	 * @return rule type model
	 */
	Class<? extends AbstractRuleModel> getRuleTypeFromTemplate(Class<? extends AbstractRuleTemplateModel> templateType);

	/**
	 * Finds engine rule type for given rule type. If no mapping is found it returns RuleType.DEFAULT.
	 *
	 * @param type
	 *           - type of the rule.
	 * @return RuleType of the engine rule
	 */
	RuleType getEngineRuleTypeForRuleType(Class<?> type);
}
