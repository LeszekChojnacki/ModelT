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
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider.FieldType;
import de.hybris.platform.solrfacetsearch.search.BoostField;
import de.hybris.platform.solrfacetsearch.search.FacetSearchQueryOperatorTranslator;
import de.hybris.platform.solrfacetsearch.search.FieldNameTranslator;
import de.hybris.platform.solrfacetsearch.search.QueryField;
import de.hybris.platform.solrfacetsearch.search.RawQuery;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.SearchQuery.Operator;
import de.hybris.platform.solrfacetsearch.search.impl.SearchQueryConverterData;
import de.hybris.platform.solrfacetsearch.solr.IndexedPropertyTypeInfo;
import de.hybris.platform.solrfacetsearch.solr.SolrIndexedPropertyTypeRegistry;
import de.hybris.platform.solrfacetsearch.solr.impl.SolrValueFormatUtils;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.springframework.beans.factory.annotation.Required;


public abstract class AbstractFacetSearchQueryPopulator implements Populator<SearchQueryConverterData, SolrQuery>
{
	private SearchQuery.Operator defaultOperator;
	private FieldNameTranslator fieldNameTranslator;
	private FacetSearchQueryOperatorTranslator facetSearchQueryOperatorTranslator;
	private SolrIndexedPropertyTypeRegistry solrIndexedPropertyTypeRegistry;

	public AbstractFacetSearchQueryPopulator()
	{
		defaultOperator = Operator.OR;
	}

	protected String convertQueryField(final SearchQuery searchQuery, final QueryField queryField)
	{
		final String convertedField = fieldNameTranslator.translate(searchQuery, queryField.getField(), FieldType.INDEX);

		final StringBuilder query = new StringBuilder();
		query.append(ClientUtils.escapeQueryChars(convertedField));
		query.append(':');

		if (queryField.getValues().size() == 1)
		{
			final String value = queryField.getValues().iterator().next();
			final String escapedValue = formatAndEscapeValue(searchQuery.getIndexedType(), queryField.getField(), value);
			final String convertedValue = facetSearchQueryOperatorTranslator.translate(escapedValue, queryField.getQueryOperator());

			query.append(convertedValue);
		}
		else
		{
			final List<String> convertedValues = new ArrayList<>(queryField.getValues().size());

			for (final String value : queryField.getValues())
			{
				final String escapedValue = formatAndEscapeValue(searchQuery.getIndexedType(), queryField.getField(), value);
				final String convertedValue = facetSearchQueryOperatorTranslator.translate(escapedValue,
						queryField.getQueryOperator());

				convertedValues.add(convertedValue);
			}

			final Operator operator = resolveQueryFieldOperator(searchQuery, queryField);
			final String separator = " " + operator + " ";
			final String convertedValue = StringUtils.join(convertedValues, separator);

			query.append('(');
			query.append(convertedValue);
			query.append(')');
		}

		return query.toString();
	}

	protected String convertBoostField(final SearchQuery searchQuery, final BoostField boostField)
	{
		final String convertedField = fieldNameTranslator.translate(searchQuery, boostField.getField(), FieldType.INDEX);

		final StringBuilder query = new StringBuilder();
		query.append(ClientUtils.escapeQueryChars(convertedField));
		query.append(':');

		final String value = String.valueOf(boostField.getValue());
		final String escapedValue = formatAndEscapeValue(searchQuery.getIndexedType(), boostField.getField(), value);
		final String convertedValue = facetSearchQueryOperatorTranslator.translate(escapedValue, boostField.getQueryOperator());

		query.append(convertedValue);

		query.append("^=");
		query.append(boostField.getBoostValue());

		return query.toString();
	}

	protected String convertRawQuery(final SearchQuery searchQuery, final RawQuery rawQuery)
	{
		final StringBuilder query = new StringBuilder();
		query.append('(');

		if (rawQuery.getField() != null)
		{
			final String convertedField = fieldNameTranslator.translate(searchQuery, rawQuery.getField(), FieldType.INDEX);
			query.append(convertedField);
			query.append(":(");
		}

		query.append(rawQuery.getQuery());

		if (rawQuery.getField() != null)
		{
			query.append(')');
		}

		query.append(')');

		return query.toString();
	}

	protected Operator resolveQueryFieldOperator(final SearchQuery searchQuery, final QueryField queryField)
	{
		if (queryField.getOperator() != null)
		{
			return queryField.getOperator();
		}

		return resolveOperator(searchQuery);
	}

	protected Operator resolveOperator(final SearchQuery searchQuery)
	{
		if (searchQuery.getDefaultOperator() != null)
		{
			return searchQuery.getDefaultOperator();
		}

		return getDefaultOperator();
	}

	protected String formatAndEscapeValue(final IndexedType indexedType, final String field, final String value)
	{
		final IndexedProperty indexedProperty = indexedType.getIndexedProperties().get(field);
		if (indexedProperty != null && MapUtils.isEmpty(indexedProperty.getValueRangeSets()))
		{
			final IndexedPropertyTypeInfo indexedPropertyTypeInfo = solrIndexedPropertyTypeRegistry
					.getIndexPropertyTypeInfo(indexedProperty.getType());
			if (indexedPropertyTypeInfo != null)
			{
				final String formattedValue = SolrValueFormatUtils.format(value, indexedPropertyTypeInfo.getJavaType());
				return ClientUtils.escapeQueryChars(formattedValue);
			}
		}

		return ClientUtils.escapeQueryChars(value);
	}

	public SearchQuery.Operator getDefaultOperator()
	{
		return defaultOperator;
	}

	@Required
	public void setDefaultOperator(final SearchQuery.Operator defaultOperator)
	{
		this.defaultOperator = defaultOperator;
	}

	public FieldNameTranslator getFieldNameTranslator()
	{
		return fieldNameTranslator;
	}

	@Required
	public void setFieldNameTranslator(final FieldNameTranslator fieldNameTranslator)
	{
		this.fieldNameTranslator = fieldNameTranslator;
	}

	public FacetSearchQueryOperatorTranslator getFacetSearchQueryOperatorTranslator()
	{
		return facetSearchQueryOperatorTranslator;
	}

	@Required
	public void setFacetSearchQueryOperatorTranslator(final FacetSearchQueryOperatorTranslator facetSearchQueryOperatorTranslator)
	{
		this.facetSearchQueryOperatorTranslator = facetSearchQueryOperatorTranslator;
	}

	public SolrIndexedPropertyTypeRegistry getSolrIndexedPropertyTypeRegistry()
	{
		return solrIndexedPropertyTypeRegistry;
	}

	@Required
	public void setSolrIndexedPropertyTypeRegistry(final SolrIndexedPropertyTypeRegistry solrIndexedPropertyTypeRegistry)
	{
		this.solrIndexedPropertyTypeRegistry = solrIndexedPropertyTypeRegistry;
	}
}
