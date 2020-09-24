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

/**
 * The interface for the properties decision strategy for spliterator
 */
public interface RuleEngineSpliteratorStrategy 
{
	/**
	 * get the number of threads to be allocated for spliterator
	 *
	 * @return number of threads to allocate
	 */
	int getNumberOfThreads();

	/**
	 * calculate the partition size based on total size and the number of partitions to allocate
	 *
	 * @param totalSize
	 * 			 total size
	 * @param numberOfPartitions
	 * 			 number of partitions to allocate
	 * @return size of a single partition
	 */
	static int getPartitionSize(final int totalSize, final int numberOfPartitions)
	{
		int partitionSize = totalSize;
		if (numberOfPartitions > 1 && partitionSize >= numberOfPartitions * numberOfPartitions)
		{
			if (totalSize % numberOfPartitions == 0)
			{
				partitionSize = totalSize / numberOfPartitions;
			}
			else if (numberOfPartitions > 1)
			{
				partitionSize = totalSize / (numberOfPartitions - 1);
			}
		}
		return partitionSize;
	}

}
