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

import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.UNIQUE_IDX_NULL_IDENTIFIER;
import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.UNIQUE_IDX_SEPARATOR;

import de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants;
import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;
import de.hybris.platform.adaptivesearch.data.AsConfigurableSearchConfiguration;
import de.hybris.platform.adaptivesearch.data.AsGenericSearchProfile;
import de.hybris.platform.adaptivesearch.data.AsReference;
import de.hybris.platform.adaptivesearch.model.AsCategoryAwareSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AsCategoryAwareSearchProfileModel;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileLoadStrategy;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.PK;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Implementation of {@link AsSearchProfileLoadStrategy} for category aware search profiles.
 */
public class AsGenericCategoryAwareSearchProfileLoadStrategy
		extends AbstractAsSearchProfileLoadStrategy<AsCategoryAwareSearchProfileModel, AsGenericSearchProfile>
{
	protected static final String AVAILABLE_SEARCH_CONFIGURATIONS_QUERY = "SELECT {sc."
			+ AsCategoryAwareSearchConfigurationModel.PK + "}, {sc." + AsCategoryAwareSearchConfigurationModel.MODIFIEDTIME
			+ "}, {sc." + AsCategoryAwareSearchConfigurationModel.UNIQUEIDX + "}, {c." + CategoryModel.CODE + "} " + "FROM {"
			+ AsCategoryAwareSearchConfigurationModel._TYPECODE + " AS sc LEFT JOIN " + CategoryModel._TYPECODE + " AS c ON {sc."
			+ AsCategoryAwareSearchConfigurationModel.CATEGORY + "} = {c." + CategoryModel.PK + "}} WHERE {sc."
			+ AsCategoryAwareSearchConfigurationModel.SEARCHPROFILE + "} = ?searchProfile";

	private FlexibleSearchService flexibleSearchService;
	private Converter<AsCategoryAwareSearchProfileModel, AsGenericSearchProfile> asGenericSearchProfileConverter;

	@Override
	public AsGenericSearchProfile load(final AsSearchProfileContext context, final AsCategoryAwareSearchProfileModel source)
	{
		final AsGenericSearchProfile target = asGenericSearchProfileConverter.convert(source);
		target.setQualifierType(AdaptivesearchConstants.CATEGORY_QUALIFIER_TYPE);
		target.setAvailableSearchConfigurations(buildAvailableSearchConfigurations(source));
		target.setSearchConfigurations(Collections.emptyMap());

		return target;
	}

	protected Map<String, AsReference> buildAvailableSearchConfigurations(final AsCategoryAwareSearchProfileModel source)
	{
		final FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(AVAILABLE_SEARCH_CONFIGURATIONS_QUERY,
				Collections.singletonMap("searchProfile", source));
		searchQuery.setResultClassList(Arrays.asList(PK.class, Date.class, String.class, String.class));

		final SearchResult<List<Object>> searchResult = flexibleSearchService.search(searchQuery);
		final int searchResultCount = searchResult.getCount();

		if (searchResultCount == 0)
		{
			return Collections.emptyMap();
		}

		final Map<String, AsReference> target = new HashMap<>();

		for (final List<Object> values : searchResult.getResult())
		{
			final PK pk = (PK) values.get(0);
			final long version = ((Date) values.get(1)).getTime();
			final String uniqueIdx = (String) values.get(2);
			final String qualifier = (String) values.get(3);

			if (isValidSearchConfiguration(qualifier, uniqueIdx))
			{
				final AsReference searchConfigurationReference = new AsReference();
				searchConfigurationReference.setPk(pk);
				searchConfigurationReference.setVersion(version);

				target.put(qualifier == null ? AdaptivesearchConstants.DEFAULT_QUALIFIER : qualifier, searchConfigurationReference);
			}
		}

		return target;
	}

	protected boolean isValidSearchConfiguration(final String qualifier, final String uniqueIdx)
	{
		if (qualifier != null)
		{
			return true;
		}

		return StringUtils.equals(UNIQUE_IDX_NULL_IDENTIFIER, StringUtils.substringAfterLast(uniqueIdx, UNIQUE_IDX_SEPARATOR));
	}

	@Override
	public AsGenericSearchProfile map(final AsSearchProfileContext context, final AsGenericSearchProfile source)
	{
		final Map<String, AsConfigurableSearchConfiguration> searchConfigurations = loadSearchConfigurations(context, source);
		if (MapUtils.isEmpty(searchConfigurations))
		{
			return source;
		}

		// creates a shallow copy
		final AsGenericSearchProfile target = new AsGenericSearchProfile();
		BeanUtils.copyProperties(source, target);

		target.setSearchConfigurations(searchConfigurations);

		return target;
	}

	public FlexibleSearchService getFlexibleSearchService()
	{
		return flexibleSearchService;
	}

	@Required
	public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService)
	{
		this.flexibleSearchService = flexibleSearchService;
	}

	public Converter<AsCategoryAwareSearchProfileModel, AsGenericSearchProfile> getAsGenericSearchProfileConverter()
	{
		return asGenericSearchProfileConverter;
	}

	@Required
	public void setAsGenericSearchProfileConverter(
			final Converter<AsCategoryAwareSearchProfileModel, AsGenericSearchProfile> asGenericSearchProfileConverter)
	{
		this.asGenericSearchProfileConverter = asGenericSearchProfileConverter;
	}
}
