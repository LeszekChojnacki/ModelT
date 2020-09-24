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

import de.hybris.platform.solrfacetsearch.enums.KeywordRedirectMatchType;
import de.hybris.platform.solrfacetsearch.model.redirect.SolrFacetSearchKeywordRedirectModel;
import de.hybris.platform.solrfacetsearch.search.KeywordRedirectSorter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Required;


public class DefaultKeywordRedirectSorter implements KeywordRedirectSorter
{

	private List<KeywordRedirectMatchType> sortOrder;
	private List<Comparator<SolrFacetSearchKeywordRedirectModel>> comparatorList;

	@Override
	public List<SolrFacetSearchKeywordRedirectModel> sort(final List<SolrFacetSearchKeywordRedirectModel> keywordRedirectList)
	{
		List<SolrFacetSearchKeywordRedirectModel> result;

		result = sortByComparators(keywordRedirectList);
		result = sortByMatchType(result);

		return result;
	}

	protected List<SolrFacetSearchKeywordRedirectModel> sortByMatchType(
			final List<SolrFacetSearchKeywordRedirectModel> keywordRedirectList)
	{
		final List<SolrFacetSearchKeywordRedirectModel> result = new ArrayList<SolrFacetSearchKeywordRedirectModel>();
		List<SolrFacetSearchKeywordRedirectModel> toSort = new ArrayList<SolrFacetSearchKeywordRedirectModel>();
		List<SolrFacetSearchKeywordRedirectModel> sorting = keywordRedirectList;

		for (final KeywordRedirectMatchType machType : sortOrder)
		{
			for (final SolrFacetSearchKeywordRedirectModel value : sorting)
			{
				if (value.getMatchType().equals(machType))
				{
					result.add(value);
				}
				else
				{
					toSort.add(value);
				}
			}
			sorting = toSort;
			toSort = new ArrayList<SolrFacetSearchKeywordRedirectModel>();
		}

		result.addAll(sorting);

		return result;
	}

	@Required
	public void setSortOrder(final List<KeywordRedirectMatchType> sortOrder)
	{
		this.sortOrder = sortOrder;
	}


	public void setComparatorList(final List<Comparator<SolrFacetSearchKeywordRedirectModel>> comparatorList)
	{
		this.comparatorList = comparatorList;
	}

	protected List<SolrFacetSearchKeywordRedirectModel> sortByComparators(
			final List<SolrFacetSearchKeywordRedirectModel> keywordRedirectList)
	{

		if (comparatorList != null && !comparatorList.isEmpty())
		{
			final List<SolrFacetSearchKeywordRedirectModel> result = new ArrayList<SolrFacetSearchKeywordRedirectModel>();
			result.addAll(keywordRedirectList);

			for (final Comparator<SolrFacetSearchKeywordRedirectModel> comparator : comparatorList)
			{
				Collections.sort(result, comparator);
			}
			return result;
		}
		else
		{
			return keywordRedirectList;
		}
	}

}
