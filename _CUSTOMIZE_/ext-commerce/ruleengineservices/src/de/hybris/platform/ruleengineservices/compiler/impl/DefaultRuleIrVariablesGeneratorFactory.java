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
package de.hybris.platform.ruleengineservices.compiler.impl;

import de.hybris.platform.ruleengineservices.compiler.RuleIrVariablesGenerator;
import de.hybris.platform.ruleengineservices.compiler.RuleIrVariablesGeneratorFactory;


/**
 * Default implementation of {@link RuleIrVariablesGeneratorFactory}.
 */
public class DefaultRuleIrVariablesGeneratorFactory implements RuleIrVariablesGeneratorFactory
{
	@Override
	public RuleIrVariablesGenerator createVariablesGenerator()
	{
		return new DefaultRuleIrVariablesGenerator();
	}
}
