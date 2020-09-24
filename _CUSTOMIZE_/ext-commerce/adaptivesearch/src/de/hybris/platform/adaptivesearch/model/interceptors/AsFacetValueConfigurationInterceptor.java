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

import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.FACET_CONFIGURATION_ATTRIBUTE;
import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.SEARCH_CONFIGURATION_ATTRIBUTE;
import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.SEARCH_PROFILE_ATTRIBUTE;
import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.UNIQUE_IDX_SEPARATOR;

import de.hybris.platform.adaptivesearch.model.AbstractAsFacetConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AbstractAsFacetValueConfigurationModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.interceptor.RemoveInterceptor;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;


/**
 * Interceptor for {@link AbstractAsFacetConfigurationModel}.
 */
public class AsFacetValueConfigurationInterceptor extends AbstractAsInterceptor
		implements PrepareInterceptor<AbstractAsFacetValueConfigurationModel>,
		ValidateInterceptor<AbstractAsFacetValueConfigurationModel>, RemoveInterceptor<AbstractAsFacetValueConfigurationModel>
{
	@Override
	public void onPrepare(final AbstractAsFacetValueConfigurationModel facetValueConfiguration, final InterceptorContext context)
			throws InterceptorException
	{
		final String uniqueIdx = generateUniqueIdx(facetValueConfiguration, context);
		if (!StringUtils.equals(uniqueIdx, facetValueConfiguration.getUniqueIdx()))
		{
			facetValueConfiguration.setUniqueIdx(uniqueIdx);
		}

		markItemAsModified(context, facetValueConfiguration, FACET_CONFIGURATION_ATTRIBUTE, SEARCH_CONFIGURATION_ATTRIBUTE, SEARCH_PROFILE_ATTRIBUTE);
	}

	protected String generateUniqueIdx(final AbstractAsFacetValueConfigurationModel facetValueConfiguration,
			final InterceptorContext context)
	{
		final AbstractAsFacetConfigurationModel asFacetConfiguration = resolveFacetConfiguration(facetValueConfiguration);

		return generateItemIdentifier(asFacetConfiguration, context) + UNIQUE_IDX_SEPARATOR
				+ decorateIdentifier(facetValueConfiguration.getValue());
	}

	@Override
	public void onValidate(final AbstractAsFacetValueConfigurationModel facetValueConfiguration, final InterceptorContext context)
			throws InterceptorException
	{
		final CatalogVersionModel catalogVersion = facetValueConfiguration.getCatalogVersion();
		final AbstractAsFacetConfigurationModel facetConfiguration = resolveAndValidateFacetConfiguration(facetValueConfiguration);

		if (facetConfiguration != null && !Objects.equals(catalogVersion, facetConfiguration.getCatalogVersion()))
		{
			throw new InterceptorException(
					"Invalid catalog version: " + catalogVersion.getCatalog() + ":" + catalogVersion.getVersion());
		}

		final String value = facetValueConfiguration.getValue();

		if (StringUtils.isEmpty(value))
		{
			throw new InterceptorException("value does not exist: " + value);
		}
	}

	@Override
	public void onRemove(final AbstractAsFacetValueConfigurationModel facetValueConfiguration, final InterceptorContext context)
			throws InterceptorException
	{
		final AbstractAsFacetConfigurationModel facetConfiguration = resolveFacetConfiguration(facetValueConfiguration);
		markItemAsModified(context, facetConfiguration, SEARCH_CONFIGURATION_ATTRIBUTE, SEARCH_PROFILE_ATTRIBUTE);
	}

	protected AbstractAsFacetConfigurationModel resolveFacetConfiguration(final ItemModel model)
	{
		return getModelService().getAttributeValue(model, FACET_CONFIGURATION_ATTRIBUTE);
	}

	protected AbstractAsFacetConfigurationModel resolveAndValidateFacetConfiguration(final ItemModel model)
			throws InterceptorException
	{
		final Object facetConfiguration = getModelService().getAttributeValue(model, FACET_CONFIGURATION_ATTRIBUTE);

		if (!(facetConfiguration instanceof AbstractAsFacetConfigurationModel))
		{
			throw new InterceptorException("Invalid facet");
		}

		return (AbstractAsFacetConfigurationModel) facetConfiguration;
	}
}
