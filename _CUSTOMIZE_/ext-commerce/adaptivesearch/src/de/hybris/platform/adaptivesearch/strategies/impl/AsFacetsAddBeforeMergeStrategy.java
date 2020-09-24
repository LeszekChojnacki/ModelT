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

import de.hybris.platform.adaptivesearch.data.AbstractAsFacetConfiguration;
import de.hybris.platform.adaptivesearch.data.AsConfigurationHolder;
import de.hybris.platform.adaptivesearch.data.AsSearchProfileResult;
import de.hybris.platform.adaptivesearch.strategies.AsFacetsMergeStrategy;
import de.hybris.platform.adaptivesearch.util.MergeMap;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;


/**
 * Default implementation of {@link AsFacetsMergeStrategy} for ADD_BEFORE merge mode.
 */
public class AsFacetsAddBeforeMergeStrategy extends AbstractAsFacetsMergeStrategy
{
	@Override
	public void mergeFacets(final AsSearchProfileResult source, final AsSearchProfileResult target)
	{
		mergeBefore(source.getPromotedFacets(), target.getPromotedFacets(), target.getFacets(), target.getExcludedFacets());
		mergeBefore(source.getFacets(), target.getFacets(), target.getPromotedFacets(), target.getExcludedFacets());
		mergeBefore(source.getExcludedFacets(), target.getExcludedFacets(), target.getPromotedFacets(), target.getFacets());
	}

	protected <V extends AbstractAsFacetConfiguration, C1 extends AbstractAsFacetConfiguration, C2 extends AbstractAsFacetConfiguration> void mergeBefore(
			final Map<String, AsConfigurationHolder<V, AbstractAsFacetConfiguration>> source,
			final Map<String, AsConfigurationHolder<V, AbstractAsFacetConfiguration>> target,
			final Map<String, AsConfigurationHolder<C1, AbstractAsFacetConfiguration>> cleanupTarget1,
			final Map<String, AsConfigurationHolder<C2, AbstractAsFacetConfiguration>> cleanupTarget2)
	{
		if (MapUtils.isEmpty(source))
		{
			return;
		}

		final MergeMap<String, AsConfigurationHolder<V, AbstractAsFacetConfiguration>> mergeSource = (MergeMap<String, AsConfigurationHolder<V, AbstractAsFacetConfiguration>>) source;
		final MergeMap<String, AsConfigurationHolder<V, AbstractAsFacetConfiguration>> mergeTarget = (MergeMap<String, AsConfigurationHolder<V, AbstractAsFacetConfiguration>>) target;

		mergeTarget.mergeBefore(mergeSource, (key, replacedConfiguration, configuration) -> {
			final AsConfigurationHolder<V, AbstractAsFacetConfiguration> newConfiguration = cloneConfigurationHolder(configuration);

			updateReplacedConfigurations(newConfiguration, cleanupTarget1.remove(key));
			updateReplacedConfigurations(newConfiguration, cleanupTarget2.remove(key));
			updateReplacedConfigurations(newConfiguration, replacedConfiguration);

			return newConfiguration;
		});
	}
}