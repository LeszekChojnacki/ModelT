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
import de.hybris.platform.solrfacetsearch.model.SolrSearchQueryPropertyModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedTypeModel;
import de.hybris.platform.solrfacetsearch.solr.SolrIndexedPropertyTypeRegistry;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Required;


public class SolrSearchQueryPropertyValidateInterceptor implements ValidateInterceptor<SolrSearchQueryPropertyModel>
{
	private SolrIndexedPropertyTypeRegistry solrIndexedPropertyTypeRegistry;

	@Override
	public void onValidate(final SolrSearchQueryPropertyModel solrSearchQueryProperty, final InterceptorContext interceptorContext)
			throws InterceptorException
	{
		if (solrSearchQueryProperty.getSearchQueryTemplate() == null || solrSearchQueryProperty.getIndexedProperty() == null)
		{
			return;
		}

		final SolrIndexedTypeModel indexedType = solrSearchQueryProperty.getSearchQueryTemplate().getIndexedType();
		final SolrIndexedTypeModel solrIndexedType = solrSearchQueryProperty.getIndexedProperty().getSolrIndexedType();

		if (!Objects.equals(indexedType, solrIndexedType))
		{
			throw new InterceptorException("The indexed property " + solrSearchQueryProperty.getIndexedProperty().getName()
					+ "does not belong to the indexed type " + indexedType.getIdentifier());
		}

		if (solrSearchQueryProperty.isFacet() && !solrIndexedPropertyTypeRegistry
				.getIndexPropertyTypeInfo(solrSearchQueryProperty.getIndexedProperty().getType().getCode()).isAllowFacet())
		{
			throw new InterceptorException("The indexed property " + solrSearchQueryProperty.getIndexedProperty().getName()
					+ " is of type " + solrSearchQueryProperty.getIndexedProperty().getType().getCode() + " and cannot be facet.");
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
