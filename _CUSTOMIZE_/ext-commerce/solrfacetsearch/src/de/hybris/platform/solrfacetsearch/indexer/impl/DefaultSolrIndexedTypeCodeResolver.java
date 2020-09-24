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
package de.hybris.platform.solrfacetsearch.indexer.impl;

import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.solrfacetsearch.indexer.SolrIndexedTypeCodeResolver;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedTypeModel;


/**
 * Default implementation of {@link SolrIndexedTypeCodeResolver} that resolves the code using pattern
 * "composedTypeCode_indexedName"
 */
public class DefaultSolrIndexedTypeCodeResolver implements SolrIndexedTypeCodeResolver
{

	@Override
	public String resolveIndexedTypeCode(final SolrIndexedTypeModel solrIndexedType)
	{
		ServicesUtil.validateParameterNotNull(solrIndexedType, "solrIndexedType must not be null");
		String uniqueCode = solrIndexedType.getType().getCode();
		if (solrIndexedType.getIndexName() != null)
		{
			uniqueCode = uniqueCode.concat("_").concat(solrIndexedType.getIndexName());
		}
		return uniqueCode;
	}

}
