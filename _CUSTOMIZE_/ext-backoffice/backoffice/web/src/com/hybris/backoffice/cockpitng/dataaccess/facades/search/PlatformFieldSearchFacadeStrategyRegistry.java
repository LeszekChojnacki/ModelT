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
package com.hybris.backoffice.cockpitng.dataaccess.facades.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.core.OrderComparator;

import com.hybris.cockpitng.dataaccess.context.Context;
import com.hybris.cockpitng.dataaccess.facades.search.FieldSearchFacadeStrategy;
import com.hybris.cockpitng.dataaccess.facades.search.impl.FieldSearchFacadeStrategyRegistry;
import com.hybris.cockpitng.util.BackofficeSpringUtil;


public class PlatformFieldSearchFacadeStrategyRegistry extends FieldSearchFacadeStrategyRegistry
{

	protected void loadAndSortStrategies()
	{
		final Map<String, FieldSearchFacadeStrategy> strategyMap = BackofficeSpringUtil
				.getAllBeans(FieldSearchFacadeStrategy.class);
		final List strategies = new ArrayList<>(strategyMap.values());
		Collections.sort(strategies, OrderComparator.INSTANCE);
		setStrategies(strategies);
	}

	@Override
	public FieldSearchFacadeStrategy<?> getStrategy(final String context, final Context additionalContext)
	{
		if (!getStrategies().isPresent())
		{
			loadAndSortStrategies();
		}
		return super.getStrategy(context, additionalContext);
	}

}
