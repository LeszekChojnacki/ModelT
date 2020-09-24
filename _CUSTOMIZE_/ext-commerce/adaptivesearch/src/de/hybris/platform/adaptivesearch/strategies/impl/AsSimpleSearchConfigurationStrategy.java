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

import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.GLOBAL_CATEGORY_LABEL;

import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;
import de.hybris.platform.adaptivesearch.data.AsSearchConfigurationInfoData;
import de.hybris.platform.adaptivesearch.model.AsSimpleSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AsSimpleSearchProfileModel;
import de.hybris.platform.adaptivesearch.strategies.AsSearchConfigurationStrategy;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;


/**
 * Implementation of {@link AsSearchConfigurationStrategy} for simple search profiles.
 */
public class AsSimpleSearchConfigurationStrategy
		extends AbstractAsSearchConfigurationStrategy<AsSimpleSearchProfileModel, AsSimpleSearchConfigurationModel>
{
	protected static final String CONTEXT_TYPE_KEY = "adaptivesearch.simplesearchprofile.contexttype";
	protected static final String CONTEXT_DESCRIPTION_KEY = "adaptivesearch.simplesearchprofile.contextdescription";

	@Override
	public Optional<AsSimpleSearchConfigurationModel> getForContext(final AsSearchProfileContext context,
			final AsSimpleSearchProfileModel searchProfile)
	{
		final Map<String, Object> filters = new HashMap<>();
		filters.put(AsSimpleSearchConfigurationModel.SEARCHPROFILE, searchProfile);

		final List<AsSimpleSearchConfigurationModel> searchConfigurations = getAsSearchConfigurationDao()
				.findSearchConfigurations(AsSimpleSearchConfigurationModel.class, filters);

		if (CollectionUtils.isEmpty(searchConfigurations))
		{
			return Optional.empty();
		}

		final List<AsSimpleSearchConfigurationModel> validSearchConfigurations = searchConfigurations.stream()
				.filter(searchConfiguration -> !searchConfiguration.isCorrupted()).collect(Collectors.toList());

		if (CollectionUtils.isEmpty(validSearchConfigurations))
		{
			return Optional.empty();
		}

		if (validSearchConfigurations.size() > 1)
		{
			throw new AmbiguousIdentifierException("More than one search configuration found");
		}

		return Optional.of(validSearchConfigurations.get(0));
	}

	@Override
	public AsSimpleSearchConfigurationModel getOrCreateForContext(final AsSearchProfileContext context,
			final AsSimpleSearchProfileModel searchProfile)
	{
		final Optional<AsSimpleSearchConfigurationModel> searchConfiguration = getForContext(context, searchProfile);

		if (searchConfiguration.isPresent())
		{
			return searchConfiguration.get();
		}
		else
		{
			return createSearchConfiguration(searchProfile);
		}
	}

	@Override
	public AsSearchConfigurationInfoData getInfoForContext(final AsSearchProfileContext context,
			final AsSimpleSearchProfileModel searchProfile)
	{
		final String contextType = getL10nService().getLocalizedString(CONTEXT_TYPE_KEY);
		final String contextLabel = buildContextLabel();
		final String contextDescription = getL10nService().getLocalizedString(CONTEXT_DESCRIPTION_KEY);

		final AsSearchConfigurationInfoData searchConfigurationInfo = new AsSearchConfigurationInfoData();
		searchConfigurationInfo.setType(AsSimpleSearchProfileModel._TYPECODE);
		searchConfigurationInfo.setContextType(contextType);
		searchConfigurationInfo.setContextLabel(contextLabel);
		searchConfigurationInfo.setContextDescription(contextDescription);
		return searchConfigurationInfo;
	}

	protected String buildContextLabel()
	{
		return getL10nService().getLocalizedString(GLOBAL_CATEGORY_LABEL);
	}

	protected AsSimpleSearchConfigurationModel createSearchConfiguration(final AsSimpleSearchProfileModel searchProfile)
	{
		final AsSimpleSearchConfigurationModel searchConfiguration = getModelService()
				.create(AsSimpleSearchConfigurationModel.class);

		searchConfiguration.setSearchProfile(searchProfile);
		searchConfiguration.setCatalogVersion(searchProfile.getCatalogVersion());

		return searchConfiguration;
	}

	@Override
	public Set<String> getQualifiers(final AsSimpleSearchProfileModel searchProfile)
	{
		final Set<String> set = new HashSet<>();
		set.add(null);
		return set;
	}
}
