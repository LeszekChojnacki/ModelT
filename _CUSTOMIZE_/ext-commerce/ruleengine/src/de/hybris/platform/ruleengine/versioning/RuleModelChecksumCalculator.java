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


/**
 * rule model checksum calculator interface
 */
public interface RuleModelChecksumCalculator
{
	/**
	 * Calculates the checksum, based on AbstractRuleEngineRuleModel instance content
	 *
	 * @param rule
	 *           - instance of AbstractRuleEngineRuleModel
	 *
	 * @return a string representing the checksum for the model
	 */
	String calculateChecksumOf(AbstractRuleEngineRuleModel rule);
}
