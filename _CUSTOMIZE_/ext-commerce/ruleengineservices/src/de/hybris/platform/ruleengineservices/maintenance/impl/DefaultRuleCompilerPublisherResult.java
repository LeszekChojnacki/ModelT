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

import de.hybris.platform.ruleengine.RuleEngineActionResult;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerResult;
import de.hybris.platform.ruleengineservices.maintenance.RuleCompilerPublisherResult;

import java.util.List;


/**
 * Default implementation of {@link RuleCompilerPublisherResult}
 */
public class DefaultRuleCompilerPublisherResult implements RuleCompilerPublisherResult
{
	private final Result result;
	private final List<RuleCompilerResult> compilerResults;
	private final List<RuleEngineActionResult> publisherResults;

	public DefaultRuleCompilerPublisherResult(final Result result, final List<RuleCompilerResult> compilerResults,
			final List<RuleEngineActionResult> publisherResults)
	{
		this.compilerResults = compilerResults;
		this.publisherResults = publisherResults;
		this.result = result;
	}

	@Override
	public Result getResult()
	{
		return result;
	}

	@Override
	public List<RuleCompilerResult> getCompilerResults()
	{
		return compilerResults;
	}

	@Override
	public List<RuleEngineActionResult> getPublisherResults()
	{
		return publisherResults;
	}

}
