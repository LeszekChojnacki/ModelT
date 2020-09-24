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
import de.hybris.platform.adaptivesearch.enums.AsFacetsMergeMode;
import de.hybris.platform.adaptivesearch.strategies.AsFacetsMergeStrategy;
import de.hybris.platform.adaptivesearch.util.MergeMap;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;


/**
 * Default implementation of {@link AsFacetsMergeStrategy} for REPLACE merge mode.
 */
public class AsFacetsReplaceMergeStrategy extends AbstractAsFacetsMergeStrategy
{
	@Override
	public void mergeFacets(final AsSearchProfileResult source, final AsSearchProfileResult target)
	{
		target.setFacetsMergeMode(AsFacetsMergeMode.REPLACE);

		replace(source.getPromotedFacets(), target.getPromotedFacets());
		replace(source.getFacets(), target.getFacets());
		replace(source.getExcludedFacets(), target.getExcludedFacets());
	}

	protected <V extends AbstractAsFacetConfiguration> void replace(
			final Map<String, AsConfigurationHolder<V, AbstractAsFacetConfiguration>> source,
			final Map<String, AsConfigurationHolder<V, AbstractAsFacetConfiguration>> target)
	{
		target.clear();

		if (MapUtils.isEmpty(source))
		{
			return;
		}

		final MergeMap<String, AsConfigurationHolder<V, AbstractAsFacetConfiguration>> mergeSource = (MergeMap<String, AsConfigurationHolder<V, AbstractAsFacetConfiguration>>) source;
		final MergeMap<String, AsConfigurationHolder<V, AbstractAsFacetConfiguration>> mergeTarget = (MergeMap<String, AsConfigurationHolder<V, AbstractAsFacetConfiguration>>) target;

		mergeTarget.mergeAfter(mergeSource, (key, replacedConfiguration, configuration) -> cloneConfigurationHolder(configuration));
	}
}