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

import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.search.FieldNameTranslator;
import de.hybris.platform.solrfacetsearch.search.OrderField;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.impl.SearchQueryConverterData;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.solrsearch.dataaccess.BackofficeSearchQuery;
import com.hybris.backoffice.solrsearch.dataaccess.SolrSearchCondition;


public class BackofficeSearchQuerySortsPopulator implements Populator<SearchQueryConverterData, SolrQuery>
{
	private FieldNamePostProcessor fieldNamePostProcessor;
	private FieldNameTranslator fieldNameTranslator;

	@Override
	public void populate(final SearchQueryConverterData source, final SolrQuery target) throws ConversionException
	{
		if (!(source.getSearchQuery() instanceof BackofficeSearchQuery))
		{
			return;
		}
		final BackofficeSearchQuery searchQuery = (BackofficeSearchQuery) source.getSearchQuery();

		for (final OrderField sort : searchQuery.getSorts())
		{
			if (isFieldLocalized(searchQuery, sort.getField()))
			{
				final List<SolrSearchCondition> conditions = searchQuery.getFieldConditions(sort.getField());
				if (CollectionUtils.isNotEmpty(conditions))
				{
					conditions.forEach(cdt -> addSortField(target, translate(sort.getField(), searchQuery, cdt), sort.isAscending()));
				}
				else
				{
					addSortField(target, translate(sort.getField(), searchQuery), sort.isAscending());
				}
			}
			else
			{
				addSortField(target, translate(sort.getField(), searchQuery), sort.isAscending());
			}
		}
	}

	protected String translate(final String field, final SearchQuery searchQuery, final SolrSearchCondition condition)
	{
		final String fieldName = translate(field, searchQuery);
		return OrderField.SCORE.equals(field) ? fieldName
				: fieldNamePostProcessor.process(searchQuery, condition.getLanguage(), fieldName);
	}

	protected String translate(final String field, final SearchQuery searchQuery)
	{
		return OrderField.SCORE.equals(field) ? field
				: fieldNameTranslator.translate(searchQuery, field, FieldNameProvider.FieldType.SORT);
	}

	protected void addSortField(final SolrQuery target, final String fieldName, final boolean isAscending)
	{
		target.addSort(fieldName, isAscending ? SolrQuery.ORDER.asc : SolrQuery.ORDER.desc);
	}

	protected boolean isFieldLocalized(final SearchQuery searchQuery, final String field)
	{
		final IndexedProperty indexedProperty = searchQuery.getIndexedType().getIndexedProperties().get(field);
		return indexedProperty != null && indexedProperty.isLocalized();
	}

	@Required
	public void setFieldNameTranslator(final FieldNameTranslator fieldNameTranslator)
	{
		this.fieldNameTranslator = fieldNameTranslator;
	}

	@Required
	public void setFieldNamePostProcessor(final FieldNamePostProcessor fieldNamePostProcessor)
	{
		this.fieldNamePostProcessor = fieldNamePostProcessor;
	}
}
