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
import de.hybris.platform.adaptivesearch.enums.AsSortsMergeMode;
import de.hybris.platform.adaptivesearch.strategies.AsSortsMergeStrategy;
import de.hybris.platform.adaptivesearch.util.MergeMap;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;


/**
 * Default implementation of {@link AsSortsMergeStrategy} for REPLACE merge mode.
 */
public class AsSortsReplaceMergeStrategy extends AbstractAsSortsMergeStrategy
{
	@Override
	public void mergeSorts(final AsSearchProfileResult source, final AsSearchProfileResult target)
	{
		target.setSortsMergeMode(AsSortsMergeMode.REPLACE);

		replace(source.getPromotedSorts(), target.getPromotedSorts());
		replace(source.getSorts(), target.getSorts());
		replace(source.getExcludedSorts(), target.getExcludedSorts());
	}

	protected <V extends AbstractAsSortConfiguration> void replace(
			final Map<String, AsConfigurationHolder<V, AbstractAsSortConfiguration>> source,
			final Map<String, AsConfigurationHolder<V, AbstractAsSortConfiguration>> target)
	{
		target.clear();

		if (MapUtils.isEmpty(source))
		{
			return;
		}

		final MergeMap<String, AsConfigurationHolder<V, AbstractAsSortConfiguration>> mergeSource = (MergeMap<String, AsConfigurationHolder<V, AbstractAsSortConfiguration>>) source;
		final MergeMap<String, AsConfigurationHolder<V, AbstractAsSortConfiguration>> mergeTarget = (MergeMap<String, AsConfigurationHolder<V, AbstractAsSortConfiguration>>) target;

		mergeTarget.mergeAfter(mergeSource, (key, replacedConfiguration, configuration) -> cloneConfigurationHolder(configuration));
	}
}