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
package com.hybris.backoffice.solrsearch.validators;

import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;

import java.util.Objects;
import java.util.Optional;

import com.hybris.backoffice.solrsearch.model.BackofficeIndexedTypeToSolrFacetSearchConfigModel;


public class BackofficeIndexedTypeToSolrFacetSearchConfigValidator implements
		ValidateInterceptor<BackofficeIndexedTypeToSolrFacetSearchConfigModel>
{
	@Override
	public void onValidate(final BackofficeIndexedTypeToSolrFacetSearchConfigModel config, final InterceptorContext ctx)
			throws InterceptorException
	{
		if (Objects.nonNull(config.getSolrFacetSearchConfig()) && Objects.nonNull(config.getIndexedType()))
		{
			final Optional<ComposedTypeModel> result = config.getSolrFacetSearchConfig().getSolrIndexedTypes().stream()
					.map(it -> (it.getType())).filter(ct -> (ct.equals(config.getIndexedType()))).findAny();
			if (!result.isPresent())
			{
				throw new InterceptorException("Configuration does not contain indexed type [" + config.getIndexedType().getCode()
						+ "]");
			}
		}
	}
}
