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
package de.hybris.platform.ruleengineservices.versioning.impl;

import de.hybris.platform.ruleengineservices.model.SourceRuleModel;
import de.hybris.platform.ruleengineservices.versioning.HistoricalRuleContentProvider;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;


/**
 * Default implementation of {@link HistoricalRuleContentProvider}
 */
public class DefaultSourceRuleHistoricalRuleContentProvider implements HistoricalRuleContentProvider
{

	@Override
	public void copyOriginalValuesIntoHistoricalVersion(final SourceRuleModel sourceRule,
			final SourceRuleModel historicalSourceRule, final InterceptorContext ctx)
	{
		historicalSourceRule.setUuid(sourceRule.getUuid());
		historicalSourceRule.setCode(sourceRule.getCode());
	}

}
