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
package de.hybris.platform.promotionengineservices.compiler.strategies;

import de.hybris.platform.promotionengineservices.model.PromotionSourceRuleModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedPromotionModel;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.rule.data.RuleConditionData;


/**
 * Strategy for resolving values of rule conditions parameters and storing them for the rule. Later they can be used for
 * example to evaluate potential promotions on product detail page.
 */
public interface ConditionResolutionStrategy
{
	/**
	 * Gets the value from parameters of {@code condition} and stores it together with the given
	 * {@code promotionSourceRule}
	 *
	 * @param condition
	 * 		{@link RuleConditionData} to get the values from.
	 * @param rule
	 * 		{@link PromotionSourceRuleModel} to store the retrieved value for.
	 * @param ruleBasedPromotion
	 * 		{@link RuleBasedPromotionModel} related to the given source rule
	 */
	void getAndStoreParameterValues(RuleConditionData condition, PromotionSourceRuleModel rule,
			RuleBasedPromotionModel ruleBasedPromotion);

	/**
	 * Cleans previously stored condition parameter values for given context.
	 *
	 * @param context
	 * 		{@link RuleCompilerContext} to clean the parameter values for.
	 */
	void cleanStoredParameterValues(final RuleCompilerContext context);
}
