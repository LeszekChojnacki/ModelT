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

import de.hybris.platform.adaptivesearch.data.AsIndexTypeData;
import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.model.AsSearchProfileActivationSetModel;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProvider;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;

import java.util.Objects;
import java.util.Optional;


/**
 * Interceptor for {@link AbstractAsSearchProfileModel}.
 */
public class AsSearchProfileInterceptor extends AbstractAsInterceptor implements ValidateInterceptor<AbstractAsSearchProfileModel>
{
	@Override
	public void onValidate(final AbstractAsSearchProfileModel searchProfile, final InterceptorContext context)
			throws InterceptorException
	{
		final String indexType = searchProfile.getIndexType();
		final CatalogVersionModel catalogVersion = searchProfile.getCatalogVersion();

		final AsSearchProvider searchProvider = resolveSearchProvider();

		if (searchProvider != null)
		{
			final Optional<AsIndexTypeData> indexTypeDataOptional = searchProvider.getIndexTypeForCode(indexType);

			if (!indexTypeDataOptional.isPresent())
			{
				throw new InterceptorException("Index type does not exist: " + indexType);
			}
		}

		final AsSearchProfileActivationSetModel activationSet = searchProfile.getActivationSet();
		if (activationSet != null)
		{
			if (!Objects.equals(indexType, activationSet.getIndexType()))
			{
				throw new InterceptorException("Cannot assign search profile to an activation set that has a different index type: "
						+ indexType + " != " + activationSet.getIndexType());
			}

			if (!Objects.equals(catalogVersion, activationSet.getCatalogVersion()))
			{
				throw new InterceptorException(
						"Cannot assign search profile to an activation set that has a different catalogversion: " + catalogVersion
								+ " != " + activationSet.getCatalogVersion());
			}
		}
	}
}
