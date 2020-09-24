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
package de.hybris.platform.adaptivesearch.strategies.impl;

import de.hybris.platform.adaptivesearch.data.AbstractAsBoostItemConfiguration;
import de.hybris.platform.adaptivesearch.data.AsConfigurationHolder;
import de.hybris.platform.adaptivesearch.data.AsSearchProfileResult;
import de.hybris.platform.adaptivesearch.strategies.AsBoostItemsMergeStrategy;
import de.hybris.platform.adaptivesearch.util.MergeMap;
import de.hybris.platform.core.PK;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;


/**
 * Default implementation of {@link AsBoostItemsMergeStrategy} for ADD_AFTER merge mode.
 */
public class AsBoostItemsAddAfterMergeStrategy extends AbstractAsBoostItemsMergeStrategy
{
	@Override
	public void mergeBoostItems(final AsSearchProfileResult source, final AsSearchProfileResult target)
	{
		mergeAfter(source.getPromotedItems(), target.getPromotedItems(), target.getExcludedItems());
		mergeAfter(source.getExcludedItems(), target.getExcludedItems(), target.getPromotedItems());
	}

	protected <V extends AbstractAsBoostItemConfiguration, C extends AbstractAsBoostItemConfiguration> void mergeAfter(
			final Map<PK, AsConfigurationHolder<V, AbstractAsBoostItemConfiguration>> source,
			final Map<PK, AsConfigurationHolder<V, AbstractAsBoostItemConfiguration>> target,
			final Map<PK, AsConfigurationHolder<C, AbstractAsBoostItemConfiguration>> cleanupTarget)
	{
		if (MapUtils.isEmpty(source))
		{
			return;
		}

		final MergeMap<PK, AsConfigurationHolder<V, AbstractAsBoostItemConfiguration>> mergeSource = (MergeMap<PK, AsConfigurationHolder<V, AbstractAsBoostItemConfiguration>>) source;
		final MergeMap<PK, AsConfigurationHolder<V, AbstractAsBoostItemConfiguration>> mergeTarget = (MergeMap<PK, AsConfigurationHolder<V, AbstractAsBoostItemConfiguration>>) target;

		mergeTarget.mergeAfter(mergeSource, (key, replacedConfiguration, configuration) -> {
			final AsConfigurationHolder<V, AbstractAsBoostItemConfiguration> newConfiguration = cloneConfigurationHolder(
					configuration);

			updateReplacedConfigurations(newConfiguration, cleanupTarget.remove(key));
			updateReplacedConfigurations(newConfiguration, replacedConfiguration);

			return newConfiguration;
		});
	}
}