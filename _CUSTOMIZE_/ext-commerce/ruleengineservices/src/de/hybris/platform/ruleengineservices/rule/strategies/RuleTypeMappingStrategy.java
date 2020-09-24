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
package de.hybris.platform.ruleengineservices.rule.strategies;

import de.hybris.platform.ruleengineservices.model.AbstractRuleModel;
import de.hybris.platform.ruleengineservices.model.AbstractRuleTemplateModel;


/**
 * Implementations of this strategy map rule template types to rule types.
 */
public interface RuleTypeMappingStrategy
{
	/**
	 * Finds and returns rule type model class based on the template type.
	 *
	 * @param templateType
	 *           - template type
	 *
	 * @return the rule type
	 */
	Class<? extends AbstractRuleModel> findRuleType(Class<? extends AbstractRuleTemplateModel> templateType);
}
