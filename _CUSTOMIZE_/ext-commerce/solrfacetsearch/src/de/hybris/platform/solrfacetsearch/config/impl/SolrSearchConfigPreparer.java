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
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.solrfacetsearch.model.config.SolrSearchConfigModel;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;


/**
 * Prepare interceptor sets description value to [{@link SolrSearchConfigModel} based on page size and sort order
 */
public class SolrSearchConfigPreparer implements PrepareInterceptor
{

	@Override
	public void onPrepare(final Object model, final InterceptorContext ctx) throws InterceptorException
	{
		if (model instanceof SolrSearchConfigModel)
		{
			//we want to be able to reference the model by description in hmc.
			final SolrSearchConfigModel config = (SolrSearchConfigModel) model;
			if (StringUtils.isNotEmpty(config.getDescription()))
			{
				return;
			}

			String sortOrder = "";
			final String pageSize = "page size: " + config.getPageSize();

			if (CollectionUtils.isNotEmpty(config.getDefaultSortOrder()))
			{
				final String elements = String.join(", ", config.getDefaultSortOrder());
				sortOrder = "page size: " + elements;
			}

			config.setDescription(pageSize + ";  " + sortOrder);
		}
	}
}
