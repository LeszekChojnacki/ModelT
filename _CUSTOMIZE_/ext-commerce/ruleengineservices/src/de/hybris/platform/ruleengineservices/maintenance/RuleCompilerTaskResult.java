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
package de.hybris.platform.ruleengineservices.maintenance;

import static de.hybris.platform.ruleengineservices.compiler.RuleCompilerResult.Result.ERROR;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import de.hybris.platform.ruleengine.concurrency.TaskResult;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerResult;

import java.util.List;


/**
 *  Implementation of {@link TaskResult} for rules compilation
 */
public class RuleCompilerTaskResult implements TaskResult
{
	private List<RuleCompilerResult> ruleCompilerResults;

	public RuleCompilerTaskResult(
				 final List<RuleCompilerResult> ruleCompilerResults)
	{
		this.ruleCompilerResults = ruleCompilerResults;
	}

	@Override
	public State getState()
	{
		if (isNotEmpty(ruleCompilerResults))
		{
			return ruleCompilerResults.stream().filter(r -> r.getResult() == ERROR).findAny().map(r -> State.FAILURE)
						 .orElse(State.SUCCESS);
		}
		return State.SUCCESS;
	}

	public List<RuleCompilerResult> getRuleCompilerResults()
	{
		return ruleCompilerResults;
	}
}
