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
package com.hybris.backoffice.solrsearch.populators;

import de.hybris.platform.solrfacetsearch.search.SearchQuery;

import java.util.Locale;


public interface FieldNamePostProcessor
{
	/**
	 * Uses locale for fieldName conversion
	 * @param searchQuery
	 * @param locale
	 * @param fieldName
	 * @return converted fieldName
	 */
	String process(final SearchQuery searchQuery, final Locale locale, final String fieldName);

}
