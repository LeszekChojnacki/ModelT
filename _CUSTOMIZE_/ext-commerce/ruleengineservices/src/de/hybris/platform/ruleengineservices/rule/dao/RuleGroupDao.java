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
import de.hybris.platform.ruleengineservices.model.RuleGroupModel;

import java.util.List;
import java.util.Optional;


public interface RuleGroupDao
{

	/**
	 * Return a rule group
	 *
	 * @param code
	 *           the Rule group code
	 * @return an optional of the rule group with code
	 */
	Optional<RuleGroupModel> findRuleGroupByCode(String code);

	/**
	 * Returns rule groups which has rules corresponding to the specified engine rule type
	 *
	 * @param engineRuleType
	 *           the engine rule type
	 * @return list of the rule groups
	 */
	List<RuleGroupModel> findRuleGroupOfType(RuleType engineRuleType);

	/**
	 * Returns all rule groups which has no any rules
	 *
	 * @return list of the rule groups
	 */
	List<RuleGroupModel> findAllNotReferredRuleGroups();

}
