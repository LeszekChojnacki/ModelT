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


import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider.FieldType;
import de.hybris.platform.solrfacetsearch.search.BoostField;
import de.hybris.platform.solrfacetsearch.search.CoupledQueryField;
import de.hybris.platform.solrfacetsearch.search.FreeTextQueryBuilder;
import de.hybris.platform.solrfacetsearch.search.FreeTextQueryBuilderFactory;
import de.hybris.platform.solrfacetsearch.search.QueryField;
import de.hybris.platform.solrfacetsearch.search.RawQuery;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.SearchQuery.Operator;
import de.hybris.platform.solrfacetsearch.search.impl.SearchQueryConverterData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Populates basic data of solr query
 */
public class FacetSearchQueryBasicPopulator extends AbstractFacetSearchQueryPopulator
{
	protected static final int BUFFER_SIZE = 256;

	protected static final String ALL_QUERY = "*:*";
	protected static final String SEPARATOR = ",";

	protected static final String Y_QUERY_PARAM = "yq";
	protected static final String Y_MULTIPLICATIVE_BOOSTS_PARAM = "ymb";
	protected static final String Y_ADDITIVE_BOOSTS_PARAM = "yab";

	private FreeTextQueryBuilderFactory freeTextQueryBuilderFactory;

	public FreeTextQueryBuilderFactory getFreeTextQueryBuilderFactory()
	{
		return freeTextQueryBuilderFactory;
	}

	@Required
	public void setFreeTextQueryBuilderFactory(final FreeTextQueryBuilderFactory freeTextQueryBuilderFactory)
	{
		this.freeTextQueryBuilderFactory = freeTextQueryBuilderFactory;
	}

	@Override
	public void populate(final SearchQueryConverterData source, final SolrQuery target)
	{
		final SearchQuery searchQuery = source.getSearchQuery();
		final List<String> queries = new ArrayList<>();
		final List<String> multiplicativeBoostQueries = new ArrayList<>();
		final List<String> additiveBoostQueries = new ArrayList<>();

		generateQueryFieldQueries(searchQuery, queries);
		generateFreeTextQuery(searchQuery, queries);
		generateRawQueries(searchQuery, queries);
		generateCoupledFieldQueries(searchQuery, queries);

		generateBoostQueries(searchQuery, multiplicativeBoostQueries, additiveBoostQueries);

		populateSolrQuery(target, searchQuery, queries, multiplicativeBoostQueries, additiveBoostQueries);
	}

	protected void generateQueryFieldQueries(final SearchQuery searchQuery, final List<String> queries)
	{
		for (final QueryField fieldQuery : searchQuery.getQueries())
		{
			final String query = convertQueryField(searchQuery, fieldQuery);
			queries.add(query);
		}
	}

	protected void generateFreeTextQuery(final SearchQuery searchQuery, final List<String> queries)
	{
		if (StringUtils.isNotBlank(searchQuery.getUserQuery()))
		{
			final FreeTextQueryBuilder freeTextQueryBuilder = getFreeTextQueryBuilderFactory().createQueryBuilder(searchQuery);
			final String freeTextQuery = freeTextQueryBuilder.buildQuery(searchQuery);

			final StringBuilder query = new StringBuilder();
			query.append("_query_:\"");
			query.append(escape(freeTextQuery));
			query.append('\"');

			queries.add(query.toString());
		}
	}

	protected void generateRawQueries(final SearchQuery searchQuery, final List<String> queries)
	{
		for (final RawQuery rawQuery : searchQuery.getRawQueries())
		{
			final String query = convertRawQuery(searchQuery, rawQuery);
			queries.add(query);
		}
	}

	protected void generateBoostQueries(final SearchQuery searchQuery, final List<String> multiplicativeBoosts,
			final List<String> additiveBoosts)
	{
		for (final BoostField boostField : searchQuery.getBoosts())
		{
			final StringBuilder query = new StringBuilder();
			query.append("def(query({!v=\"");

			final String boostQuery = convertBoostField(searchQuery, boostField);
			query.append(escape(boostQuery));

			if (BoostField.BoostType.MULTIPLICATIVE == boostField.getBoostType())
			{
				query.append("\"}),1)");
				multiplicativeBoosts.add(query.toString());
			}
			else
			{
				query.append("\"}),0)");
				additiveBoosts.add(query.toString());
			}
		}
	}

	protected List<String> convertLegacyBoostField(final SearchQuery searchQuery, final QueryField queryField)
	{
		final String convertedField = getFieldNameTranslator().translate(searchQuery, queryField.getField(), FieldType.INDEX);

		final List<String> convertedBoostQueries = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(queryField.getValues()))
		{
			for (final String value : queryField.getValues())
			{
				final StringBuilder query = new StringBuilder();
				query.append(convertedField);
				query.append(':');
				query.append(value);

				convertedBoostQueries.add(query.toString());
			}
		}

		return convertedBoostQueries;
	}

	protected void generateCoupledFieldQueries(final SearchQuery searchQuery, final List<String> queries)
	{
		final Map<String, List<String>> couples = new HashMap<>();
		final Map<String, Operator> operatorMapping = new HashMap<>();

		for (final CoupledQueryField coupledQueryField : searchQuery.getCoupledFields())
		{
			final StringBuilder couple = new StringBuilder();
			couple.append('(');
			couple.append(convertQueryField(searchQuery, coupledQueryField.getField1()));
			couple.append(coupledQueryField.getInnerCouplingOperator().getName());
			couple.append(convertQueryField(searchQuery, coupledQueryField.getField2()));
			couple.append(')');

			List<String> joinedCouples = couples.get(coupledQueryField.getCoupleId());
			if (joinedCouples == null)
			{
				joinedCouples = new ArrayList<>();
			}
			joinedCouples.add(couple.toString());
			couples.put(coupledQueryField.getCoupleId(), joinedCouples);

			operatorMapping.put(coupledQueryField.getCoupleId(), coupledQueryField.getOuterCouplingOperator());
		}

		for (final Map.Entry<String, List<String>> coupleEntry : couples.entrySet())
		{
			final List<String> couple = couples.get(coupleEntry.getKey());
			final Operator operator = operatorMapping.get(coupleEntry.getKey());
			final String separator = " " + operator.getName() + " ";
			final String coupleQuery = StringUtils.join(couple.toArray(), separator);

			final StringBuilder query = new StringBuilder();
			query.append('(');
			query.append(coupleQuery);
			query.append(')');

			queries.add(query.toString());
		}
	}

	protected String buildQuery(final SearchQuery searchQuery, final List<String> queries)
	{
		if (queries.isEmpty())
		{
			return ALL_QUERY;
		}

		final SearchQuery.Operator operator = resolveOperator(searchQuery);
		final String separator = operator.getName();

		final StringBuilder query = new StringBuilder(BUFFER_SIZE);
		query.append(StringUtils.join(queries, separator));

		return query.toString();
	}

	protected String buildMultiplicativeBoostsFunction(final SearchQuery searchQuery, final List<String> boostFields)
	{
		if (CollectionUtils.isEmpty(boostFields))
		{
			return StringUtils.EMPTY;
		}

		final StringBuilder query = new StringBuilder(BUFFER_SIZE);
		query.append("product(");
		query.append(StringUtils.join(boostFields, SEPARATOR));
		query.append(')');

		return query.toString();
	}

	protected String buildAdditiveBoostsFunction(final SearchQuery searchQuery, final List<String> boostFields)
	{
		if (CollectionUtils.isEmpty(boostFields))
		{
			return StringUtils.EMPTY;
		}

		final StringBuilder query = new StringBuilder(BUFFER_SIZE);
		query.append("sum(");
		query.append(StringUtils.join(boostFields, SEPARATOR));
		query.append(')');

		return query.toString();
	}

	protected void populateSolrQuery(final SolrQuery solrQuery, final SearchQuery searchQuery, final List<String> queries,
			final List<String> multiplicativeBoosts, final List<String> additiveBoosts)
	{
		final String query = buildQuery(searchQuery, queries);
		final String multiplicativeBoostsFunction = buildMultiplicativeBoostsFunction(searchQuery, multiplicativeBoosts);
		final String additiveBoostsFunction = buildAdditiveBoostsFunction(searchQuery, additiveBoosts);

		final StringBuilder queryBuilder = new StringBuilder(BUFFER_SIZE);
		queryBuilder.append("{!boost");

		if (StringUtils.isNotBlank(multiplicativeBoostsFunction))
		{
			queryBuilder.append(" b=$");
			queryBuilder.append(Y_MULTIPLICATIVE_BOOSTS_PARAM);
			solrQuery.set(Y_MULTIPLICATIVE_BOOSTS_PARAM, multiplicativeBoostsFunction);
		}

		queryBuilder.append("}(");

		if (StringUtils.isNotBlank(query))
		{
			queryBuilder.append("+{!lucene v=$");
			queryBuilder.append(Y_QUERY_PARAM);
			queryBuilder.append('}');
			solrQuery.set(Y_QUERY_PARAM, buildQuery(searchQuery, queries));
		}

		if (StringUtils.isNotBlank(query) && StringUtils.isNotBlank(additiveBoostsFunction))
		{
			queryBuilder.append(' ');
		}

		if (StringUtils.isNotBlank(additiveBoostsFunction))
		{
			queryBuilder.append("{!func v=$");
			queryBuilder.append(Y_ADDITIVE_BOOSTS_PARAM);
			queryBuilder.append('}');
			solrQuery.set(Y_ADDITIVE_BOOSTS_PARAM, additiveBoostsFunction);
		}

		queryBuilder.append(')');

		solrQuery.setQuery(queryBuilder.toString());
	}

	protected String escape(final String value)
	{
		return ClientUtils.escapeQueryChars(value);
	}
}
