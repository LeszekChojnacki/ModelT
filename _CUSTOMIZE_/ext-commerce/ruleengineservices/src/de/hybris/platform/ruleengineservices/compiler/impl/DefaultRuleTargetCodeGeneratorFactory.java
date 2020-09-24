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

import de.hybris.platform.ruleengineservices.compiler.RuleCompilerContext;
import de.hybris.platform.ruleengineservices.compiler.RuleTargetCodeGenerator;
import de.hybris.platform.ruleengineservices.compiler.RuleTargetCodeGeneratorFactory;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link RuleTargetCodeGeneratorFactory}.
 */
public class DefaultRuleTargetCodeGeneratorFactory implements RuleTargetCodeGeneratorFactory
{
	private RuleTargetCodeGenerator ruleTargetCodeGenerator;

	public RuleTargetCodeGenerator getRuleTargetCodeGenerator()
	{
		return ruleTargetCodeGenerator;
	}

	@Required
	public void setRuleTargetCodeGenerator(final RuleTargetCodeGenerator ruleTargetCodeGenerator)
	{
		this.ruleTargetCodeGenerator = ruleTargetCodeGenerator;
	}

	@Override
	public RuleTargetCodeGenerator getTargetCodeGenerator(final RuleCompilerContext context)
	{
		return ruleTargetCodeGenerator;
	}
}
