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
package de.hybris.platform.ruleengineservices.maintenance.impl;

import de.hybris.platform.ruleengine.infrastructure.Prototyped;
import de.hybris.platform.ruleengineservices.maintenance.RuleCompilationContext;
import de.hybris.platform.ruleengineservices.maintenance.RuleCompilationContextProvider;


/**
 * Default implementation for {@link RuleCompilationContextProvider}
 */
public class DefaultRuleCompilationContextProvider implements RuleCompilationContextProvider
{

	@Override
	@Prototyped(beanName = "ruleCompilationContext")
	public RuleCompilationContext getRuleCompilationContext()
	{
		return new DefaultRuleCompilationContext();
	}
}
