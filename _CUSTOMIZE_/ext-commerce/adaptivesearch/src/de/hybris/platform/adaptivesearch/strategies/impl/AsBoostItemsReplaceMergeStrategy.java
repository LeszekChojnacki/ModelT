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
import de.hybris.platform.adaptivesearch.enums.AsBoostItemsMergeMode;
import de.hybris.platform.adaptivesearch.strategies.AsBoostItemsMergeStrategy;
import de.hybris.platform.adaptivesearch.util.MergeMap;
import de.hybris.platform.core.PK;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;


/**
 * Default implementation of {@link AsBoostItemsMergeStrategy} for REPLACE merge mode.
 */
public class AsBoostItemsReplaceMergeStrategy extends AbstractAsBoostItemsMergeStrategy
{
	@Override
	public void mergeBoostItems(final AsSearchProfileResult source, final AsSearchProfileResult target)
	{
		target.setBoostItemsMergeMode(AsBoostItemsMergeMode.REPLACE);

		replace(source.getPromotedItems(), target.getPromotedItems());
		replace(source.getExcludedItems(), target.getExcludedItems());
	}

	protected <V extends AbstractAsBoostItemConfiguration> void replace(
			final Map<PK, AsConfigurationHolder<V, AbstractAsBoostItemConfiguration>> source,
			final Map<PK, AsConfigurationHolder<V, AbstractAsBoostItemConfiguration>> target)
	{
		target.clear();

		if (MapUtils.isEmpty(source))
		{
			return;
		}

		final MergeMap<PK, AsConfigurationHolder<V, AbstractAsBoostItemConfiguration>> mergeSource = (MergeMap<PK, AsConfigurationHolder<V, AbstractAsBoostItemConfiguration>>) source;
		final MergeMap<PK, AsConfigurationHolder<V, AbstractAsBoostItemConfiguration>> mergeTarget = (MergeMap<PK, AsConfigurationHolder<V, AbstractAsBoostItemConfiguration>>) target;

		mergeTarget.mergeAfter(mergeSource, (key, replacedConfiguration, configuration) -> cloneConfigurationHolder(configuration));
	}
}