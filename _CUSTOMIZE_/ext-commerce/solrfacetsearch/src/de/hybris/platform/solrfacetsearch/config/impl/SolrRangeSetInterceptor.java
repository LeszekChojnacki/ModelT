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
package de.hybris.platform.solrfacetsearch.config.impl;

import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;
import de.hybris.platform.solrfacetsearch.config.ValueRanges;
import de.hybris.platform.solrfacetsearch.model.config.SolrValueRangeSetModel;


public class SolrRangeSetInterceptor implements ValidateInterceptor
{

	@Override
	public void onValidate(final Object model, final InterceptorContext ctx) throws InterceptorException
	{
		if (model instanceof SolrValueRangeSetModel)
		{
			final String type = ((SolrValueRangeSetModel) model).getType();
			if (!ValueRanges.ALLOWEDTYPES.contains(type))
			{
				throw new InterceptorException("Possible range set types are : " + ValueRanges.getAllowedRangeTypes());
			}
		}
	}

}
