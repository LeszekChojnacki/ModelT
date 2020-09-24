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
package com.hybris.backoffice.solrsearch.cache;

import com.hybris.backoffice.solrsearch.model.BackofficeIndexedTypeToSolrFacetSearchConfigModel;


/**
 * Cache containing <code>BackofficeIndexedTypeToSolrFacetSearchConfig</code>s for typecodes. Should also cache <code>null</code>
 * values if no mapping is configured for given type code.
 */
public interface BackofficeFacetSearchConfigCache
{

	boolean containsSearchConfigForTypeCode(final String typeCode);

	BackofficeIndexedTypeToSolrFacetSearchConfigModel getSearchConfigForTypeCode(final String typeCode);

	void putSearchConfigForTypeCode(final String typeCode, final BackofficeIndexedTypeToSolrFacetSearchConfigModel searchConfig);

	void invalidateCache();

}
