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
package de.hybris.platform.ruleengine.cache.impl;

import de.hybris.platform.ruleengine.cache.RuleGlobalsBeanProvider;
import de.hybris.platform.ruleengine.infrastructure.GetRuleEngineGlobalByName;


/**
 * default (proxy-based) implementation of {@link RuleGlobalsBeanProvider}
 */
public class DefaultRuleGlobalsBeanProvider implements RuleGlobalsBeanProvider
{
	@GetRuleEngineGlobalByName
	@Override
	public Object getRuleGlobals(final String key)
	{
		return null;
	}
}
