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
package de.hybris.platform.ruleengine.concurrency.impl;

import static com.google.common.collect.Lists.partition;
import static com.google.common.collect.Sets.newHashSet;
import static de.hybris.platform.ruleengine.concurrency.RuleEngineSpliteratorStrategy.getPartitionSize;

import de.hybris.platform.core.Tenant;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.ruleengine.concurrency.RuleEngineSpliteratorStrategy;
import de.hybris.platform.ruleengine.concurrency.RuleEngineTaskProcessor;
import de.hybris.platform.ruleengine.concurrency.SuspendResumeTaskManager;
import de.hybris.platform.ruleengine.concurrency.TaskExecutionFuture;
import de.hybris.platform.ruleengine.concurrency.TaskResult;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link RuleEngineTaskProcessor}
 *
 * @param <I>
 * 		type of {@link ItemModel} in the list
 */
public class DefaultRuleEngineTaskProcessor<I extends ItemModel>
		implements RuleEngineTaskProcessor<I, TaskResult>, RuleEngineSpliteratorStrategy
{

	private Tenant tenant;
	private ThreadFactory tenantAwareThreadFactory;
	private SuspendResumeTaskManager suspendResumeTaskManager;

	@Override
	public TaskExecutionFuture<TaskResult> execute(final List<I> items, final Consumer<List<I>> taskConsumer)
	{
		return execute(items, taskConsumer, -1);
	}

	@Override
	public TaskExecutionFuture<TaskResult> execute(final List<I> items, final Consumer<List<I>> taskConsumer,
			final long predestroyTimeout)
	{
		final List<List<I>> partitionedItems = partition(items, getPartitionSize(items.size(), getNumberOfThreads()));
		final Set<Thread> workers = newHashSet();
		for (List<I> itemsPartition : partitionedItems)
		{
			workers.add(createAndStartNewWorker(itemsPartition, taskConsumer));
		}
		return new DefaultTaskExecutionFuture(workers, predestroyTimeout);
	}

	@Override
	public int getNumberOfThreads()
	{
		return Runtime.getRuntime().availableProcessors() + 1;
	}

	protected Thread createAndStartNewWorker(final List<I> items, final Consumer<List<I>> taskConsumer)
	{
		final Thread asyncWorker;
		try
		{
			asyncWorker = getTenant()
					.createAndRegisterBackgroundThread(() -> taskConsumer.accept(items), getTenantAwareThreadFactory());
			getSuspendResumeTaskManager().registerAsNonSuspendableTask(asyncWorker, "Generic rule engine task is in progress");
		}
		catch (final Exception e)
		{
			throw new IllegalStateException("Exception caught: ", e);
		}
		asyncWorker.start();
		return asyncWorker;
	}

	protected Tenant getTenant()
	{
		return tenant;
	}

	@Required
	public void setTenant(final Tenant tenant)
	{
		this.tenant = tenant;
	}

	protected ThreadFactory getTenantAwareThreadFactory()
	{
		return tenantAwareThreadFactory;
	}

	@Required
	public void setTenantAwareThreadFactory(final ThreadFactory tenantAwareThreadFactory)
	{
		this.tenantAwareThreadFactory = tenantAwareThreadFactory;
	}

	protected SuspendResumeTaskManager getSuspendResumeTaskManager()
	{
		return suspendResumeTaskManager;
	}

	@Required
	public void setSuspendResumeTaskManager(final SuspendResumeTaskManager suspendResumeTaskManager)
	{
		this.suspendResumeTaskManager = suspendResumeTaskManager;
	}
}
