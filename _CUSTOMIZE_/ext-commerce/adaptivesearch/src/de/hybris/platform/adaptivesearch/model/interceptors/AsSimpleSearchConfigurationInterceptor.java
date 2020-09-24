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

import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.model.AsSimpleSearchConfigurationModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;

import org.apache.commons.lang3.StringUtils;


/**
 * Interceptor for {@link AsSimpleSearchConfigurationModel}.
 */
public class AsSimpleSearchConfigurationInterceptor extends AbstractAsInterceptor
		implements PrepareInterceptor<AsSimpleSearchConfigurationModel>
{
	@Override
	public void onPrepare(final AsSimpleSearchConfigurationModel searchConfiguration, final InterceptorContext context)
			throws InterceptorException
	{
		final String uniqueIdx = generateUniqueIdx(searchConfiguration, context);
		if (!StringUtils.equals(uniqueIdx, searchConfiguration.getUniqueIdx()))
		{
			searchConfiguration.setUniqueIdx(uniqueIdx);
		}
	}

	protected String generateUniqueIdx(final AsSimpleSearchConfigurationModel searchConfiguration,
			final InterceptorContext context)
	{
		final AbstractAsSearchProfileModel searchProfile = resolveSearchProfile(searchConfiguration);

		return generateItemIdentifier(searchProfile, context);
	}
}
