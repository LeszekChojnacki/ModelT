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
import de.hybris.platform.solrfacetsearch.model.SolrSearchQueryTemplateModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedTypeModel;

import java.util.Objects;

public class SolrSearchQueryTemplateValidateInterceptor implements ValidateInterceptor<SolrSearchQueryTemplateModel>
{

	@Override
	public void onValidate(final SolrSearchQueryTemplateModel solrSearchQueryTemplate, final InterceptorContext interceptorContext) throws InterceptorException
	{
		if (solrSearchQueryTemplate.getGroupProperty() == null || solrSearchQueryTemplate.getIndexedType() == null)
		{
			return;
		}

		final SolrIndexedTypeModel groupIndexedType = solrSearchQueryTemplate.getGroupProperty().getSolrIndexedType();
		final SolrIndexedTypeModel indexedType = solrSearchQueryTemplate.getIndexedType();

		if (!Objects.equals(groupIndexedType, indexedType))
		{
			throw new InterceptorException(
					"The group property " + solrSearchQueryTemplate.getGroupProperty().getName() + " does note belong to the indexed type: " + indexedType.getIdentifier());
		}
	}
}
