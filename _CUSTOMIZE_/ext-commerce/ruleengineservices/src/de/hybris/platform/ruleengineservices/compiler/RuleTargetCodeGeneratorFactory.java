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
package de.hybris.platform.ruleengineservices.compiler;

/**
 * Returns the {@link RuleTargetCodeGenerator} that should be used for a specific rule engine.
 */
public interface RuleTargetCodeGeneratorFactory
{
	/**
	 * Returns the appropriate {@link RuleTargetCodeGenerator}.
	 *
	 * @param context
	 *           - the rule compiler context
	 */
	RuleTargetCodeGenerator getTargetCodeGenerator(RuleCompilerContext context);
}
