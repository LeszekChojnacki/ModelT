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

import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurationModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;

import org.apache.commons.lang3.StringUtils;


/**
 * Interceptor for {@link AbstractAsConfigurationModel}.
 */

public class AsConfigurationInterceptor extends AbstractAsInterceptor implements PrepareInterceptor<AbstractAsConfigurationModel>
{
	@Override
	public void onPrepare(final AbstractAsConfigurationModel configuration, final InterceptorContext context)
			throws InterceptorException
	{
		if (context.isNew(configuration) && StringUtils.isBlank(configuration.getUid()))
		{
			configuration.setUid(generateUid());
		}
	}
}
