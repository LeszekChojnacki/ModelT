/*
 * Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved
 */
package com.hybris.backoffice.engine.operations.impl;

import de.hybris.platform.util.threadpool.PoolableThread;
import de.hybris.platform.util.threadpool.ThreadPool;

import com.hybris.cockpitng.engine.operations.LongOperationThreadExecutor;
import com.hybris.cockpitng.engine.operations.impl.DefaultLongOperationThreadExecutor;


/**
 * This implementation of {@link LongOperationThreadExecutor} utilizes platform's {@link ThreadPool} to execute async
 * operations and interact with the thread pool.
 */
public class BackofficeLongOperationThreadExecutor extends DefaultLongOperationThreadExecutor
{

	/**
	 * This implementation uses platforms thread pool and guarantees that the thread is returned to the pool after the
	 * runnable is executed.
	 *
	 * @param runnable
	 *           Runnable to execute
	 */
	@Override
	public void execute(final Runnable runnable)
	{
		getPoolableThread().execute(runnable);
	}

	protected PoolableThread getPoolableThread()
	{
		return ThreadPool.getInstance().borrowThread();
	}
}
