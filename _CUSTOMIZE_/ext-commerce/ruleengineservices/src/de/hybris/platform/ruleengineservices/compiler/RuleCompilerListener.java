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
 * Interface for receiving notifications about {@link RuleCompilerContext} instances.
 */
public interface RuleCompilerListener
{
	/**
	 * Handles a notification that the compilation for a particular {@link RuleCompilerContext} is about to begin.
	 *
	 * @param context
	 *           - the {@link RuleCompilerContext}
	 */
	void beforeCompile(RuleCompilerContext context);

	/**
	 * Handles a notification that the compilation for a particular {@link RuleCompilerContext} has just been completed.
	 *
	 * @param context
	 *           - the {@link RuleCompilerContext}
	 */
	void afterCompile(RuleCompilerContext context);

	/**
	 * Handles a notification that the compilation for a particular {@link RuleCompilerContext} failed.
	 *
	 * @param context
	 *           - the {@link RuleCompilerContext}
	 */
	void afterCompileError(RuleCompilerContext context);
}
