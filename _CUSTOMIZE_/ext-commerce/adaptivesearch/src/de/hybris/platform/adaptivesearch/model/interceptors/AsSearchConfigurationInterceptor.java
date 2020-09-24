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

import de.hybris.platform.adaptivesearch.model.AbstractAsSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.servicelayer.interceptor.RemoveInterceptor;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;

import java.util.Objects;

import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.SEARCH_PROFILE_ATTRIBUTE;


/**
 * Interceptor for {@link AbstractAsSearchConfigurationModel}.
 */
public class AsSearchConfigurationInterceptor extends AbstractAsInterceptor
		implements PrepareInterceptor<AbstractAsSearchConfigurationModel>, ValidateInterceptor<AbstractAsSearchConfigurationModel>,
		RemoveInterceptor<AbstractAsSearchConfigurationModel>
{
	@Override
	public void onPrepare(final AbstractAsSearchConfigurationModel searchConfiguration, final InterceptorContext context)
			throws InterceptorException
	{
		markItemAsModified(context, searchConfiguration, SEARCH_PROFILE_ATTRIBUTE);
	}

	@Override
	public void onValidate(final AbstractAsSearchConfigurationModel searchConfiguration, final InterceptorContext context)
			throws InterceptorException
	{
		final CatalogVersionModel catalogVersion = searchConfiguration.getCatalogVersion();
		final AbstractAsSearchProfileModel searchProfile = resolveAndValidateSearchProfile(searchConfiguration);

		if (searchProfile != null && !Objects.equals(catalogVersion, searchProfile.getCatalogVersion()))
		{
			throw new InterceptorException(
					"Invalid catalog version: " + catalogVersion.getCatalog() + ":" + catalogVersion.getVersion());
		}
	}

	@Override
	public void onRemove(final AbstractAsSearchConfigurationModel searchConfiguration, final InterceptorContext context)
			throws InterceptorException
	{
		final AbstractAsSearchProfileModel searchProfile = resolveAndValidateSearchProfile(searchConfiguration);
		markItemAsModified(context, searchProfile);
	}
}
