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
import de.hybris.platform.adaptivesearch.model.AsCategoryAwareSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AsCategoryAwareSearchProfileModel;
import de.hybris.platform.adaptivesearch.strategies.AsSearchConfigurationStrategy;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;


/**
 * Implementation of {@link AsSearchConfigurationStrategy} for category aware search profiles.
 */
public class AsCategoryAwareSearchConfigurationStrategy
		extends AbstractAsSearchConfigurationStrategy<AsCategoryAwareSearchProfileModel, AsCategoryAwareSearchConfigurationModel>
{
	protected static final String CONTEXT_TYPE_KEY = "adaptivesearch.categoryawaresearchprofile.contexttype";
	protected static final String CONTEXT_DESCRIPTION_KEY = "adaptivesearch.categoryawaresearchprofile.contextdescription";

	@Override
	public Optional<AsCategoryAwareSearchConfigurationModel> getForContext(final AsSearchProfileContext context,
			final AsCategoryAwareSearchProfileModel searchProfile)
	{
		final CategoryModel category = resolveCategory(context);

		final Map<String, Object> filters = new HashMap<>();
		filters.put(AsCategoryAwareSearchConfigurationModel.SEARCHPROFILE, searchProfile);
		filters.put(AsCategoryAwareSearchConfigurationModel.CATEGORY, category);

		final List<AsCategoryAwareSearchConfigurationModel> searchConfigurations = getAsSearchConfigurationDao()
				.findSearchConfigurations(AsCategoryAwareSearchConfigurationModel.class, filters);

		if (CollectionUtils.isEmpty(searchConfigurations))
		{
			return Optional.empty();
		}

		final List<AsCategoryAwareSearchConfigurationModel> validSearchConfigurations = searchConfigurations.stream()
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
	public AsCategoryAwareSearchConfigurationModel getOrCreateForContext(final AsSearchProfileContext context,
			final AsCategoryAwareSearchProfileModel searchProfile)
	{
		final Optional<AsCategoryAwareSearchConfigurationModel> searchConfiguration = getForContext(context, searchProfile);

		if (searchConfiguration.isPresent())
		{
			return searchConfiguration.get();
		}
		else
		{
			return createSearchConfiguration(context, searchProfile);
		}
	}

	protected AsCategoryAwareSearchConfigurationModel createSearchConfiguration(final AsSearchProfileContext context,
			final AsCategoryAwareSearchProfileModel searchProfile)
	{
		final CategoryModel category = resolveCategory(context);

		final AsCategoryAwareSearchConfigurationModel searchConfiguration = getModelService()
				.create(AsCategoryAwareSearchConfigurationModel.class);

		searchConfiguration.setSearchProfile(searchProfile);
		searchConfiguration.setCatalogVersion(searchProfile.getCatalogVersion());
		searchConfiguration.setCategory(category);

		return searchConfiguration;
	}

	@Override
	public AsSearchConfigurationInfoData getInfoForContext(final AsSearchProfileContext context,
			final AsCategoryAwareSearchProfileModel searchProfile)
	{
		final String contextType = getL10nService().getLocalizedString(CONTEXT_TYPE_KEY);
		final String contextLabel = buildContextLabel(context);
		final String contextDescription = getL10nService().getLocalizedString(CONTEXT_DESCRIPTION_KEY);

		final AsSearchConfigurationInfoData searchConfigurationInfo = new AsSearchConfigurationInfoData();
		searchConfigurationInfo.setType(AsCategoryAwareSearchProfileModel._TYPECODE);
		searchConfigurationInfo.setContextType(contextType);
		searchConfigurationInfo.setContextLabel(contextLabel);
		searchConfigurationInfo.setContextDescription(contextDescription);
		return searchConfigurationInfo;
	}

	protected String buildContextLabel(final AsSearchProfileContext context)
	{
		final String globalCategory = getL10nService().getLocalizedString(GLOBAL_CATEGORY_LABEL);

		final StringJoiner contextLabel = new StringJoiner(" / ");
		contextLabel.add(globalCategory);

		if (CollectionUtils.isNotEmpty(context.getCategoryPath()))
		{
			for (final CategoryModel category : context.getCategoryPath())
			{
				if (StringUtils.isNotBlank(category.getName()))
				{
					contextLabel.add(category.getName());
				}
				else
				{
					contextLabel.add("[" + category.getCode() + "]");
				}
			}
		}

		return contextLabel.toString();
	}

	protected CategoryModel resolveCategory(final AsSearchProfileContext context)
	{
		final List<CategoryModel> categoryPath = context.getCategoryPath();

		if (CollectionUtils.isNotEmpty(categoryPath))
		{
			return categoryPath.get(categoryPath.size() - 1);
		}

		return null;
	}

	@Override
	public Set<String> getQualifiers(final AsCategoryAwareSearchProfileModel searchProfile)
	{
		final Set<String> qualifiers = new HashSet<>();
		for (final AsCategoryAwareSearchConfigurationModel conf : searchProfile.getSearchConfigurations())
		{
			if (conf.getCategory() != null)
			{
				qualifiers.add(conf.getCategory().getCode());
			}
			else
			{
				qualifiers.add(null);
			}
		}
		return qualifiers;
	}
}
