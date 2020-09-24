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

import de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants;
import de.hybris.platform.adaptivesearch.data.AbstractAsBoostItemConfiguration;
import de.hybris.platform.adaptivesearch.data.AbstractAsBoostRuleConfiguration;
import de.hybris.platform.adaptivesearch.data.AbstractAsFacetConfiguration;
import de.hybris.platform.adaptivesearch.data.AbstractAsSortConfiguration;
import de.hybris.platform.adaptivesearch.data.AsBoostRule;
import de.hybris.platform.adaptivesearch.data.AsConfigurableSearchConfiguration;
import de.hybris.platform.adaptivesearch.data.AsConfigurationHolder;
import de.hybris.platform.adaptivesearch.data.AsExcludedFacet;
import de.hybris.platform.adaptivesearch.data.AsExcludedItem;
import de.hybris.platform.adaptivesearch.data.AsExcludedSort;
import de.hybris.platform.adaptivesearch.data.AsFacet;
import de.hybris.platform.adaptivesearch.data.AsPromotedFacet;
import de.hybris.platform.adaptivesearch.data.AsPromotedItem;
import de.hybris.platform.adaptivesearch.data.AsPromotedSort;
import de.hybris.platform.adaptivesearch.data.AsSearchProfileResult;
import de.hybris.platform.adaptivesearch.data.AsSort;
import de.hybris.platform.adaptivesearch.enums.AsBoostItemsMergeMode;
import de.hybris.platform.adaptivesearch.enums.AsBoostRulesMergeMode;
import de.hybris.platform.adaptivesearch.enums.AsFacetsMergeMode;
import de.hybris.platform.adaptivesearch.enums.AsSortsMergeMode;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileResultFactory;
import de.hybris.platform.adaptivesearch.util.ConfigurationUtils;
import de.hybris.platform.adaptivesearch.util.MergeMap;
import de.hybris.platform.core.PK;
import de.hybris.platform.servicelayer.config.ConfigurationService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link AsSearchProfileResultFactory}
 */
public class DefaultAsSearchProfileResultFactory implements AsSearchProfileResultFactory
{
	protected static final FacetComparator FACET_COMPARATOR = new FacetComparator();
	protected static final SortComparator SORT_COMPARATOR = new SortComparator();

	private ConfigurationService configurationService;

	@Override
	public AsSearchProfileResult createResult()
	{
		final AsSearchProfileResult result = new AsSearchProfileResult();

		initializeFacets(result);
		initializeBoostItems(result);
		initializeBoostRules(result);
		initializeSorts(result);

		return result;
	}

	protected void initializeFacets(final AsSearchProfileResult result)
	{
		final AsFacetsMergeMode facetsMergeMode = AsFacetsMergeMode.valueOf(configurationService.getConfiguration()
				.getString(ConfigurationUtils.DEFAULT_FACETS_MERGE_MODE, AsFacetsMergeMode.ADD_AFTER.name()));
		result.setFacetsMergeMode(facetsMergeMode);

		result.setPromotedFacets(new DefaultMergeMap<>());
		result.setFacets(new DefaultMergeMap<>(FACET_COMPARATOR));
		result.setExcludedFacets(new DefaultMergeMap<>());
	}

	protected void initializeBoostItems(final AsSearchProfileResult result)
	{
		final AsBoostItemsMergeMode boostItemsMergeMode = AsBoostItemsMergeMode.valueOf(configurationService.getConfiguration()
				.getString(ConfigurationUtils.DEFAULT_BOOST_ITEMS_MERGE_MODE, AsBoostItemsMergeMode.ADD_AFTER.name()));
		result.setBoostItemsMergeMode(boostItemsMergeMode);

		result.setPromotedItems(new DefaultMergeMap<>());
		result.setExcludedItems(new DefaultMergeMap<>());
	}

	protected void initializeBoostRules(final AsSearchProfileResult result)
	{
		final AsBoostRulesMergeMode boostRulesMergeMode = AsBoostRulesMergeMode.valueOf(configurationService.getConfiguration()
				.getString(ConfigurationUtils.DEFAULT_BOOST_RULES_MERGE_MODE, AsBoostRulesMergeMode.ADD.name()));
		result.setBoostRulesMergeMode(boostRulesMergeMode);

		result.setBoostRules(new ArrayList<>());
	}

	protected void initializeSorts(final AsSearchProfileResult result)
	{
		final AsSortsMergeMode sortsMergetMode = AsSortsMergeMode.valueOf(configurationService.getConfiguration()
				.getString(ConfigurationUtils.DEFAULT_SORTS_MERGE_MODE, AsSortsMergeMode.ADD_AFTER.name()));
		result.setSortsMergeMode(sortsMergetMode);

		result.setPromotedSorts(new DefaultMergeMap<>());
		result.setSorts(new DefaultMergeMap<>(SORT_COMPARATOR));
		result.setExcludedSorts(new DefaultMergeMap<>());
	}

	@Override
	public AsSearchProfileResult createResultFromSearchConfiguration(final AsConfigurableSearchConfiguration searchConfiguration)
	{
		final AsSearchProfileResult result = createResult();

		populateFacetsFromSearchConfiguration(result, searchConfiguration);
		populateBoostItemsFromSearchConfiguration(result, searchConfiguration);
		populateBoostRulesFromSearchConfiguration(result, searchConfiguration);
		populateSortsFromSearchConfiguration(result, searchConfiguration);

		return result;
	}

	protected void populateFacetsFromSearchConfiguration(final AsSearchProfileResult result,
			final AsConfigurableSearchConfiguration searchConfiguration)
	{
		result.setFacetsMergeMode(searchConfiguration.getFacetsMergeMode());

		final MergeMap<String, AsConfigurationHolder<AsPromotedFacet, AbstractAsFacetConfiguration>> promotedFacets = (MergeMap<String, AsConfigurationHolder<AsPromotedFacet, AbstractAsFacetConfiguration>>) result
				.getPromotedFacets();
		for (final AsPromotedFacet promotedFacet : searchConfiguration.getPromotedFacets())
		{
			promotedFacets.mergeAfter(promotedFacet.getIndexProperty(), createConfigurationHolder(promotedFacet));
		}

		final MergeMap<String, AsConfigurationHolder<AsFacet, AbstractAsFacetConfiguration>> facets = (MergeMap<String, AsConfigurationHolder<AsFacet, AbstractAsFacetConfiguration>>) result
				.getFacets();
		for (final AsFacet facet : searchConfiguration.getFacets())
		{
			facets.mergeAfter(facet.getIndexProperty(), createConfigurationHolder(facet));
		}

		final MergeMap<String, AsConfigurationHolder<AsExcludedFacet, AbstractAsFacetConfiguration>> excludedFacets = (MergeMap<String, AsConfigurationHolder<AsExcludedFacet, AbstractAsFacetConfiguration>>) result
				.getExcludedFacets();
		for (final AsExcludedFacet excludedFacet : searchConfiguration.getExcludedFacets())
		{
			excludedFacets.mergeAfter(excludedFacet.getIndexProperty(), createConfigurationHolder(excludedFacet));
		}
	}

	protected void populateBoostItemsFromSearchConfiguration(final AsSearchProfileResult result,
			final AsConfigurableSearchConfiguration searchConfiguration)
	{
		result.setBoostItemsMergeMode(searchConfiguration.getBoostItemsMergeMode());

		final MergeMap<PK, AsConfigurationHolder<AsPromotedItem, AbstractAsBoostItemConfiguration>> promotedItems = (MergeMap<PK, AsConfigurationHolder<AsPromotedItem, AbstractAsBoostItemConfiguration>>) result
				.getPromotedItems();
		for (final AsPromotedItem promotedItem : searchConfiguration.getPromotedItems())
		{
			promotedItems.mergeAfter(promotedItem.getItemPk(), createConfigurationHolder(promotedItem));
		}

		final MergeMap<PK, AsConfigurationHolder<AsExcludedItem, AbstractAsBoostItemConfiguration>> excludedItems = (MergeMap<PK, AsConfigurationHolder<AsExcludedItem, AbstractAsBoostItemConfiguration>>) result
				.getExcludedItems();
		for (final AsExcludedItem excludedItem : searchConfiguration.getExcludedItems())
		{
			excludedItems.mergeAfter(excludedItem.getItemPk(), createConfigurationHolder(excludedItem));
		}
	}

	protected void populateBoostRulesFromSearchConfiguration(final AsSearchProfileResult result,
			final AsConfigurableSearchConfiguration searchConfiguration)
	{
		result.setBoostRulesMergeMode(searchConfiguration.getBoostRulesMergeMode());

		final List<AsConfigurationHolder<AsBoostRule, AbstractAsBoostRuleConfiguration>> boostRules = result.getBoostRules();
		for (final AsBoostRule boostRule : searchConfiguration.getBoostRules())
		{
			boostRules.add(createConfigurationHolder(boostRule));
		}
	}

	protected void populateSortsFromSearchConfiguration(final AsSearchProfileResult result,
			final AsConfigurableSearchConfiguration searchConfiguration)
	{
		result.setSortsMergeMode(searchConfiguration.getSortsMergeMode());

		final MergeMap<String, AsConfigurationHolder<AsPromotedSort, AbstractAsSortConfiguration>> promotedSorts = (MergeMap<String, AsConfigurationHolder<AsPromotedSort, AbstractAsSortConfiguration>>) result
				.getPromotedSorts();
		for (final AsPromotedSort promotedSort : searchConfiguration.getPromotedSorts())
		{
			promotedSorts.mergeAfter(promotedSort.getCode(), createConfigurationHolder(promotedSort));
		}

		final MergeMap<String, AsConfigurationHolder<AsSort, AbstractAsSortConfiguration>> sorts = (MergeMap<String, AsConfigurationHolder<AsSort, AbstractAsSortConfiguration>>) result
				.getSorts();
		for (final AsSort sort : searchConfiguration.getSorts())
		{
			sorts.mergeAfter(sort.getCode(), createConfigurationHolder(sort));
		}

		final MergeMap<String, AsConfigurationHolder<AsExcludedSort, AbstractAsSortConfiguration>> excludedSorts = (MergeMap<String, AsConfigurationHolder<AsExcludedSort, AbstractAsSortConfiguration>>) result
				.getExcludedSorts();
		for (final AsExcludedSort excludedSort : searchConfiguration.getExcludedSorts())
		{
			excludedSorts.mergeAfter(excludedSort.getCode(), createConfigurationHolder(excludedSort));
		}
	}

	@Override
	public <T, R> AsConfigurationHolder<T, R> createConfigurationHolder(final T configuration)
	{
		final AsConfigurationHolder<T, R> configurationHolder = new AsConfigurationHolder<>();
		configurationHolder.setConfiguration(configuration);
		configurationHolder.setReplacedConfigurations(new ArrayList<>());
		configurationHolder.setRank(0);

		return configurationHolder;
	}

	@Override
	public <T, R> AsConfigurationHolder<T, R> createConfigurationHolder(final T configuration, final Object data)
	{
		final AsConfigurationHolder<T, R> configurationHolder = new AsConfigurationHolder<>();
		configurationHolder.setConfiguration(configuration);
		configurationHolder.setReplacedConfigurations(new ArrayList<>());
		configurationHolder.setRank(0);
		configurationHolder.setData(data);

		return configurationHolder;
	}

	@Override
	public <T, R> AsConfigurationHolder<T, R> cloneConfigurationHolder(final AsConfigurationHolder<T, R> configurationHolder)
	{
		final AsConfigurationHolder<T, R> newConfigurationHolder = new AsConfigurationHolder<>();
		newConfigurationHolder.setConfiguration(configurationHolder.getConfiguration());
		newConfigurationHolder.setReplacedConfigurations(ObjectUtils.clone(configurationHolder.getReplacedConfigurations()));
		newConfigurationHolder.setRank(configurationHolder.getRank());
		newConfigurationHolder.setData(configurationHolder.getData());

		return newConfigurationHolder;
	}

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}

	protected static final class FacetComparator
			implements Comparator<AsConfigurationHolder<AsFacet, AbstractAsFacetConfiguration>>, Serializable
	{
		@Override
		public int compare(final AsConfigurationHolder<AsFacet, AbstractAsFacetConfiguration> configurationHolder1,
				final AsConfigurationHolder<AsFacet, AbstractAsFacetConfiguration> configurationHolder2)
		{
			final AsFacet facet1 = configurationHolder1.getConfiguration();
			final int priority1 = facet1.getPriority() != null ? facet1.getPriority().intValue()
					: AdaptivesearchConstants.DEFAULT_SORT_PRIORITY;

			final AsFacet facet2 = configurationHolder2.getConfiguration();
			final int priority2 = facet2.getPriority() != null ? facet2.getPriority().intValue()
					: AdaptivesearchConstants.DEFAULT_SORT_PRIORITY;

			return Integer.compare(priority2, priority1);
		}
	}

	protected static final class SortComparator
			implements Comparator<AsConfigurationHolder<AsSort, AbstractAsSortConfiguration>>, Serializable
	{
		@Override
		public int compare(final AsConfigurationHolder<AsSort, AbstractAsSortConfiguration> configurationHolder1,
				final AsConfigurationHolder<AsSort, AbstractAsSortConfiguration> configurationHolder2)
		{
			final AsSort sort1 = configurationHolder1.getConfiguration();
			final int priority1 = sort1.getPriority() != null ? sort1.getPriority().intValue()
					: AdaptivesearchConstants.DEFAULT_SORT_PRIORITY;

			final AsSort sort2 = configurationHolder2.getConfiguration();
			final int priority2 = sort2.getPriority() != null ? sort2.getPriority().intValue()
					: AdaptivesearchConstants.DEFAULT_SORT_PRIORITY;

			return Integer.compare(priority2, priority1);
		}
	}
}
