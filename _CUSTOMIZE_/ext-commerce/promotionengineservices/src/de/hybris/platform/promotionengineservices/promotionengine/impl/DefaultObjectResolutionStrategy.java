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
package de.hybris.platform.promotionengineservices.promotionengine.impl;

import de.hybris.platform.promotionengineservices.promotionengine.PromotionMessageParameterResolutionStrategy;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import java.util.Locale;


/**
 * DefaultProductResolutionStrategy resolves the given {@link RuleParameterData#getValue()} into a string representation
 * of the object.
 */
public class DefaultObjectResolutionStrategy implements PromotionMessageParameterResolutionStrategy
{

	@Override
	public String getValue(final RuleParameterData data, final PromotionResultModel promotionResult, final Locale locale)
	{
		ServicesUtil.validateParameterNotNull(data, "data must not be null");
		ServicesUtil.validateParameterNotNull(data.getValue(), "data value must not be null");

		return data.getValue().toString();
	}

	@Override
	public RuleParameterData getReplacedParameter(final RuleParameterData paramToReplace,
			final PromotionResultModel promotionResult, final Object actualValueAsObject)
	{
		ServicesUtil.validateParameterNotNull(paramToReplace, "parameter paramToReplace must not be null");
		ServicesUtil.validateParameterNotNull(actualValueAsObject, "parameter actualValueAsObject must not be null");

		final RuleParameterData result = new RuleParameterData();
		result.setType(paramToReplace.getType());
		result.setUuid(paramToReplace.getUuid());
		result.setValue(actualValueAsObject);
		return result;
	}
}
