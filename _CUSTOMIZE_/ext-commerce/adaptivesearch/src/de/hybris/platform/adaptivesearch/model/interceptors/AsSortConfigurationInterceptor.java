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

import de.hybris.platform.adaptivesearch.model.AbstractAsSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AbstractAsSortConfigurationModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.interceptor.RemoveInterceptor;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;


/**
 * Interceptor for {@link AbstractAsSortConfigurationModel}.
 */
public class AsSortConfigurationInterceptor extends AbstractAsInterceptor
		implements PrepareInterceptor<AbstractAsSortConfigurationModel>, ValidateInterceptor<AbstractAsSortConfigurationModel>,
		RemoveInterceptor<AbstractAsSortConfigurationModel>
{
	@Override
	public void onPrepare(final AbstractAsSortConfigurationModel sortConfiguration, final InterceptorContext context)
			throws InterceptorException
	{
		final String uniqueIdx = generateUniqueIdx(sortConfiguration, context);
		if (!StringUtils.equals(uniqueIdx, sortConfiguration.getUniqueIdx()))
		{
			sortConfiguration.setUniqueIdx(uniqueIdx);
		}

		markItemAsModified(context, sortConfiguration, SEARCH_CONFIGURATION_ATTRIBUTE, SEARCH_PROFILE_ATTRIBUTE);
	}

	protected String generateUniqueIdx(final AbstractAsSortConfigurationModel sortConfiguration, final InterceptorContext context)
	{
		final AbstractAsSearchConfigurationModel searchConfiguration = resolveSearchConfiguration(sortConfiguration);

		return generateItemIdentifier(searchConfiguration, context) + UNIQUE_IDX_SEPARATOR
				+ decorateIdentifier(sortConfiguration.getCode());
	}

	@Override
	public void onValidate(final AbstractAsSortConfigurationModel sortConfiguration, final InterceptorContext context)
			throws InterceptorException
	{
		final CatalogVersionModel catalogVersion = sortConfiguration.getCatalogVersion();
		final AbstractAsSearchConfigurationModel searchConfiguration = resolveAndValidateSearchConfiguration(sortConfiguration);

		if (searchConfiguration != null && !Objects.equals(catalogVersion, searchConfiguration.getCatalogVersion()))
		{
			throw new InterceptorException(
					"Invalid catalog version: " + catalogVersion.getCatalog() + ":" + catalogVersion.getVersion());
		}
	}

	@Override
	public void onRemove(final AbstractAsSortConfigurationModel sortConfiguration, final InterceptorContext context)
			throws InterceptorException
	{
		final AbstractAsSearchConfigurationModel searchConfiguration = resolveSearchConfiguration(sortConfiguration);
		markItemAsModified(context, searchConfiguration, SEARCH_PROFILE_ATTRIBUTE);
	}
}
