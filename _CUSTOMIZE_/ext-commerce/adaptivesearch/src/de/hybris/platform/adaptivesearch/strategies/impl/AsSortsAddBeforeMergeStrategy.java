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

import de.hybris.platform.adaptivesearch.data.AbstractAsSortConfiguration;
import de.hybris.platform.adaptivesearch.data.AsConfigurationHolder;
import de.hybris.platform.adaptivesearch.data.AsSearchProfileResult;
import de.hybris.platform.adaptivesearch.strategies.AsSortsMergeStrategy;
import de.hybris.platform.adaptivesearch.util.MergeMap;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;


/**
 * Default implementation of {@link AsSortsMergeStrategy} for ADD_BEFORE merge mode.
 */
public class AsSortsAddBeforeMergeStrategy extends AbstractAsSortsMergeStrategy
{
	@Override
	public void mergeSorts(final AsSearchProfileResult source, final AsSearchProfileResult target)
	{
		mergeBefore(source.getPromotedSorts(), target.getPromotedSorts(), target.getSorts(), target.getExcludedSorts());
		mergeBefore(source.getSorts(), target.getSorts(), target.getPromotedSorts(), target.getExcludedSorts());
		mergeBefore(source.getExcludedSorts(), target.getExcludedSorts(), target.getPromotedSorts(), target.getSorts());
	}

	protected <V extends AbstractAsSortConfiguration, C1 extends AbstractAsSortConfiguration, C2 extends AbstractAsSortConfiguration> void mergeBefore(
			final Map<String, AsConfigurationHolder<V, AbstractAsSortConfiguration>> source,
			final Map<String, AsConfigurationHolder<V, AbstractAsSortConfiguration>> target,
			final Map<String, AsConfigurationHolder<C1, AbstractAsSortConfiguration>> cleanupTarget1,
			final Map<String, AsConfigurationHolder<C2, AbstractAsSortConfiguration>> cleanupTarget2)
	{
		if (MapUtils.isEmpty(source))
		{
			return;
		}

		final MergeMap<String, AsConfigurationHolder<V, AbstractAsSortConfiguration>> mergeSource = (MergeMap<String, AsConfigurationHolder<V, AbstractAsSortConfiguration>>) source;
		final MergeMap<String, AsConfigurationHolder<V, AbstractAsSortConfiguration>> mergeTarget = (MergeMap<String, AsConfigurationHolder<V, AbstractAsSortConfiguration>>) target;

		mergeTarget.mergeBefore(mergeSource, (key, replacedConfiguration, configuration) -> {
			final AsConfigurationHolder<V, AbstractAsSortConfiguration> newConfiguration = cloneConfigurationHolder(configuration);

			updateReplacedConfigurations(newConfiguration, cleanupTarget1.remove(key));
			updateReplacedConfigurations(newConfiguration, cleanupTarget2.remove(key));
			updateReplacedConfigurations(newConfiguration, replacedConfiguration);

			return newConfiguration;
		});
	}
}