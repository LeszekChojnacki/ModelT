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
package de.hybris.platform.solrfacetsearch.search.impl;

import de.hybris.platform.solrfacetsearch.model.redirect.SolrFacetSearchKeywordRedirectModel;

import java.util.Comparator;


public class KeywordLengthComparator implements Comparator<SolrFacetSearchKeywordRedirectModel>
{
	@Override
	public int compare(final SolrFacetSearchKeywordRedirectModel keywordRedirectModel1,
			final SolrFacetSearchKeywordRedirectModel keywordRedirectModel2)
	{
		int length_1 = 0;
		int length_2 = 0;

		if (keywordRedirectModel1.getKeyword() != null)
		{
			length_1 = keywordRedirectModel1.getKeyword().length();
		}

		if (keywordRedirectModel2.getKeyword() != null)
		{
			length_2 = keywordRedirectModel2.getKeyword().length();
		}


		if (length_1 < length_2)
		{
			return 1;
		}
		else if (length_1 > length_2)
		{
			return -1;
		}
		else
		{
			return 0;
		}
	}
}
