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

import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;
import de.hybris.platform.adaptivesearch.data.AsMergeConfiguration;
import de.hybris.platform.adaptivesearch.data.AsSearchProfileResult;
import de.hybris.platform.adaptivesearch.enums.AsBoostItemsMergeMode;
import de.hybris.platform.adaptivesearch.enums.AsBoostRulesMergeMode;
import de.hybris.platform.adaptivesearch.enums.AsFacetsMergeMode;
import de.hybris.platform.adaptivesearch.enums.AsSortsMergeMode;
import de.hybris.platform.adaptivesearch.strategies.AsBoostItemsMergeStrategy;
import de.hybris.platform.adaptivesearch.strategies.AsBoostRulesMergeStrategy;
import de.hybris.platform.adaptivesearch.strategies.AsFacetsMergeStrategy;
import de.hybris.platform.adaptivesearch.strategies.AsMergeStrategy;
import de.hybris.platform.adaptivesearch.strategies.AsMergeStrategyFactory;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileResultFactory;

import java.util.List;

import de.hybris.platform.adaptivesearch.strategies.AsSortsMergeStrategy;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


public class DefaultAsMergeStrategy implements AsMergeStrategy
{
	private AsSearchProfileResultFactory asSearchProfileResultFactory;
	private AsMergeStrategyFactory asMergeStrategyFactory;

	@Override
	public AsSearchProfileResult merge(final AsSearchProfileContext context, final List<AsSearchProfileResult> results, final AsMergeConfiguration mergeConfiguration)
	{
		if (CollectionUtils.isEmpty(results))
		{
			return asSearchProfileResultFactory.createResult();
		}

		if (results.size() == 1 && mergeConfiguration == null)
		{
			return results.get(0);
		}

		final AsSearchProfileResult mergeResult = asSearchProfileResultFactory.createResult();

		// sets the default merge modes in the result, can be changed by specific merge strategies
		final AsSearchProfileResult firstResult = results.get(0);

		mergeResult.setFacetsMergeMode(resolveResultFacetsMergeMode(firstResult, mergeConfiguration));
		mergeResult.setBoostItemsMergeMode(resolveResultBoostItemsMergeMode(firstResult, mergeConfiguration));
		mergeResult.setBoostRulesMergeMode(resolveResultBoostRulesMergeMode(firstResult, mergeConfiguration));
		mergeResult.setSortsMergeMode(resolveSortsMergeMode(firstResult, mergeConfiguration));

		for (final AsSearchProfileResult result : results)
		{
			final AsFacetsMergeStrategy facetsMergeStrategy = asMergeStrategyFactory
					.getFacetsMergeStrategy(resolveFacetsMergeMode(result, mergeConfiguration));
			facetsMergeStrategy.mergeFacets(result, mergeResult);

			final AsBoostItemsMergeStrategy boostItemsMergeStrategy = asMergeStrategyFactory
					.getBoostItemsMergeStrategy(resolveBoostItemsMergeMode(result, mergeConfiguration));
			boostItemsMergeStrategy.mergeBoostItems(result, mergeResult);

			final AsBoostRulesMergeStrategy boostRulesMergeStrategy = asMergeStrategyFactory
					.getBoostRulesMergeStrategy(resolveBoostRulesMergeMode(result, mergeConfiguration));
			boostRulesMergeStrategy.mergeBoostRules(result, mergeResult);

			final AsSortsMergeStrategy sortsMergeStrategy = asMergeStrategyFactory
					.getSortsMergeStrategy(resolveSortsMergeMode(result, mergeConfiguration));
			sortsMergeStrategy.mergeSorts(result, mergeResult);
		}

		return mergeResult;
	}

	protected AsFacetsMergeMode resolveResultFacetsMergeMode(final AsSearchProfileResult firstResult, final AsMergeConfiguration mergeConfiguration)
	{
		if (mergeConfiguration != null && mergeConfiguration.getResultFacetsMergeMode() != null)
		{
			return mergeConfiguration.getResultFacetsMergeMode();
		}

		return firstResult.getFacetsMergeMode();
	}

	protected AsBoostItemsMergeMode resolveResultBoostItemsMergeMode(final AsSearchProfileResult firstResult, final AsMergeConfiguration mergeConfiguration)
	{
		if (mergeConfiguration != null && mergeConfiguration.getResultBoostItemsMergeMode() != null)
		{
			return mergeConfiguration.getResultBoostItemsMergeMode();
		}

		return firstResult.getBoostItemsMergeMode();
	}

	protected AsBoostRulesMergeMode resolveResultBoostRulesMergeMode(final AsSearchProfileResult firstResult, final AsMergeConfiguration mergeConfiguration)
	{
		if (mergeConfiguration != null && mergeConfiguration.getResultBoostRulesMergeMode() != null)
		{
			return mergeConfiguration.getResultBoostRulesMergeMode();
		}

		return firstResult.getBoostRulesMergeMode();
	}

	protected AsFacetsMergeMode resolveFacetsMergeMode(final AsSearchProfileResult result, final AsMergeConfiguration mergeConfiguration)
	{
		if (mergeConfiguration != null && mergeConfiguration.getFacetsMergeMode() != null)
		{
			return mergeConfiguration.getFacetsMergeMode();
		}

		return result.getFacetsMergeMode();
	}

	protected AsBoostItemsMergeMode resolveBoostItemsMergeMode(final AsSearchProfileResult result, final AsMergeConfiguration mergeConfiguration)
	{
		if (mergeConfiguration != null && mergeConfiguration.getBoostItemsMergeMode() != null)
		{
			return mergeConfiguration.getBoostItemsMergeMode();
		}

		return result.getBoostItemsMergeMode();
	}

	protected AsBoostRulesMergeMode resolveBoostRulesMergeMode(final AsSearchProfileResult result, final AsMergeConfiguration mergeConfiguration)
	{
		if (mergeConfiguration != null && mergeConfiguration.getBoostRulesMergeMode() != null)
		{
			return mergeConfiguration.getBoostRulesMergeMode();
		}

		return result.getBoostRulesMergeMode();
	}

	protected AsSortsMergeMode resolveSortsMergeMode(final AsSearchProfileResult result, final AsMergeConfiguration mergeConfiguration)
	{
		if (mergeConfiguration != null && mergeConfiguration.getSortsMergeMode() != null)
		{
			return mergeConfiguration.getSortsMergeMode();
		}

		return result.getSortsMergeMode();
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

	public AsMergeStrategyFactory getAsMergeStrategyFactory()
	{
		return asMergeStrategyFactory;
	}

	@Required
	public void setAsMergeStrategyFactory(final AsMergeStrategyFactory asMergeStrategyFactory)
	{
		this.asMergeStrategyFactory = asMergeStrategyFactory;
	}
}
