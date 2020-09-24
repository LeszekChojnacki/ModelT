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

import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.UNIQUE_IDX_SEPARATOR;

import de.hybris.platform.adaptivesearch.model.AbstractAsSearchProfileModel;
import de.hybris.platform.adaptivesearch.model.AsCategoryAwareSearchConfigurationModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;

import org.apache.commons.lang3.StringUtils;


/**
 * Interceptor for {@link AsCategoryAwareSearchConfigurationModel}.
 */
public class AsCategoryAwareSearchConfigurationInterceptor extends AbstractAsInterceptor
		implements PrepareInterceptor<AsCategoryAwareSearchConfigurationModel>
{
	@Override
	public void onPrepare(final AsCategoryAwareSearchConfigurationModel searchConfiguration, final InterceptorContext context)
	{
		final String uniqueIdx = generateUniqueIdx(searchConfiguration, context);
		if (!StringUtils.equals(uniqueIdx, searchConfiguration.getUniqueIdx()))
		{
			searchConfiguration.setUniqueIdx(uniqueIdx);
		}
	}

	protected String generateUniqueIdx(final AsCategoryAwareSearchConfigurationModel searchConfiguration,
			final InterceptorContext context)
	{
		final AbstractAsSearchProfileModel searchProfile = resolveSearchProfile(searchConfiguration);

		return generateItemIdentifier(searchProfile, context) + UNIQUE_IDX_SEPARATOR
				+ generateItemIdentifier(searchConfiguration.getCategory(), context);
	}
}
