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
package de.hybris.platform.adaptivesearch.services.impl;

import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;
import de.hybris.platform.adaptivesearch.data.AbstractAsSearchProfile;
import de.hybris.platform.adaptivesearch.data.AsConfigurationHolder;
import de.hybris.platform.adaptivesearch.data.AsSearchProfileActivationGroup;
import de.hybris.platform.adaptivesearch.data.AsSearchProfileResult;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.services.AsSearchProfileCalculationService;
import de.hybris.platform.adaptivesearch.strategies.AsCacheKey;
import de.hybris.platform.adaptivesearch.strategies.AsCacheScope;
import de.hybris.platform.adaptivesearch.strategies.AsCacheStrategy;
import de.hybris.platform.adaptivesearch.strategies.AsMergeStrategy;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileCalculationStrategy;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileLoadStrategy;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileMapping;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileRegistry;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileResultFactory;
import de.hybris.platform.adaptivesearch.strategies.impl.DefaultAsCacheKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link AsSearchProfileCalculationService}.
 */
public class DefaultAsSearchProfileCalculationService implements AsSearchProfileCalculationService
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultAsSearchProfileCalculationService.class);

	private AsSearchProfileResultFactory asSearchProfileResultFactory;
	private AsSearchProfileRegistry asSearchProfileRegistry;
	private AsMergeStrategy asMergeStrategy;
	private AsCacheStrategy asCacheStrategy;

	@Override
	public AsSearchProfileResult createResult(final AsSearchProfileContext context)
	{
		return asSearchProfileResultFactory.createResult();
	}

	@Override
	public <T, R> AsConfigurationHolder<T, R> createConfigurationHolder(final AsSearchProfileContext context,
			final T configuration)
	{
		return asSearchProfileResultFactory.createConfigurationHolder(configuration);
	}

	@Override
	public <T, R> AsConfigurationHolder<T, R> createConfigurationHolder(final AsSearchProfileContext context,
			final T configuration, final Object data)
	{
		return asSearchProfileResultFactory.createConfigurationHolder(configuration, data);
	}

	@Override
	public AsSearchProfileResult calculate(final AsSearchProfileContext context,
			final List<AbstractAsSearchProfileModel> searchProfiles)
	{
		return calculate(context, null, searchProfiles);
	}

	@Override
	public AsSearchProfileResult calculate(final AsSearchProfileContext context, final AsSearchProfileResult result,
			final List<AbstractAsSearchProfileModel> searchProfiles)
	{
		final AsSearchProfileActivationGroup group = new AsSearchProfileActivationGroup();
		group.setSearchProfiles(searchProfiles);

		return calculateGroups(context, result, Collections.singletonList(group));
	}

	@Override
	public AsSearchProfileResult calculateGroups(final AsSearchProfileContext context,
			final List<AsSearchProfileActivationGroup> groups)
	{
		return calculateGroups(context, null, groups);
	}

	@Override
	public AsSearchProfileResult calculateGroups(final AsSearchProfileContext context, final AsSearchProfileResult result,
			final List<AsSearchProfileActivationGroup> groups)
	{
		final List<AsSearchProfileResult> searchProfileResults = new ArrayList<>();

		if (result != null)
		{
			searchProfileResults.add(result);
		}

		if (CollectionUtils.isNotEmpty(groups))
		{
			for (final AsSearchProfileActivationGroup group : groups)
			{
				searchProfileResults.add(doCalculateGroup(context, group));
			}
		}

		return asMergeStrategy.merge(context, searchProfileResults, null);
	}

	protected AsSearchProfileResult doCalculateGroup(final AsSearchProfileContext context,
			final AsSearchProfileActivationGroup group)
	{
		final List<AsSearchProfileResult> results = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(group.getSearchProfiles()))
		{
			for (final AbstractAsSearchProfileModel searchProfile : group.getSearchProfiles())
			{
				try
				{
					final AsSearchProfileMapping strategyMapping = asSearchProfileRegistry.getSearchProfileMapping(searchProfile);
					final Long baseKey = searchProfile.getPk().getLong();
					final Long baseKeyVersion = searchProfile.getModifiedtime().getTime();

					// load
					final AsSearchProfileLoadStrategy loadStrategy = strategyMapping.getLoadStrategy();
					final Serializable loadKeyFragment = loadStrategy.getCacheKeyFragment(context, searchProfile);
					final AsCacheKey loadKey = new DefaultAsCacheKey(AsCacheScope.LOAD, baseKey, baseKeyVersion, loadKeyFragment);
					final AbstractAsSearchProfile loadValue = asCacheStrategy.getWithLoader(loadKey,
							key -> loadStrategy.load(context, searchProfile));
					final AbstractAsSearchProfile loadStrategyData = loadStrategy.map(context, loadValue);

					// calculate
					final AsSearchProfileCalculationStrategy calculationStrategy = strategyMapping.getCalculationStrategy();
					final Serializable calculationKeyFragment = calculationStrategy.getCacheKeyFragment(context, loadStrategyData);
					final AsCacheKey calculationKey = new DefaultAsCacheKey(AsCacheScope.CALCULATION, baseKey, baseKeyVersion,
							calculationKeyFragment);
					final AsSearchProfileResult calculationValue = asCacheStrategy.getWithLoader(calculationKey,
							key -> calculationStrategy.calculate(context, loadStrategyData));
					final AsSearchProfileResult calculationStrategyData = calculationStrategy.map(context, calculationValue);

					results.add(calculationStrategyData);
				}
				catch (final Exception e)
				{
					// we ignore the search profile if an error occurs
					LOG.error("Could not process search profile with pk " + searchProfile.getPk(), e);
				}
			}
		}

		if (CollectionUtils.isNotEmpty(group.getGroups()))
		{
			for (final AsSearchProfileActivationGroup childGroup : group.getGroups())
			{
				results.add(doCalculateGroup(context, childGroup));
			}
		}

		// merge
		return asMergeStrategy.merge(context, results, group.getMergeConfiguration());
	}

	public AsSearchProfileResultFactory getAsSearchProfileResultFactory()
	{
		return asSearchProfileResultFactory;
	}

	@Required
	public void setAsSearchProfileResultFactory(final AsSearchProfileResultFactory asSearchProfileResultFactory)
	{
		this.asSearchProfileResultFactory = asSearchProfileResultFactory;
	}

	public AsSearchProfileRegistry getAsSearchProfileRegistry()
	{
		return asSearchProfileRegistry;
	}

	@Required
	public void setAsSearchProfileRegistry(final AsSearchProfileRegistry asSearchProfileRegistry)
	{
		this.asSearchProfileRegistry = asSearchProfileRegistry;
	}

	public AsMergeStrategy getAsMergeStrategy()
	{
		return asMergeStrategy;
	}

	@Required
	public void setAsMergeStrategy(final AsMergeStrategy asMergeStrategy)
	{
		this.asMergeStrategy = asMergeStrategy;
	}

	public AsCacheStrategy getAsCacheStrategy()
	{
		return asCacheStrategy;
	}

	@Required
	public void setAsCacheStrategy(final AsCacheStrategy asCacheStrategy)
	{
		this.asCacheStrategy = asCacheStrategy;
	}
}
