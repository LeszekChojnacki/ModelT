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

import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.PK;
import de.hybris.platform.solrfacetsearch.config.IndexedTypeSort;
import de.hybris.platform.solrfacetsearch.config.IndexedTypeSortField;
import de.hybris.platform.solrfacetsearch.constants.SolrfacetsearchConstants;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider.FieldType;
import de.hybris.platform.solrfacetsearch.search.FieldNameTranslator;
import de.hybris.platform.solrfacetsearch.search.OrderField;
import de.hybris.platform.solrfacetsearch.search.OrderField.SortOrder;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.impl.SearchQueryConverterData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.springframework.beans.factory.annotation.Required;


public class FacetSearchQuerySortsPopulator implements Populator<SearchQueryConverterData, SolrQuery>
{
	protected static final int BUFFER_SIZE = 256;
	protected static final float MAX_PROMOTED_RESULT_SCORE = 10000;
	protected static final String SOLR_SCORE_FIELD = "score";

	private FieldNameTranslator fieldNameTranslator;

	public FieldNameTranslator getFieldNameTranslator()
	{
		return fieldNameTranslator;
	}

	@Required
	public void setFieldNameTranslator(final FieldNameTranslator fieldNameTranslator)
	{
		this.fieldNameTranslator = fieldNameTranslator;
	}

	@Override
	public void populate(final SearchQueryConverterData source, final SolrQuery target)
	{
		final SearchQuery searchQuery = source.getSearchQuery();
		final List<PK> promotedItems = new ArrayList<>();
		final List<OrderField> sorts = new ArrayList<>();

		buildSorts(source, promotedItems, sorts);

		if(CollectionUtils.isNotEmpty(promotedItems))
		{
			final String promotedItemsSort = buildPromotedItemsSort(searchQuery);
			target.addSort(promotedItemsSort, ORDER.desc);
		}

		if(CollectionUtils.isNotEmpty(sorts))
		{
			for (final OrderField sort : sorts)
			{
				final String field = Objects.equals(sort.getField(), OrderField.SCORE) ? sort.getField()
						: fieldNameTranslator.translate(searchQuery, sort.getField(), FieldType.SORT);
				target.addSort(field, sort.isAscending() ? ORDER.asc : ORDER.desc);
			}
		}
		else
		{
			target.addSort(OrderField.SCORE, ORDER.desc);
		}
	}

	protected void buildSorts(final SearchQueryConverterData searchQueryConverterData, final List<PK> promotedItems, final List<OrderField> sorts)
	{
		final SearchQuery searchQuery = searchQueryConverterData.getSearchQuery();

		final IndexedTypeSort currentSort = searchQueryConverterData.getFacetSearchContext().getNamedSort();

		if (currentSort != null)
		{
			if(currentSort.isApplyPromotedItems())
			{
				promotedItems.addAll(searchQuery.getPromotedItems());
			}

			for (final IndexedTypeSortField indexedTypeSortField : currentSort.getFields())
			{
				sorts.add(new OrderField(indexedTypeSortField.getFieldName(),
						indexedTypeSortField.isAscending() ? SortOrder.ASCENDING : SortOrder.DESCENDING));
			}
		}
		else
		{
			promotedItems.addAll(searchQuery.getPromotedItems());
			sorts.addAll(searchQuery.getSorts());
		}
	}

	protected String buildPromotedItemsSort(final SearchQuery searchQuery)
	{
		final List<PK> promotedItems = searchQuery.getPromotedItems();
		if (CollectionUtils.isEmpty(promotedItems))
		{
			return StringUtils.EMPTY;
		}

		final StringBuilder query = new StringBuilder(BUFFER_SIZE);

		query.append("query({!v='");

		float score = MAX_PROMOTED_RESULT_SCORE;
		int index = 0;

		for (final PK promotedItem : promotedItems)
		{
			if (index != 0)
			{
				query.append(' ');
			}

			query.append(SolrfacetsearchConstants.PK_FIELD);
			query.append(':');
			query.append(promotedItem.getLongValueAsString());
			query.append("^=");
			query.append(score);

			score = Math.nextDown(score);
			index++;
		}

		query.append("'},1)");

		return query.toString();
	}
}
