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
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;


/**
 * Historical rule content provider interface to be used in order to set the historical copy incrementally
 */
public interface HistoricalRuleContentProvider
{
	void copyOriginalValuesIntoHistoricalVersion(final AbstractRuleEngineRuleModel droolsRule,
			final AbstractRuleEngineRuleModel historicalDroolsRule, final InterceptorContext ctx);
}
