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
import de.hybris.platform.servicelayer.interceptor.LoadInterceptor;
import de.hybris.platform.solrfacetsearch.config.FacetSortProvider;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedPropertyModel;

import org.springframework.beans.factory.annotation.Required;


/**
 * Load interceptor for {@link SolrIndexedPropertyModel} which loads default {@link FacetSortProvider} name for the
 * indexed property in case no bean id was defined. The default bean id can be configured via spring:
 * {@link SolrIndexedPropertyLoadInterceptor#setDefaultFacetSortProvider(String)}
 */
public class SolrIndexedPropertyLoadInterceptor implements LoadInterceptor
{

	private String defaultFacetSortProvider;

	@Override
	public void onLoad(final Object model, final InterceptorContext ctx) throws InterceptorException
	{
		if (model instanceof SolrIndexedPropertyModel)
		{
			final SolrIndexedPropertyModel indexedProperty = (SolrIndexedPropertyModel) model;
			if (indexedProperty.getCustomFacetSortProvider() == null)
			{
				indexedProperty.setCustomFacetSortProvider(defaultFacetSortProvider);
			}
		}
	}

	@Required
	public void setDefaultFacetSortProvider(final String defaultFacetSortProvider)
	{
		this.defaultFacetSortProvider = defaultFacetSortProvider;
	}
}
