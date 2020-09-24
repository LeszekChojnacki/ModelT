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
package de.hybris.platform.solrfacetsearch.indexer;

import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedTypeModel;


/**
 * Resolves indexed type's unique code. Implementation should assure that indexed types codes remain unique within one
 * {@link SolrFacetSearchConfigModel}
 */
public interface SolrIndexedTypeCodeResolver
{

	/**
	 * Resolves unique solrIndexedType code
	 * 
	 * @return indexed type code
	 */
	String resolveIndexedTypeCode(SolrIndexedTypeModel solrIndexedType);
}
