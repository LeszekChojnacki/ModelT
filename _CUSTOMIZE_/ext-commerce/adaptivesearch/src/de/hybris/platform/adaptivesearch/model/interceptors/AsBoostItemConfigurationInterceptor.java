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

import de.hybris.platform.adaptivesearch.model.AbstractAsBoostItemConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchConfigurationModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.interceptor.RemoveInterceptor;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;


/**
 * Interceptor for {@link AbstractAsBoostItemConfigurationModel}.
 */
public class AsBoostItemConfigurationInterceptor extends AbstractAsInterceptor
		implements PrepareInterceptor<AbstractAsBoostItemConfigurationModel>,
		ValidateInterceptor<AbstractAsBoostItemConfigurationModel>, RemoveInterceptor<AbstractAsBoostItemConfigurationModel>
{
	@Override
	public void onPrepare(final AbstractAsBoostItemConfigurationModel boostItemConfiguration, final InterceptorContext context)
			throws InterceptorException
	{
		final String uniqueIdx = generateUniqueIdx(boostItemConfiguration, context);
		if (!StringUtils.equals(uniqueIdx, boostItemConfiguration.getUniqueIdx()))
		{
			boostItemConfiguration.setUniqueIdx(uniqueIdx);
		}

		markItemAsModified(context, boostItemConfiguration, SEARCH_CONFIGURATION_ATTRIBUTE, SEARCH_PROFILE_ATTRIBUTE);
	}

	protected String generateUniqueIdx(final AbstractAsBoostItemConfigurationModel boostItemConfiguration,
			final InterceptorContext context)
	{
		final AbstractAsSearchConfigurationModel searchConfiguration = resolveSearchConfiguration(boostItemConfiguration);

		return generateItemIdentifier(searchConfiguration, context) + UNIQUE_IDX_SEPARATOR
				+ generateItemIdentifier(boostItemConfiguration.getItem(), context);
	}

	@Override
	public void onValidate(final AbstractAsBoostItemConfigurationModel boostItemConfiguration, final InterceptorContext context)
			throws InterceptorException
	{
		final CatalogVersionModel catalogVersion = boostItemConfiguration.getCatalogVersion();
		final AbstractAsSearchConfigurationModel searchConfiguration = resolveAndValidateSearchConfiguration(
				boostItemConfiguration);

		if (searchConfiguration != null && !Objects.equals(catalogVersion, searchConfiguration.getCatalogVersion()))
		{
			throw new InterceptorException(
					"Invalid catalog version: " + catalogVersion.getCatalog() + ":" + catalogVersion.getVersion());
		}
	}

	@Override
	public void onRemove(final AbstractAsBoostItemConfigurationModel boostItemConfiguration, final InterceptorContext context)
			throws InterceptorException
	{
		final AbstractAsSearchConfigurationModel searchConfiguration = resolveSearchConfiguration(boostItemConfiguration);
		markItemAsModified(context, searchConfiguration, SEARCH_PROFILE_ATTRIBUTE);
	}
}
