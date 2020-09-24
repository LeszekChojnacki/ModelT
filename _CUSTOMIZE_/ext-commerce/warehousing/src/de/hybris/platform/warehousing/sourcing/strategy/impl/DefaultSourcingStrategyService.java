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
 *
 */
package de.hybris.platform.warehousing.sourcing.strategy.impl;

import de.hybris.platform.warehousing.data.sourcing.SourcingContext;
import de.hybris.platform.warehousing.sourcing.strategy.SourcingStrategy;
import de.hybris.platform.warehousing.sourcing.strategy.SourcingStrategyMapper;
import de.hybris.platform.warehousing.sourcing.strategy.SourcingStrategyService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of the sourcing strategy service. We will get strategies for mappers that produce a match.
 * Also, if a matching mapper is terminal, then no other sourcing strategies will be added after the terminal one.
 */
public class DefaultSourcingStrategyService implements SourcingStrategyService, InitializingBean
{
	private List<SourcingStrategy> defaultStrategies;

	@Override
	public List<SourcingStrategy> getStrategies(final SourcingContext context,
			final Collection<SourcingStrategyMapper> mappers)
	{
		final List<SourcingStrategy> strategies = new ArrayList<>();

		for(final SourcingStrategyMapper mapper : mappers)
		{
			if (mapper.isMatch(context))
			{
				strategies.add(mapper.getStrategy());
			}
		}
		return strategies;
	}

	@Override
	public List<SourcingStrategy> getDefaultStrategies()
	{
		return defaultStrategies;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		if (CollectionUtils.isEmpty(defaultStrategies))
		{
			throw new IllegalArgumentException("Default strategies cannot be empty.");
		}

	}

	@Required
	public void setDefaultStrategies(final List<SourcingStrategy> defaultStrategies)
	{
		this.defaultStrategies = defaultStrategies;
	}

}
