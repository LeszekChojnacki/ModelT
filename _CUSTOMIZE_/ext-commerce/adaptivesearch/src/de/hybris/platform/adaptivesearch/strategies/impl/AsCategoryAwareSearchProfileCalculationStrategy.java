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
import de.hybris.platform.adaptivesearch.data.AsCategoryAwareSearchProfile;
import de.hybris.platform.adaptivesearch.data.AsConfigurableSearchConfiguration;
import de.hybris.platform.adaptivesearch.data.AsSearchProfileResult;
import de.hybris.platform.adaptivesearch.strategies.AsMergeStrategy;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileCalculationStrategy;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileResultFactory;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.PK;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Implementation of {@link AsSearchProfileCalculationStrategy} for category aware search profiles.
 *
 * @deprecated Since 1811, replaced by {@link AsGenericSearchProfileCalculationStrategy}
 */
@Deprecated
public class AsCategoryAwareSearchProfileCalculationStrategy
		extends AbstractAsSearchProfileCalculationStrategy<AsCategoryAwareSearchProfile>
{
	private AsSearchProfileResultFactory asSearchProfileResultFactory;
	private AsMergeStrategy asMergeStrategy;

	@Override
	public Serializable getCacheKeyFragment(final AsSearchProfileContext context, final AsCategoryAwareSearchProfile searchProfile)
	{
		if (CollectionUtils.isEmpty(context.getCategoryPath()))
		{
			return null;
		}

		final Map<PK, AsConfigurableSearchConfiguration> searchConfigurations = searchProfile.getSearchConfigurations();
		final ArrayList<Long> keyFragment = context.getCategoryPath().stream().map(CategoryModel::getPk)
				.filter(searchConfigurations::containsKey).map(PK::getLong).collect(Collectors.toCollection(ArrayList::new));

		return keyFragment;
	}

	@Override
	public AsSearchProfileResult calculate(final AsSearchProfileContext context, final AsCategoryAwareSearchProfile searchProfile)
	{
		final List<AsSearchProfileResult> results = new ArrayList<>();
		final Map<PK, AsConfigurableSearchConfiguration> searchConfigurations = searchProfile.getSearchConfigurations();

		final AsConfigurableSearchConfiguration globalSearchConfiguration = searchConfigurations.get(null);
		AsSearchProfileResult globalCategoryResult;

		if (globalSearchConfiguration == null)
		{
			globalCategoryResult = asSearchProfileResultFactory.createResult();
		}
		else
		{
			globalCategoryResult = asSearchProfileResultFactory.createResultFromSearchConfiguration(globalSearchConfiguration);
		}

		results.add(globalCategoryResult);

		if (!CollectionUtils.isEmpty(context.getCategoryPath()))
		{
			for (final CategoryModel category : context.getCategoryPath())
			{
				final AsConfigurableSearchConfiguration categorySearchConfiguration = searchConfigurations.get(category.getPk());
				if (categorySearchConfiguration != null)
				{
					results.add(asSearchProfileResultFactory.createResultFromSearchConfiguration(categorySearchConfiguration));
				}
			}
		}

		final AsSearchProfileResult mergeResult = asMergeStrategy.merge(context, results, null);

		// forces the merge result to use the merge modes from the global category, this defines how this will be merged with other profiles
		mergeResult.setFacetsMergeMode(globalCategoryResult.getFacetsMergeMode());
		mergeResult.setBoostItemsMergeMode(globalCategoryResult.getBoostItemsMergeMode());
		mergeResult.setBoostRulesMergeMode(globalCategoryResult.getBoostRulesMergeMode());
		mergeResult.setSortsMergeMode(globalCategoryResult.getSortsMergeMode());

		return mergeResult;
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

	public AsMergeStrategy getAsMergeStrategy()
	{
		return asMergeStrategy;
	}

	@Required
	public void setAsMergeStrategy(final AsMergeStrategy asMergeStrategy)
	{
		this.asMergeStrategy = asMergeStrategy;
	}
}
