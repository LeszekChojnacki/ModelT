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
package de.hybris.platform.ruleengine.init;

import de.hybris.platform.servicelayer.config.ConfigurationService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Required;


public class ConcurrentMapFactory
{
	private ConfigurationService configurationService;

	public static final String WORKER_MAP_INITIAL_CAPACITY = "ruleengine.kiemodule.swapping.workers.initialcapacity";
	public static final String WORKER_MAP_LOAD_FACTOR = "ruleengine.kiemodule.swapping.workers.loadfactor";
	public static final String WORKER_MAP_CONCURRENCY_LEVEL = "ruleengine.kiemodule.swapping.workers.concurrencylevel";

	public <K, V> Map<K, V> createNew()
	{
		final int workersInitialCapacity = getConfigurationService().getConfiguration().getInt(WORKER_MAP_INITIAL_CAPACITY, 3);
		final float workersLoadFactor = getConfigurationService().getConfiguration().getFloat(WORKER_MAP_LOAD_FACTOR, 0.75F);
		final int workersConcurrencyLevel = getConfigurationService().getConfiguration().getInt(WORKER_MAP_CONCURRENCY_LEVEL, 2);

		return new ConcurrentHashMap<>(workersInitialCapacity, workersLoadFactor, workersConcurrencyLevel);
	}

	protected ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}
}
