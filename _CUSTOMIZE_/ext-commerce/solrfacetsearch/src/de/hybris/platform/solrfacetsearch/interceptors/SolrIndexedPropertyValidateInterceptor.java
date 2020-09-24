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
package de.hybris.platform.solrfacetsearch.interceptors;

import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedPropertyModel;
import de.hybris.platform.solrfacetsearch.solr.SolrIndexedPropertyTypeRegistry;

import org.springframework.beans.factory.annotation.Required;


/**
 * Validate interceptor for{@link SolrIndexedPropertyModel}. Checks if facet is of allowed type.
 */
public class SolrIndexedPropertyValidateInterceptor implements ValidateInterceptor<SolrIndexedPropertyModel>
{
	private SolrIndexedPropertyTypeRegistry solrIndexedPropertyTypeRegistry;

	@Override
	public void onValidate(final SolrIndexedPropertyModel model, final InterceptorContext interceptorContext)
			throws InterceptorException
	{
		if (model.isFacet() && !solrIndexedPropertyTypeRegistry.getIndexPropertyTypeInfo(model.getType().getCode()).isAllowFacet())
		{
			throw new InterceptorException(
					"The indexed property " + model.getName() + " is of type " + model.getType().getCode() + " and cannot be facet.");
		}
	}

	public SolrIndexedPropertyTypeRegistry getSolrIndexedPropertyTypeRegistry()
	{
		return solrIndexedPropertyTypeRegistry;
	}

	@Required
	public void setSolrIndexedPropertyTypeRegistry(final SolrIndexedPropertyTypeRegistry solrIndexedPropertyTypeRegistry)
	{
		this.solrIndexedPropertyTypeRegistry = solrIndexedPropertyTypeRegistry;
	}
}
