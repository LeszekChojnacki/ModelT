/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.datahubbackoffice.service.cockpitng.fallback.extender;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.collections.MapUtils;

import com.google.common.collect.Lists;
import com.hybris.cockpitng.core.config.CockpitConfigurationFallbackStrategy;
import com.hybris.cockpitng.core.config.impl.DefaultCockpitConfigurationService;

public class ConfigurationFallbackStrategyExtender
{
	private DefaultCockpitConfigurationService cockpitConfigurationService;
	private List<CockpitConfigurationFallbackStrategy> orginalStrategies = Lists.newArrayList();
	private String key;
	private CockpitConfigurationFallbackStrategy replacement;

	@PostConstruct
	public void init()
	{
		if (MapUtils.isNotEmpty(cockpitConfigurationService.getFallbackStrategies()))
		{
			List<CockpitConfigurationFallbackStrategy> storedFallbackStrategies = cockpitConfigurationService.getFallbackStrategies()
					.get(key);

			if (storedFallbackStrategies == null)
			{
				storedFallbackStrategies = Lists.newArrayList();
				cockpitConfigurationService.getFallbackStrategies().put(key, storedFallbackStrategies);

			}
			else
			{
				this.orginalStrategies = Lists.newArrayList(cockpitConfigurationService.getFallbackStrategies().get(key));
				storedFallbackStrategies.clear();
			}

			storedFallbackStrategies.add(replacement);
		}
		else
		{
			cockpitConfigurationService.getFallbackStrategies().put(key, Lists.newArrayList(replacement));
		}
	}

	@PreDestroy
	public void remove()
	{
		cockpitConfigurationService.getFallbackStrategies().get(key).clear();
		cockpitConfigurationService.getFallbackStrategies().put(key, this.orginalStrategies);
	}

	public void setCockpitConfigurationService(final DefaultCockpitConfigurationService cockpitConfigurationService)
	{
		this.cockpitConfigurationService = cockpitConfigurationService;
	}

	public void setKey(final String key)
	{
		this.key = key;
	}

	public void setReplacement(final CockpitConfigurationFallbackStrategy replacement)
	{
		this.replacement = replacement;
	}
}
