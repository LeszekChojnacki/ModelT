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
package de.hybris.platform.solrfacetsearch.search.impl.populators;

import de.hybris.platform.solrfacetsearch.config.FacetType;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.search.FacetField;
import de.hybris.platform.solrfacetsearch.search.FacetValueField;
import de.hybris.platform.solrfacetsearch.search.QueryField;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.SearchQuery.Operator;
import de.hybris.platform.solrfacetsearch.search.impl.SearchQueryConverterData;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.FacetParams;
import org.springframework.beans.factory.annotation.Required;


/**
 * Populates solr query facets
 */
public class FacetSearchQueryFacetsPopulator extends AbstractFacetSearchQueryPopulator
{
	public static final String DEFAULT_FACET_SORT = FacetParams.FACET_SORT_COUNT;
	public static final Integer DEFAULT_LIMIT = 50;
	public static final Integer MINIMUM_COUNT = 1;

	private String defaultFacetSort;

	public String getDefaultFacetSort()
	{
		return defaultFacetSort;
	}

	@Required
	public void setDefaultFacetSort(final String defaultFacetSort)
	{
		this.defaultFacetSort = defaultFacetSort;
	}

	@Override
	public void populate(final SearchQueryConverterData source, final SolrQuery target)
	{
		final SearchQuery searchQuery = source.getSearchQuery();
		final Map<String, FacetInfo> facetInfos = buildFacetInfos(searchQuery);

		int index = 0;

		for (final FacetInfo facetInfo : facetInfos.values())
		{
			final FacetField facetField = facetInfo.getFacetField();

			final String translatedField = getFieldNameTranslator().translate(searchQuery, facetField.getField(),
					FieldNameProvider.FieldType.INDEX);
			final FacetType facetType = facetField.getFacetType();

			if (CollectionUtils.isEmpty(facetInfo.getSelectedValues()))
			{
				target.addFacetField(translatedField);
			}
			else if (FacetType.REFINE.equals(facetType))
			{
				final QueryField facetFilterQuery = new QueryField(facetField.getField(), Operator.AND,
						facetInfo.getSelectedValues());
				final String filterQuery = convertQueryField(searchQuery, facetFilterQuery);

				target.addFilterQuery(filterQuery);
				target.addFacetField(translatedField);
			}
			else if (FacetType.MULTISELECTAND.equals(facetType))
			{
				final QueryField facetFilterQuery = new QueryField(facetField.getField(), Operator.AND,
						facetInfo.getSelectedValues());
				final String filterQuery = convertQueryField(searchQuery, facetFilterQuery);

				target.addFilterQuery("{!tag=fk" + index + "}" + filterQuery);
				target.addFacetField("{!ex=fk" + index + "}" + translatedField);
			}
			else if (FacetType.MULTISELECTOR.equals(facetType))
			{
				final QueryField facetFilterQuery = new QueryField(facetField.getField(), Operator.OR, facetInfo.getSelectedValues());
				final String filterQuery = convertQueryField(searchQuery, facetFilterQuery);

				target.addFilterQuery("{!tag=fk" + index + "}" + filterQuery);
				target.addFacetField("{!ex=fk" + index + "}" + translatedField);
			}
			else
			{
				final QueryField facetFilterQuery = new QueryField(facetField.getField(), Operator.AND,
						facetInfo.getSelectedValues());
				final String filterQuery = convertQueryField(searchQuery, facetFilterQuery);

				target.addFilterQuery(filterQuery);
				target.addFacetField(translatedField);
			}

			index++;
		}

		target.setFacetSort(resolveFacetSort());

		final String[] facetMincount = searchQuery.getRawParams().get(FacetParams.FACET_MINCOUNT);
		if (facetMincount == null || facetMincount.length == 0)
		{
			target.setFacetMinCount(MINIMUM_COUNT);
		}

		final String[] facetLimit = searchQuery.getRawParams().get(FacetParams.FACET_LIMIT);
		if (facetLimit == null || facetLimit.length == 0)
		{
			target.setFacetLimit(DEFAULT_LIMIT);
		}
	}

	protected Map<String, FacetInfo> buildFacetInfos(final SearchQuery searchQuery)
	{
		final Map<String, FacetInfo> facetInfos = new HashMap<>();

		for (final FacetField facet : searchQuery.getFacets())
		{
			final FacetInfo facetInfo = new FacetInfo(facet);
			facetInfos.put(facet.getField(), facetInfo);
		}

		for (final FacetValueField facetValue : searchQuery.getFacetValues())
		{
			FacetInfo facetInfo = facetInfos.get(facetValue.getField());
			if (facetInfo == null)
			{
				// in some cases filter queries are still sent as facet values
				// we create a fake FacetField to apply the filtering
				final FacetField facetField = new FacetField(facetValue.getField());

				facetInfo = new FacetInfo(facetField);
				facetInfos.put(facetValue.getField(), facetInfo);
			}

			if (CollectionUtils.isNotEmpty(facetValue.getValues()))
			{
				facetInfo.getSelectedValues().addAll(facetValue.getValues());
			}
		}

		return facetInfos;
	}

	protected String resolveFacetSort()
	{
		if (defaultFacetSort != null)
		{
			return defaultFacetSort;
		}

		return DEFAULT_FACET_SORT;
	}

	protected static class FacetInfo
	{
		private final FacetField facetField;
		private final Set<String> selectedValues;

		public FacetInfo(final FacetField facetField)
		{
			this.facetField = facetField;
			this.selectedValues = new LinkedHashSet<>();
		}

		public FacetField getFacetField()
		{
			return facetField;
		}

		public Set<String> getSelectedValues()
		{
			return selectedValues;
		}
	}
}
