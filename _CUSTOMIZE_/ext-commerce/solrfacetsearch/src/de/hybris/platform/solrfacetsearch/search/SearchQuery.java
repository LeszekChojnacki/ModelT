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
package de.hybris.platform.solrfacetsearch.search;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.PK;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.FacetType;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.SearchConfig;
import de.hybris.platform.solrfacetsearch.config.WildcardType;
import de.hybris.platform.solrfacetsearch.search.OrderField.SortOrder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class SearchQuery implements Serializable
{
	public static final String ALL_FIELDS = "*";

	public enum Operator
	{
		AND(" AND "), OR(" OR ");

		private final String name;

		Operator(final String name)
		{
			this.name = name;
		}

		public String getName()
		{
			return name;
		}
	}

	public enum QueryOperator
	{
		GREATER_THAN_OR_EQUAL_TO, LESS_THAN_OR_EQUAL_TO, GREATER_THAN, LESS_THAN, EQUAL_TO, CONTAINS, MATCHES
	}

	/**
	 * @deprecated Since 5.7, query syntax should not rely on specific query parser.
	 */
	@Deprecated
	public enum QueryParser
	{
		LUCENE("lucene"), EDISMAX("edismax"), DISMAX("dismax");

		private final String name;

		QueryParser(final String name)
		{
			this.name = name;
		}

		public String getName()
		{
			return name;
		}
	}

	private static final long serialVersionUID = 1L;

	private final FacetSearchConfig facetSearchConfig;
	private final IndexedType indexedType;

	private String language;
	private String currency;

	private List<CatalogVersionModel> catalogVersions;

	private int offset = 0;
	private int pageSize = 10;

	private Operator defaultOperator;

	private final List<QueryField> queryFields;

	private String freeTextQueryBuilder;
	private final Map<String, String> freeTextQueryBuilderParameters;
	private String userQuery;
	private List<Keyword> keywords;
	private final List<FreeTextQueryField> freeTextQueryFields;
	private final List<FreeTextFuzzyQueryField> freeTextFuzzyQueryFields;
	private final List<FreeTextWildcardQueryField> freeTextWildcardQueryFields;
	private final List<FreeTextPhraseQueryField> freeTextPhraseQueryFields;

	private final List<RawQuery> rawQueries;

	private final List<QueryField> filterQueryFields;
	private final List<RawQuery> filterRawQueries;

	private final List<GroupCommandField> groupCommandFields;
	private boolean groupFacets = false;

	private final List<OrderField> orderFields;

	private final List<String> fields;
	private final List<String> highlightingFields;

	private final List<FacetField> facetFields;
	private final List<FacetValueField> facetValueFields;

	private final List<BoostField> boostFields;

	private final List<PK> promotedItems;
	private final List<PK> excludedItems;

	private boolean enableSpellcheck = false;
	private final Map<String, String[]> rawParams;

	private final List<Breadcrumb> breadcrumbs;
	private final List<CoupledQueryField> coupledFields;
	private final List<QueryField> legacyBoostFields;

	private QueryParser queryParser;

	private String namedSort;

	/**
	 * @deprecated since 6.4, use any of {@link FacetSearchService} methods for creating a search query.
	 */
	@Deprecated
	public SearchQuery(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType)
	{
		this.facetSearchConfig = facetSearchConfig;
		this.indexedType = indexedType;

		this.queryFields = new ArrayList<>();

		this.freeTextQueryBuilderParameters = new HashMap<>();
		this.freeTextQueryFields = new ArrayList<>();
		this.freeTextFuzzyQueryFields = new ArrayList<>();
		this.freeTextWildcardQueryFields = new ArrayList<>();
		this.freeTextPhraseQueryFields = new ArrayList<>();

		this.rawQueries = new ArrayList<>();

		this.filterQueryFields = new ArrayList<>();
		this.filterRawQueries = new ArrayList<>();

		this.groupCommandFields = new ArrayList<>();
		this.orderFields = new ArrayList<>();

		this.facetFields = new ArrayList<>();
		this.facetValueFields = new ArrayList<>();

		this.boostFields = new ArrayList<>();

		this.promotedItems = new ArrayList<>();
		this.excludedItems = new ArrayList<>();

		this.fields = new ArrayList<>();
		this.highlightingFields = new ArrayList<>();

		this.rawParams = new LinkedHashMap<String, String[]>();

		this.breadcrumbs = new ArrayList<>();
		this.coupledFields = new ArrayList<>();
		this.legacyBoostFields = new ArrayList<>();

		final SearchConfig searchConfig = facetSearchConfig.getSearchConfig();
		if (searchConfig != null)
		{
			this.pageSize = searchConfig.getPageSize();

			if (searchConfig.isLegacyMode())
			{
				addLegacySorts(searchConfig);
			}
		}
	}

	protected final void addLegacySorts(final SearchConfig searchConfig)
	{
		for (final String order : searchConfig.getDefaultSortOrder())
		{
			if (OrderField.SCORE.equals(order))
			{
				addSort(order, SortOrder.DESCENDING);
			}
			else
			{
				addSort(order, SortOrder.ASCENDING);
			}
		}
	}

	/**
	 * Returns the facet search config.
	 *
	 * @return the facet search config
	 */
	public FacetSearchConfig getFacetSearchConfig()
	{
		return this.facetSearchConfig;
	}

	/**
	 * Returns the indexed type.
	 *
	 * @return the indexed type
	 */
	public IndexedType getIndexedType()
	{
		return this.indexedType;
	}

	/**
	 * Returns the language.
	 *
	 * @return the language
	 */
	public String getLanguage()
	{
		return this.language;
	}

	/**
	 * Sets the language.
	 *
	 * @param language
	 *           - isocode of the language
	 */
	public void setLanguage(final String language)
	{
		this.language = language;
	}

	/**
	 * Returns the currency.
	 *
	 * @return the currency
	 */
	public String getCurrency()
	{
		return this.currency;
	}

	/**
	 * Sets the currency.
	 *
	 * @param currency
	 *           - isocode of the currency
	 */
	public void setCurrency(final String currency)
	{
		this.currency = currency;
	}

	/**
	 * Returns the catalog versions.
	 *
	 * @return the catalog versions
	 */
	public List<CatalogVersionModel> getCatalogVersions()
	{
		return catalogVersions;
	}

	/**
	 * Sets the catalog versions.
	 *
	 * @param catalogVersions
	 *           - the catalog versions
	 */
	public void setCatalogVersions(final List<CatalogVersionModel> catalogVersions)
	{
		this.catalogVersions = catalogVersions;
	}

	/**
	 * Returns the offset (page number). This is used for pagination.
	 *
	 * @return the offset (page number)
	 */
	public int getOffset()
	{
		return this.offset;
	}

	/**
	 * Sets the offset (page number). This is used for pagination.
	 *
	 * @param offset
	 *           the offset (page number)
	 */
	public void setOffset(final int offset)
	{
		this.offset = offset < 0 ? 0 : offset;
	}

	/**
	 * Returns the page size. This is used for pagination.
	 *
	 * @return the page size
	 */
	public int getPageSize()
	{
		return this.pageSize;
	}

	/**
	 * Sets the page size. This is used for pagination.
	 *
	 * @param pageSize
	 *           the page size
	 */
	public void setPageSize(final int pageSize)
	{
		this.pageSize = pageSize > 0 ? pageSize : this.pageSize;
	}

	/**
	 * Changes the offset to the next page. Used for pagination.
	 */
	public void nextPage()
	{
		this.offset++;
	}

	/**
	 * Changes the offset to the previous page. Used for pagination.
	 */
	public void prevPage()
	{
		this.offset = this.offset > 0 ? (this.offset - 1) : 0;
	}

	/**
	 * Returns the default operator for the search query.
	 *
	 * @return the defaultOperator
	 */
	public Operator getDefaultOperator()
	{
		return defaultOperator;
	}

	/**
	 * Sets the default operator for the search query.
	 *
	 * @param defaultOperator
	 *           the default operator
	 */
	public void setDefaultOperator(final Operator defaultOperator)
	{
		this.defaultOperator = defaultOperator;
	}

	/**
	 * Adds a query field.
	 *
	 * @param field
	 *           - the field
	 * @param values
	 *           - the values
	 */
	public void addQuery(final String field, final String... values)
	{
		addQuery(new QueryField(field, values));
	}

	/**
	 * Adds a query field.
	 *
	 * @param field
	 *           - the field
	 * @param operator
	 *           - the operator
	 * @param values
	 *           - the values
	 */
	public void addQuery(final String field, final Operator operator, final String... values)
	{
		addQuery(new QueryField(field, operator, values));
	}

	/**
	 * Adds a query field.
	 *
	 * @param field
	 *           - the field
	 * @param operator
	 *           - the operator
	 * @param queryOperator
	 *           - the query operator
	 * @param values
	 *           - the values
	 */
	public void addQuery(final String field, final Operator operator, final QueryOperator queryOperator, final String... values)
	{
		addQuery(new QueryField(field, operator, queryOperator, values));
	}

	/**
	 * Adds a query field.
	 *
	 * @param field
	 *           - the field
	 * @param operator
	 *           - the operator
	 * @param queryOperator
	 *           - the query operator
	 * @param values
	 *           - the values
	 */
	public void addQuery(final String field, final Operator operator, final QueryOperator queryOperator, final Set<String> values)
	{
		addQuery(new QueryField(field, operator, queryOperator, values));
	}

	/**
	 * Adds a query field.
	 *
	 * @param query
	 *           - the query field
	 */
	public void addQuery(final QueryField query)
	{
		queryFields.add(query);
	}

	/**
	 * Returns the query fields.
	 *
	 * @return the query fields
	 */
	public List<QueryField> getQueries()
	{
		return queryFields;
	}

	/**
	 * Returns the query builder.
	 *
	 * @return the query builder
	 */
	public String getFreeTextQueryBuilder()
	{
		return freeTextQueryBuilder;
	}

	/**
	 * Sets the query builder.
	 *
	 * @param freeTextQueryBuilder
	 *           - the query builder
	 */
	public void setFreeTextQueryBuilder(final String freeTextQueryBuilder)
	{
		this.freeTextQueryBuilder = freeTextQueryBuilder;
	}

	/**
	 * Returns the query builder parameters.
	 *
	 * @return the query builder parameters
	 */
	public Map<String, String> getFreeTextQueryBuilderParameters()
	{
		return freeTextQueryBuilderParameters;
	}

	/**
	 * Returns the user query.
	 *
	 * @return the user query
	 */
	public String getUserQuery()
	{
		return userQuery;
	}

	/**
	 * Sets the user query.
	 *
	 * @param userQuery
	 *           - the user query
	 */
	public void setUserQuery(final String userQuery)
	{
		this.userQuery = userQuery;
	}

	/**
	 * Returns the keywords.
	 *
	 * @return the keywords
	 */
	public List<Keyword> getKeywords()
	{
		return keywords;
	}

	/**
	 * Sets the keywords.
	 *
	 * @param keywords
	 *           - the keywords
	 */
	public void setKeywords(final List<Keyword> keywords)
	{
		this.keywords = keywords;
	}

	/**
	 * Adds a free text query field.
	 *
	 * @param field
	 *           - the field
	 * @param minTermLength
	 *           - minimal length of the search term
	 * @param boost
	 *           - the boost value
	 */
	public void addFreeTextQuery(final String field, final Integer minTermLength, final Float boost)
	{
		this.freeTextQueryFields.add(new FreeTextQueryField(field, minTermLength, boost));
	}

	/**
	 * Adds a free text query field.
	 *
	 * @param freeTextQuery
	 *           - the free text query field
	 */
	public void addFreeTextQuery(final FreeTextQueryField freeTextQuery)
	{
		this.freeTextQueryFields.add(freeTextQuery);
	}

	/**
	 * Returns the free text query fields.
	 *
	 * @return the free text query fields
	 */
	public List<FreeTextQueryField> getFreeTextQueries()
	{
		return freeTextQueryFields;
	}

	/**
	 * Adds a free text fuzzy query field.
	 *
	 * @param field
	 *           -the field
	 * @param minTermLength
	 *           - minimal length of the search term
	 * @param fuzziness
	 *           - the fuzziness value
	 * @param boost
	 *           - the boost value
	 */
	public void addFreeTextFuzzyQuery(final String field, final Integer minTermLength, final Integer fuzziness, final Float boost)
	{
		this.freeTextFuzzyQueryFields.add(new FreeTextFuzzyQueryField(field, minTermLength, fuzziness, boost));
	}

	/**
	 * Adds a free text fuzzy query field.
	 *
	 * @param freeTextFuzzyQuery
	 *           -the free text fuzzy query field
	 */
	public void addFreeTextFuzzyQuery(final FreeTextFuzzyQueryField freeTextFuzzyQuery)
	{
		this.freeTextFuzzyQueryFields.add(freeTextFuzzyQuery);
	}

	/**
	 * Returns the free text fuzzy query fields.
	 *
	 * @return the free text fuzzy query fields
	 */
	public List<FreeTextFuzzyQueryField> getFreeTextFuzzyQueries()
	{
		return freeTextFuzzyQueryFields;
	}

	/**
	 * Adds a free text wildcard query field.
	 *
	 * @param field
	 *           - the field
	 * @param minTermLength
	 *           - minimal length of the search term
	 * @param wildcardType
	 *           - the wildcard type
	 * @param boost
	 *           - the boost value
	 */
	public void addFreeTextWildcardQuery(final String field, final Integer minTermLength, final WildcardType wildcardType,
			final Float boost)
	{
		this.freeTextWildcardQueryFields.add(new FreeTextWildcardQueryField(field, minTermLength, wildcardType, boost));
	}

	/**
	 * Adds a free text wildcard query field.
	 *
	 * @param freeTextWildcardQuery
	 *           -the free text wildcard query field
	 */
	public void addFreeTextWildcardQuery(final FreeTextWildcardQueryField freeTextWildcardQuery)
	{
		this.freeTextWildcardQueryFields.add(freeTextWildcardQuery);
	}

	/**
	 * Returns the free text wildcard query fields.
	 *
	 * @return the free text wildcard query fields
	 */
	public List<FreeTextWildcardQueryField> getFreeTextWildcardQueries()
	{
		return freeTextWildcardQueryFields;
	}

	/**
	 * Adds a free text phrase query field.
	 *
	 * @param field
	 *           - the field
	 * @param slop
	 *           - the slop value
	 * @param boost
	 *           - the boost value
	 */
	public void addFreeTextPhraseQuery(final String field, final Float slop, final Float boost)
	{
		this.freeTextPhraseQueryFields.add(new FreeTextPhraseQueryField(field, slop, boost));
	}

	/**
	 * Adds a free text phrase query field.
	 *
	 * @param freeTextPhraseQuery
	 *           - the free text phrase query field
	 */
	public void addFreeTextPhraseQuery(final FreeTextPhraseQueryField freeTextPhraseQuery)
	{
		this.freeTextPhraseQueryFields.add(freeTextPhraseQuery);
	}

	/**
	 * Returns the free text phrase query fields.
	 *
	 * @return the free text phrase query fields
	 */
	public List<FreeTextPhraseQueryField> getFreeTextPhraseQueries()
	{
		return freeTextPhraseQueryFields;
	}

	/**
	 * Adds a raw query using lucene syntax.
	 *
	 * @param rawQuery
	 *           - the raw query
	 */
	public void addRawQuery(final String rawQuery)
	{
		addRawQuery(new RawQuery(rawQuery));
	}

	/**
	 * Adds a raw query using lucene syntax.
	 *
	 * @param rawQuery
	 *           - the raw query
	 */
	public void addRawQuery(final RawQuery rawQuery)
	{
		if (StringUtils.isNotBlank(rawQuery.getField()))
		{
			// this is only for compatibility purposes, should be removed in a future version
			final Iterator<RawQuery> iterator = rawQueries.iterator();
			while (iterator.hasNext())
			{
				final RawQuery existingRawQuery = iterator.next();
				if (StringUtils.isNotBlank(existingRawQuery.getField()) && existingRawQuery.getField().equals(rawQuery.getField()))
				{
					iterator.remove();
					final String query = existingRawQuery.getQuery() + " " + rawQuery.getOperator() + " " + rawQuery.getQuery();
					rawQueries.add(new RawQuery(rawQuery.getField(), query, rawQuery.getOperator()));
					return;
				}
			}
		}

		rawQueries.add(rawQuery);
	}

	/**
	 * Returns the raw queries.
	 *
	 * @return the raw queries
	 */
	public List<RawQuery> getRawQueries()
	{
		return rawQueries;
	}

	/**
	 * Adds a filter query field.
	 *
	 * @param field
	 *           - the field
	 * @param values
	 *           - the values
	 */
	public void addFilterQuery(final String field, final String... values)
	{
		addFilterQuery(new QueryField(field, values));
	}

	/**
	 * Adds a filter query field.
	 *
	 * @param field
	 *           - the field
	 * @param operator
	 *           - the operator
	 * @param values
	 *           - the values
	 */
	public void addFilterQuery(final String field, final SearchQuery.Operator operator, final String... values)
	{
		addFilterQuery(new QueryField(field, operator, values));
	}

	/**
	 * Adds a filter query field.
	 *
	 * @param field
	 *           - the field
	 * @param operator
	 *           - the operator
	 * @param values
	 *           - the values
	 */
	public void addFilterQuery(final String field, final SearchQuery.Operator operator, final Set<String> values)
	{
		addFilterQuery(new QueryField(field, operator, values));
	}

	/**
	 * Adds a filter query field.
	 *
	 * @param query
	 *           - the filter query field
	 */
	public void addFilterQuery(final QueryField query)
	{
		filterQueryFields.add(query);
	}

	/**
	 * Returns the filter query fields.
	 *
	 * @return the filter query fields
	 */
	public List<QueryField> getFilterQueries()
	{
		return filterQueryFields;
	}

	/**
	 * Adds a filter raw query using lucene syntax.
	 *
	 * @param rawQuery
	 *           - the raw query
	 */
	public void addFilterRawQuery(final String rawQuery)
	{
		addFilterRawQuery(new RawQuery(rawQuery));
	}

	/**
	 * Adds a filter raw query using lucene syntax.
	 *
	 * @param rawQuery
	 *           - the raw query
	 */
	public void addFilterRawQuery(final RawQuery rawQuery)
	{
		if (StringUtils.isNotBlank(rawQuery.getField()))
		{
			// this is only for compatibility purposes, should be removed in a future version
			final Iterator<RawQuery> iterator = filterRawQueries.iterator();
			while (iterator.hasNext())
			{
				final RawQuery existingRawQuery = iterator.next();
				if (StringUtils.isNotBlank(existingRawQuery.getField()) && existingRawQuery.getField().equals(rawQuery.getField()))
				{
					iterator.remove();
					final String query = existingRawQuery.getQuery() + " " + rawQuery.getOperator() + " " + rawQuery.getQuery();
					filterRawQueries.add(new RawQuery(rawQuery.getField(), query, rawQuery.getOperator()));
					return;
				}
			}
		}

		filterRawQueries.add(rawQuery);
	}

	/**
	 * Returns the filter raw queries.
	 *
	 * @return the filter raw queries
	 */
	public List<RawQuery> getFilterRawQueries()
	{
		return filterRawQueries;
	}

	/**
	 * Adds group command field.
	 *
	 * @param field
	 *           - the group field
	 */
	public void addGroupCommand(final String field)
	{
		addGroupCommand(new GroupCommandField(field));
	}

	/**
	 * Adds group command field.
	 *
	 * @param field
	 *           - the group field
	 * @param groupLimit
	 *           - the group limit
	 */
	public void addGroupCommand(final String field, final Integer groupLimit)
	{
		addGroupCommand(new GroupCommandField(field, groupLimit));
	}

	/**
	 * Adds group command field.
	 *
	 * @param groupCommand
	 *           - the group command field
	 */
	public void addGroupCommand(final GroupCommandField groupCommand)
	{
		this.groupCommandFields.add(groupCommand);
	}

	/**
	 * Returns group command fields.
	 *
	 * @return the group command fields
	 */
	public List<GroupCommandField> getGroupCommands()
	{
		return groupCommandFields;
	}

	/**
	 * Returns {@code true} if group facets is enabled for the search query.
	 *
	 * @return {@code true} if group facets is enabled, {@code false} otherwise
	 */
	public boolean isGroupFacets()
	{
		return this.groupFacets;
	}

	/**
	 * Enables or disables group facets for the search query.
	 *
	 * @param groupFacets
	 *           {@code true} to enable group facets, {@code false} to disable it
	 */
	public void setGroupFacets(final boolean groupFacets)
	{
		this.groupFacets = groupFacets;
	}

	/**
	 * Adds a sort field using ascending as sort order.
	 *
	 * @param field
	 *           - the field
	 */
	public void addSort(final String field)
	{
		addSort(new OrderField(field));
	}

	/**
	 * Adds a sort field.
	 *
	 * @param field
	 *           - the field
	 * @param sortOrder
	 *           - the sort order
	 */
	public void addSort(final String field, final OrderField.SortOrder sortOrder)
	{
		addSort(new OrderField(field, sortOrder));
	}

	/**
	 * Adds a sort field.
	 *
	 * @param sort
	 *           - the sort field
	 */
	public void addSort(final OrderField sort)
	{
		orderFields.add(sort);
	}

	/**
	 * Returns the sort fields.
	 *
	 * @return the sort fields
	 */
	public List<OrderField> getSorts()
	{
		return orderFields;
	}

	/**
	 * Adds a field to the list of fields that will be returned within a query response.
	 *
	 * @param field
	 *           - the field
	 */
	public void addField(final String field)
	{
		this.fields.add(field);
	}

	/**
	 * Returns the list of fields that will be returned within a query response.
	 *
	 * @return the fields
	 */
	public List<String> getFields()
	{
		return fields;
	}

	/**
	 * Adds a field to the list of fields that will be used for highlighting the search term.
	 *
	 * @param highlightingField
	 *           - the field used for highlighting.
	 */
	public void addHighlightingField(final String highlightingField)
	{
		this.highlightingFields.add(highlightingField);
	}

	/**
	 * Returns the list of fields that will be used for highlighting search term.
	 *
	 * @return the fields
	 */
	public List<String> getHighlightingFields()
	{
		return highlightingFields;
	}

	/**
	 * Adds a facet field.
	 *
	 * @param field
	 *           - the field
	 */
	public void addFacet(final String field)
	{
		addFacet(new FacetField(field));
	}

	/**
	 * Adds a facet field.
	 *
	 * @param field
	 *           - the field
	 * @param facetType
	 *           - the facet type
	 */
	public void addFacet(final String field, final FacetType facetType)
	{
		addFacet(new FacetField(field, facetType));
	}

	/**
	 * Adds a facet field.
	 *
	 * @param facet
	 *           - the facet field
	 */
	public void addFacet(final FacetField facet)
	{
		this.facetFields.add(facet);
	}

	/**
	 * Returns the facet fields.
	 *
	 * @return the facet fields.
	 */
	public List<FacetField> getFacets()
	{
		return facetFields;
	}

	/**
	 * Adds a facet value field.
	 *
	 * @param field
	 *           - the field
	 * @param values
	 *           - the values
	 */
	public void addFacetValue(final String field, final String... values)
	{
		addFacetValue(new FacetValueField(field, values));
	}

	/**
	 * Adds a facet value field.
	 *
	 * @param field
	 *           - the field
	 * @param values
	 *           - the values
	 */
	public void addFacetValue(final String field, final Set<String> values)
	{
		addFacetValue(new FacetValueField(field, values));
	}

	/**
	 * Adds a facet value field.
	 *
	 * @param facetValue
	 *           - the facet value field
	 */
	public void addFacetValue(final FacetValueField facetValue)
	{
		for (final String value : facetValue.getValues())
		{
			breadcrumbs.add(new Breadcrumb(facetValue.getField(), value));
		}

		facetValueFields.add(facetValue);
	}

	/**
	 * Returns the facet value fields.
	 *
	 * @return the facet value fields.
	 */
	public List<FacetValueField> getFacetValues()
	{
		return facetValueFields;
	}

	public void addBoost(final String field, final QueryOperator queryOperator, final Object value, final Float boost,
			final BoostField.BoostType boostType)
	{
		final BoostField boostField = new BoostField(field, queryOperator, value, boost, boostType);
		addBoost(boostField);
	}

	public void addBoost(final BoostField boostField)
	{
		boostFields.add(boostField);
	}

	public List<BoostField> getBoosts()
	{
		return boostFields;
	}

	public void addPromotedItem(final PK itemPk)
	{
		promotedItems.add(itemPk);
	}

	public List<PK> getPromotedItems()
	{
		return promotedItems;
	}

	public void addExcludedItem(final PK itemPk)
	{
		excludedItems.add(itemPk);
	}

	public List<PK> getExcludedItems()
	{
		return excludedItems;
	}

	/**
	 * Returns {@code true} if spell checking is enabled for the search query.
	 *
	 * @return {@code true} if spell checking is enabled, {@code false} otherwise
	 */
	public boolean isEnableSpellcheck()
	{
		return enableSpellcheck;
	}

	/**
	 * Enables or disables spell checking for the search query.
	 *
	 * @param enableSpellcheck
	 *           {@code true} to enable spell checking, {@code false} to disable it
	 */
	public void setEnableSpellcheck(final boolean enableSpellcheck)
	{
		this.enableSpellcheck = enableSpellcheck;
	}

	/**
	 * Adds a new raw parameter.
	 *
	 * @param param
	 *           - the parameter
	 * @param values
	 *           - the values for the parameter
	 */
	public void addRawParam(final String param, final String... values)
	{
		rawParams.put(param, values);
	}

	/**
	 * Returns the raw parameters.
	 *
	 * @return the raw parameters
	 */
	public Map<String, String[]> getRawParams()
	{
		return rawParams;
	}

	/**
	 * @return the breadcrumbs
	 */
	public List<Breadcrumb> getBreadcrumbs()
	{
		return breadcrumbs;
	}

	/**
	 * Set custom QueryParser. If not set default query parser is used.
	 *
	 * @deprecated Since 5.7, query syntax should not rely on specific query parser.
	 */
	@Deprecated
	public void setQueryParser(final QueryParser queryParser)
	{
		this.queryParser = queryParser;
	}

	/**
	 * Get custom QueryParser.
	 *
	 * @return QueryParser or null when it was not set.
	 *
	 * @deprecated Since 5.7, query syntax should not rely on specific query parser.
	 */
	@Deprecated
	public QueryParser getQueryParser()
	{
		return queryParser;
	}

	/**
	 * Adds a raw query using lucene syntax.
	 *
	 * @param rawQuery
	 *           - the raw query
	 * @param operator
	 *           - the operator
	 *
	 * @deprecated Since 5.7, see {@link #setDefaultOperator(Operator)}.
	 */
	@Deprecated
	public void addRawQuery(final String rawQuery, final Operator operator)
	{
		addRawQuery(new RawQuery(rawQuery, operator));
	}

	/**
	 * @deprecated Since 5.7, replaced by {@link #addBoost(BoostField)}
	 */
	@Deprecated
	public void addBoostField(final String field, final String value, final Operator operator)
	{
		final int fieldIndex = this.findBoostField(field);
		if (fieldIndex == -1)
		{
			this.legacyBoostFields.add(new QueryField(field, operator, value));
		}
		else
		{
			this.legacyBoostFields.get(fieldIndex).getValues().add(value);
			if (Operator.OR == operator)
			{
				this.legacyBoostFields.get(fieldIndex).setOperator(Operator.OR);
			}
		}
	}

	/**
	 * @return list of all boost fields
	 *
	 * @deprecated Since 5.7, replaced by {@link #getRawQueries()}
	 */
	@Deprecated
	public List<QueryField> getBoostFields()
	{
		return legacyBoostFields;
	}

	protected int findField(final String name)
	{
		for (int i = 0; i < this.queryFields.size(); i++)
		{
			if (this.queryFields.get(i).getField().equals(name))
			{
				return i;
			}
		}
		return -1;
	}

	protected int findBoostField(final String name)
	{
		for (int i = 0; i < this.legacyBoostFields.size(); i++)
		{
			if (this.legacyBoostFields.get(i).getField().equals(name))
			{
				return i;
			}
		}
		return -1;
	}

	/**
	 * Adds logically coupled query fields.
	 */
	public void addCoupledFields(final CoupledQueryField field)
	{
		this.coupledFields.add(field);
	}

	/**
	 * @return the coupledFields
	 */
	public List<CoupledQueryField> getCoupledFields()
	{
		return coupledFields;
	}


	public String getNamedSort() {
		return namedSort;
	}

	public void setNamedSort(final String namedSort) {
		this.namedSort = namedSort;
	}
}
