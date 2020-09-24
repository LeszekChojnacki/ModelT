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
import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;
import de.hybris.platform.adaptivesearch.data.AsConfigurableSearchConfiguration;
import de.hybris.platform.adaptivesearch.data.AsGenericSearchProfile;
import de.hybris.platform.adaptivesearch.data.AsReference;
import de.hybris.platform.adaptivesearch.data.AsSearchProfileResult;
import de.hybris.platform.adaptivesearch.strategies.AsMergeStrategy;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileCalculationStrategy;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileResultFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Implementation of {@link AsSearchProfileCalculationStrategy} for generic search profiles.
 */
public class AsGenericSearchProfileCalculationStrategy extends AbstractAsSearchProfileCalculationStrategy<AsGenericSearchProfile>
{
	private AsSearchProfileResultFactory asSearchProfileResultFactory;
	private AsMergeStrategy asMergeStrategy;

	@Override
	public Serializable getCacheKeyFragment(final AsSearchProfileContext context, final AsGenericSearchProfile searchProfile)
	{
		final List<String> qualifiers = extractQualifiers(context, searchProfile);
		if (CollectionUtils.isEmpty(qualifiers))
		{
			return null;
		}

		final Map<String, AsReference> availableSearchConfigurations = searchProfile.getAvailableSearchConfigurations();
		final ArrayList<Long> keyFragment = new ArrayList<>();

		for (final String qualifier : qualifiers)
		{
			final AsReference searchConfigurationReference = availableSearchConfigurations.get(qualifier);
			keyFragment.add(searchConfigurationReference.getPk().getLong());
			keyFragment.add(searchConfigurationReference.getVersion());
		}

		return keyFragment;
	}

	@Override
	public AsSearchProfileResult calculate(final AsSearchProfileContext context, final AsGenericSearchProfile searchProfile)
	{
		final List<AsSearchProfileResult> results = new ArrayList<>();

		final Map<String, AsConfigurableSearchConfiguration> searchConfigurations = searchProfile.getSearchConfigurations();
		final AsConfigurableSearchConfiguration defaultSearchConfiguration = searchConfigurations
				.get(AdaptivesearchConstants.DEFAULT_QUALIFIER);
		AsSearchProfileResult defaultResult;

		if (defaultSearchConfiguration == null)
		{
			defaultResult = asSearchProfileResultFactory.createResult();
		}
		else
		{
			defaultResult = asSearchProfileResultFactory.createResultFromSearchConfiguration(defaultSearchConfiguration);
		}

		results.add(defaultResult);

		final List<String> qualifiers = extractQualifiers(context, searchProfile);
		if (CollectionUtils.isNotEmpty(qualifiers))
		{
			for (final String qualifier : qualifiers)
			{
				final AsConfigurableSearchConfiguration searchConfiguration = searchConfigurations.get(qualifier);
				if (searchConfiguration != null)
				{
					results.add(asSearchProfileResultFactory.createResultFromSearchConfiguration(searchConfiguration));
				}
			}
		}

		final AsSearchProfileResult result = asMergeStrategy.merge(context, results, null);

		// forces the merge result to use the merge modes from the global category, this defines how this will be merged with other profiles
		result.setFacetsMergeMode(defaultResult.getFacetsMergeMode());
		result.setBoostItemsMergeMode(defaultResult.getBoostItemsMergeMode());
		result.setBoostRulesMergeMode(defaultResult.getBoostRulesMergeMode());
		result.setSortsMergeMode(defaultResult.getSortsMergeMode());

		return result;
	}

	protected List<String> extractQualifiers(final AsSearchProfileContext context, final AsGenericSearchProfile searchProfile)
	{
		if (MapUtils.isEmpty(context.getQualifiers()) || StringUtils.isBlank(searchProfile.getQualifierType()))
		{
			return Collections.emptyList();
		}

		final List<String> qualifiers = context.getQualifiers().get(searchProfile.getQualifierType());
		if (CollectionUtils.isEmpty(qualifiers))
		{
			return Collections.emptyList();
		}

		final Map<String, AsReference> availableSearchConfigurations = searchProfile.getAvailableSearchConfigurations();
		return qualifiers.stream().filter(Objects::nonNull).filter(availableSearchConfigurations::containsKey)
				.collect(Collectors.toList());
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
