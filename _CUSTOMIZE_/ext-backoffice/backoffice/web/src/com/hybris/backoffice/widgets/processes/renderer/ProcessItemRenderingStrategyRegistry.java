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
package com.hybris.backoffice.widgets.processes.renderer;

import de.hybris.platform.cronjob.model.CronJobHistoryModel;

import java.util.Collections;
import java.util.List;

import com.hybris.backoffice.widgets.processes.ProcessItemRenderingStrategy;
import com.hybris.cockpitng.dataaccess.facades.common.impl.AbstractStrategyRegistry;


public class ProcessItemRenderingStrategyRegistry
		extends AbstractStrategyRegistry<ProcessItemRenderingStrategy, CronJobHistoryModel>
{

	@Override
	public boolean canHandle(final ProcessItemRenderingStrategy strategy, final CronJobHistoryModel context)
	{
		return strategy.canHandle(context);
	}

	public List<ProcessItemRenderingStrategy> getStrategiesList()
	{
		return getStrategies().orElse(Collections.emptyList());
	}
}
