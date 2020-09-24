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
package de.hybris.platform.ruleengine.concurrency;

import de.hybris.platform.core.model.ItemModel;

import java.util.List;
import java.util.function.Consumer;


/**
 * Interface encapsulating the behavior of multi-thread executing in tenant-aware context
 *
 * @param <I>
 * 		type if the item models to process
 * @param <T>
 * 		type or expected {@link TaskResult}
 */
public interface RuleEngineTaskProcessor<I extends ItemModel, T extends TaskResult>
{

	/**
	 * Process the items in the list in multi-thread mode with provided items consumer
	 *
	 * @param items
	 * 		the list of items to process
	 * @param taskConsumer
	 * 		instance of {@link Consumer} encapsulating the processing logic
	 * @return The {@link TaskExecutionFuture} of the execution process
	 */
	TaskExecutionFuture<T> execute(final List<I> items, final Consumer<List<I>> taskConsumer);

	/**
	 * Process the items in the list in multi-thread mode with provided items consumer
	 *
	 * @param items
	 * 		the list of items to process
	 * @param taskConsumer
	 * 		instance of {@link Consumer} encapsulating the processing logic
	 * @param predestroyTimeout
	 * 	   time in milliseconds to wait until forcing the thread join (to prevent eventual thread blocking)
	 * @return The {@link TaskExecutionFuture} of the execution process
	 */
	TaskExecutionFuture<T> execute(final List<I> items, final Consumer<List<I>> taskConsumer, long predestroyTimeout);

}
