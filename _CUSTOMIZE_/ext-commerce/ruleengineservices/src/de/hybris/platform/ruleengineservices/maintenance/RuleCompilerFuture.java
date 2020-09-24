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

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

import de.hybris.platform.ruleengine.concurrency.TaskResult;
import de.hybris.platform.ruleengine.concurrency.impl.DefaultTaskExecutionFuture;
import de.hybris.platform.ruleengineservices.compiler.RuleCompilerResult;

import java.util.Collection;
import java.util.List;
import java.util.Set;


/**
 * The implementation of the rules compilation task future, used to collect the {@link RuleCompilerResult} and wait for the
 * workers to join the main thread
 */
public class RuleCompilerFuture extends DefaultTaskExecutionFuture
{

	private final List<List<RuleCompilerResult>> ruleCompilerResultsList;
	private final Long workerPreDestroyTimeout;

	public RuleCompilerFuture(final Set<Thread> workers, final Long workerPreDestroyTimeout)
	{
		super(workers);
		ruleCompilerResultsList = newArrayList();
		this.workerPreDestroyTimeout = workerPreDestroyTimeout;
	}

	/**
	 * Blocks until the task execution finishes (all threads join) and returns the list of {@link RuleCompilerResult}
	 *
	 * @return list of {@link RuleCompilerResult}
	 */
	@Override
	public TaskResult getTaskResult()
	{
		waitForTasksToFinish();
		return new RuleCompilerTaskResult(getRuleCompilerResults());
	}

	@Override
	public long getWorkerPreDestroyTimeout()
	{
		return workerPreDestroyTimeout;
	}

	/**
	 * Adds the list of {@link RuleCompilerResult} to this future. These results are supposed to keep track of the rule compilation
	 * execution within the same rules partition
	 *
	 * @param ruleCompilerResults
	 * 		list of {@link RuleCompilerResult}
	 */
	public void addRuleCompilerResults(final List<RuleCompilerResult> ruleCompilerResults)
	{
		ruleCompilerResultsList.add(ruleCompilerResults);
	}

	protected List<RuleCompilerResult> getRuleCompilerResults()
	{
		return ruleCompilerResultsList.stream().flatMap(Collection::stream).collect(toList());
	}

}
