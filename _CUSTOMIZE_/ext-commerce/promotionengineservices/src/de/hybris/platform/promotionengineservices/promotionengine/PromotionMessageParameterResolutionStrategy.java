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
package de.hybris.platform.promotionengineservices.promotionengine;

import de.hybris.platform.promotionengineservices.promotionengine.impl.DefaultPromotionEngineResultService;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;

import java.util.Locale;


/**
 * PromotionMessageParameterResolutionStrategy is used by {@link DefaultPromotionEngineResultService} for resolving
 * {@link RuleParameterData} into displayable messages.
 */
public interface PromotionMessageParameterResolutionStrategy
{

	/**
	 * resolves the given {@link RuleParameterData} into a displayable Object.
	 *
	 * @param data
	 *           the rule parameter to resolve
	 * @param promotionResult
	 *           the promotion result
	 * @param locale
	 *           the locale
	 * @return an object (to be displayed via {@link #toString()}
	 */
	Object getValue(final RuleParameterData data, final PromotionResultModel promotionResult, final Locale locale);

	/**
	 * returns {@link RuleParameterData} created from given {@link RuleParameterData} with replaced actual value.
	 *
	 * @param paramToReplace
	 *           original rule parameter
	 * @param promotionResult
	 *           the promotion result
	 * @param actualValueAsObject
	 *           actual value to set
	 * @return an object (to be displayed via {@link #toString()}
	 */
	default RuleParameterData getReplacedParameter(final RuleParameterData paramToReplace,
			final PromotionResultModel promotionResult, final Object actualValueAsObject)
	{
		return paramToReplace;
	}
}
