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

import de.hybris.platform.adaptivesearch.AsRuntimeException;
import de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants;
import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;
import de.hybris.platform.adaptivesearch.data.AsConfigurableSearchConfiguration;
import de.hybris.platform.adaptivesearch.data.AsGenericSearchProfile;
import de.hybris.platform.adaptivesearch.data.AsReference;
import de.hybris.platform.adaptivesearch.model.AsSimpleSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AsSimpleSearchProfileModel;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProfileLoadStrategy;
import de.hybris.platform.core.PK;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Implementation of {@link AsSearchProfileLoadStrategy} for simple search profiles.
 */
public class AsGenericSimpleSearchProfileLoadStrategy
		extends AbstractAsSearchProfileLoadStrategy<AsSimpleSearchProfileModel, AsGenericSearchProfile>
{
	protected static final String AVAILABLE_SEARCH_CONFIGURATIONS_QUERY = "SELECT {sc." + AsSimpleSearchConfigurationModel.PK
			+ "}, {sc." + AsSimpleSearchConfigurationModel.MODIFIEDTIME + "} FROM {" + AsSimpleSearchConfigurationModel._TYPECODE
			+ " AS sc} WHERE {sc." + AsSimpleSearchConfigurationModel.SEARCHPROFILE + "} = ?searchProfile";

	private FlexibleSearchService flexibleSearchService;
	private Converter<AsSimpleSearchProfileModel, AsGenericSearchProfile> asGenericSearchProfileConverter;

	@Override
	public AsGenericSearchProfile load(final AsSearchProfileContext context, final AsSimpleSearchProfileModel source)
	{
		final AsGenericSearchProfile target = asGenericSearchProfileConverter.convert(source);
		target.setQualifierType(null);
		target.setAvailableSearchConfigurations(buildAvailableSearchConfigurations(source));
		target.setSearchConfigurations(Collections.emptyMap());

		return target;
	}

	protected Map<String, AsReference> buildAvailableSearchConfigurations(final AsSimpleSearchProfileModel source)
	{
		final FlexibleSearchQuery searchQuery = new FlexibleSearchQuery(AVAILABLE_SEARCH_CONFIGURATIONS_QUERY,
				Collections.singletonMap("searchProfile", source));
		searchQuery.setResultClassList(Arrays.asList(PK.class, Date.class));

		final SearchResult<List<Object>> searchResult = flexibleSearchService.search(searchQuery);
		final int searchResultCount = searchResult.getCount();

		if (searchResultCount == 0)
		{
			return Collections.emptyMap();
		}
		else if (searchResultCount == 1)
		{
			final List<Object> values = searchResult.getResult().get(0);
			final PK pk = (PK) values.get(0);
			final long version = ((Date) values.get(1)).getTime();

			final AsReference searchConfigurationReference = new AsReference();
			searchConfigurationReference.setPk(pk);
			searchConfigurationReference.setVersion(version);

			return Collections.singletonMap(AdaptivesearchConstants.DEFAULT_QUALIFIER, searchConfigurationReference);
		}
		else
		{
			throw new AsRuntimeException(
					"Simple search profile with pk " + source.getPk() + " has more than one search configuration");
		}
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

	public Converter<AsSimpleSearchProfileModel, AsGenericSearchProfile> getAsGenericSearchProfileConverter()
	{
		return asGenericSearchProfileConverter;
	}

	@Required
	public void setAsGenericSearchProfileConverter(
			final Converter<AsSimpleSearchProfileModel, AsGenericSearchProfile> asGenericSearchProfileConverter)
	{
		this.asGenericSearchProfileConverter = asGenericSearchProfileConverter;
	}
}
