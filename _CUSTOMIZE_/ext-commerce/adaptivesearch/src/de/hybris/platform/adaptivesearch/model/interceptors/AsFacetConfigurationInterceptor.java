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
package de.hybris.platform.adaptivesearch.model.interceptors;

import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.SEARCH_CONFIGURATION_ATTRIBUTE;
import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.SEARCH_PROFILE_ATTRIBUTE;
import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.UNIQUE_IDX_SEPARATOR;

import de.hybris.platform.adaptivesearch.model.AbstractAsFacetConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProvider;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.interceptor.RemoveInterceptor;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;


/**
 * Interceptor for {@link AbstractAsFacetConfigurationModel}.
 */
public class AsFacetConfigurationInterceptor extends AbstractAsInterceptor
		implements PrepareInterceptor<AbstractAsFacetConfigurationModel>, ValidateInterceptor<AbstractAsFacetConfigurationModel>,
		RemoveInterceptor<AbstractAsFacetConfigurationModel>
{
	@Override
	public void onPrepare(final AbstractAsFacetConfigurationModel facetConfiguration, final InterceptorContext context)
			throws InterceptorException
	{
		final String uniqueIdx = generateUniqueIdx(facetConfiguration, context);
		if (!StringUtils.equals(uniqueIdx, facetConfiguration.getUniqueIdx()))
		{
			facetConfiguration.setUniqueIdx(uniqueIdx);
		}

		markItemAsModified(context, facetConfiguration, SEARCH_CONFIGURATION_ATTRIBUTE, SEARCH_PROFILE_ATTRIBUTE);
	}

	protected String generateUniqueIdx(final AbstractAsFacetConfigurationModel facetConfiguration,
			final InterceptorContext context)
	{
		final AbstractAsSearchConfigurationModel searchConfiguration = resolveSearchConfiguration(facetConfiguration);

		return generateItemIdentifier(searchConfiguration, context) + UNIQUE_IDX_SEPARATOR
				+ decorateIdentifier(facetConfiguration.getIndexProperty());
	}

	@Override
	public void onValidate(final AbstractAsFacetConfigurationModel facetConfiguration, final InterceptorContext context)
			throws InterceptorException
	{
		final CatalogVersionModel catalogVersion = facetConfiguration.getCatalogVersion();
		final AbstractAsSearchConfigurationModel searchConfiguration = resolveAndValidateSearchConfiguration(facetConfiguration);

		if (searchConfiguration != null && !Objects.equals(catalogVersion, searchConfiguration.getCatalogVersion()))
		{
			throw new InterceptorException(
					"Invalid catalog version: " + catalogVersion.getCatalog() + ":" + catalogVersion.getVersion());
		}

		final AbstractAsSearchProfileModel searchProfile = resolveAndValidateSearchProfile(searchConfiguration);
		final String indexType = searchProfile.getIndexType();
		final String indexProperty = facetConfiguration.getIndexProperty();

		final AsSearchProvider searchProvider = resolveSearchProvider();

		if (!searchProvider.isValidFacetIndexProperty(indexType, indexProperty))
		{
			throw new InterceptorException("Index property cannot be used for facets: " + indexProperty);
		}
	}

	@Override
	public void onRemove(final AbstractAsFacetConfigurationModel facetConfiguration, final InterceptorContext context)
			throws InterceptorException
	{
		final AbstractAsSearchConfigurationModel searchConfiguration = resolveSearchConfiguration(facetConfiguration);
		markItemAsModified(context, searchConfiguration, SEARCH_PROFILE_ATTRIBUTE);
	}
}
