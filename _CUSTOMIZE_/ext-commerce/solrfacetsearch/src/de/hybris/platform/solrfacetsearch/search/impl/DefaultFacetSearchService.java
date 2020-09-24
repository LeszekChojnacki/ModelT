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

import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.SearchConfig;
import de.hybris.platform.solrfacetsearch.config.SearchQueryProperty;
import de.hybris.platform.solrfacetsearch.config.SearchQuerySort;
import de.hybris.platform.solrfacetsearch.config.SearchQueryTemplate;
import de.hybris.platform.solrfacetsearch.provider.IndexedTypeFieldsValuesProvider;
import de.hybris.platform.solrfacetsearch.search.FacetField;
import de.hybris.platform.solrfacetsearch.search.FacetSearchException;
import de.hybris.platform.solrfacetsearch.search.FacetSearchService;
import de.hybris.platform.solrfacetsearch.search.FacetSearchStrategy;
import de.hybris.platform.solrfacetsearch.search.FacetSearchStrategyFactory;
import de.hybris.platform.solrfacetsearch.search.OrderField;
import de.hybris.platform.solrfacetsearch.search.OrderField.SortOrder;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.SearchResult;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation for {@link FacetSearchService}
 */
public class DefaultFacetSearchService implements FacetSearchService, BeanFactoryAware
{
	public static final String DEFAULT_QUERY_TEMPLATE_NAME = "DEFAULT";
	private FacetSearchStrategyFactory facetSearchStrategyFactory;

	private BeanFactory beanFactory;

	public FacetSearchStrategyFactory getFacetSearchStrategyFactory()
	{
		return facetSearchStrategyFactory;
	}

	@Required
	public void setFacetSearchStrategyFactory(final FacetSearchStrategyFactory facetSearchStrategyFactory)
	{
		this.facetSearchStrategyFactory = facetSearchStrategyFactory;
	}

	public BeanFactory getBeanFactory()
	{
		return beanFactory;
	}

	@Override
	public void setBeanFactory(final BeanFactory beanFactory)
	{
		this.beanFactory = beanFactory;
	}

	@Override
	public SearchResult search(final SearchQuery query) throws FacetSearchException
	{
		return search(query, Collections.<String, String> emptyMap());
	}

	@Override
	public SearchResult search(final SearchQuery query, final Map<String, String> searchHints) throws FacetSearchException
	{
		final FacetSearchConfig facetSearchConfig = query.getFacetSearchConfig();
		final IndexedType indexedType = query.getIndexedType();

		final FacetSearchStrategy facetSearchStrategy = getFacetSearchStrategy(facetSearchConfig, indexedType);

		return facetSearchStrategy.search(query, searchHints);
	}

	protected FacetSearchStrategy getFacetSearchStrategy(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType)
	{
		return facetSearchStrategyFactory.createStrategy(facetSearchConfig, indexedType);
	}

	@Override
	public SearchQuery createSearchQuery(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType)
	{
		return new SearchQuery(facetSearchConfig, indexedType);
	}

	@Override
	public SearchQuery createPopulatedSearchQuery(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType)
	{
		return createFreeTextSearchQuery(facetSearchConfig, indexedType, null);
	}

	@Override
	public SearchQuery createFreeTextSearchQuery(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final String userQuery)
	{
		final SearchQuery searchQuery = this.createSearchQuery(facetSearchConfig, indexedType);

		populateGroupCommandFields(facetSearchConfig, indexedType, searchQuery);
		populateFacetFields(facetSearchConfig, indexedType, searchQuery);
		populateFields(facetSearchConfig, indexedType, searchQuery);
		populateHighlightingFields(facetSearchConfig, indexedType, searchQuery);
		populateSortFields(facetSearchConfig, indexedType, searchQuery);

		if (StringUtils.isNotBlank(userQuery))
		{
			populateFreeTextQuery(facetSearchConfig, indexedType, searchQuery, userQuery);
		}

		return searchQuery;
	}

	protected void populateGroupCommandFields(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final SearchQuery searchQuery)
	{
		if (indexedType.isGroup())
		{
			searchQuery.addGroupCommand(indexedType.getGroupFieldName(), indexedType.getGroupLimit());
			searchQuery.setGroupFacets(indexedType.isGroupFacets());
		}
	}

	protected void populateFacetFields(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final SearchQuery searchQuery)
	{
		if (indexedType.getIndexedProperties() != null)
		{
			for (final IndexedProperty indexedProperty : indexedType.getIndexedProperties().values())
			{
				if (indexedProperty.isFacet())
				{
					final FacetField facetField = new FacetField(indexedProperty.getName(), indexedProperty.getFacetType());
					facetField.setPriority(indexedProperty.getPriority());
					facetField.setDisplayNameProvider(indexedProperty.getFacetDisplayNameProvider());
					facetField.setSortProvider(indexedProperty.getFacetSortProvider());
					facetField.setTopValuesProvider(indexedProperty.getTopValuesProvider());

					searchQuery.addFacet(facetField);
				}
			}
		}

		final IndexedTypeFieldsValuesProvider provider = getFieldsValuesProvider(indexedType);
		if (provider != null)
		{
			for (final String facet : provider.getFacets())
			{
				searchQuery.addFacet(facet);
			}
		}
	}

	protected void populateFields(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final SearchQuery searchQuery)
	{
		final SearchConfig searchConfig = facetSearchConfig.getSearchConfig();
		if (searchConfig != null && searchConfig.isRestrictFieldsInResponse())
		{
			if (MapUtils.isNotEmpty(indexedType.getIndexedProperties()))
			{
				indexedType.getIndexedProperties().values().stream().filter(indexedProperty -> indexedProperty.isIncludeInResponse())
						.forEach(fieldIncluded -> searchQuery.addField(fieldIncluded.getName()));
			}

			final IndexedTypeFieldsValuesProvider provider = getFieldsValuesProvider(indexedType);
			if (provider != null)
			{
				provider.getFieldNamesMapping().keySet().forEach(searchQuery::addField);
			}
		}
	}

	protected void populateHighlightingFields(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final SearchQuery searchQuery)
	{
		final SearchConfig searchConfig = facetSearchConfig.getSearchConfig();
		if (searchConfig != null && searchConfig.isEnableHighlighting() && MapUtils.isNotEmpty(indexedType.getIndexedProperties()))
		{
			indexedType.getIndexedProperties().values().stream().filter(indexedProperty -> indexedProperty.isHighlight())
					.forEach(highlightingField -> searchQuery.addHighlightingField(highlightingField.getName()));
		}
	}

	protected void populateSortFields(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final SearchQuery searchQuery)
	{
		final SearchConfig searchConfig = facetSearchConfig.getSearchConfig();
		if (searchConfig != null && searchConfig.getDefaultSortOrder() != null)
		{
			for (final String order : searchConfig.getDefaultSortOrder())
			{
				if (OrderField.SCORE.equals(order))
				{
					searchQuery.addSort(order, SortOrder.DESCENDING);
				}
				else
				{
					searchQuery.addSort(order, SortOrder.ASCENDING);
				}
			}
		}
	}

	protected void populateFreeTextQuery(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final SearchQuery searchQuery, final String userQuery)
	{
		searchQuery.setFreeTextQueryBuilder(indexedType.getFtsQueryBuilder());

		if (MapUtils.isNotEmpty(indexedType.getFtsQueryBuilderParameters()))
		{
			searchQuery.getFreeTextQueryBuilderParameters().putAll(indexedType.getFtsQueryBuilderParameters());
		}

		searchQuery.setUserQuery(userQuery);

		for (final IndexedProperty indexedProperty : indexedType.getIndexedProperties().values())
		{
			if (indexedProperty.isFtsQuery())
			{
				searchQuery.addFreeTextQuery(indexedProperty.getName(), indexedProperty.getFtsQueryMinTermLength(),
						indexedProperty.getFtsQueryBoost());
			}

			if (indexedProperty.isFtsFuzzyQuery())
			{
				searchQuery.addFreeTextFuzzyQuery(indexedProperty.getName(), indexedProperty.getFtsFuzzyQueryMinTermLength(),
						indexedProperty.getFtsFuzzyQueryFuzziness(), indexedProperty.getFtsFuzzyQueryBoost());
			}

			if (indexedProperty.isFtsWildcardQuery())
			{
				searchQuery.addFreeTextWildcardQuery(indexedProperty.getName(), indexedProperty.getFtsWildcardQueryMinTermLength(),
						indexedProperty.getFtsWildcardQueryType(), indexedProperty.getFtsWildcardQueryBoost());
			}

			if (indexedProperty.isFtsPhraseQuery())
			{
				searchQuery.addFreeTextPhraseQuery(indexedProperty.getName(), indexedProperty.getFtsPhraseQuerySlop(),
						indexedProperty.getFtsPhraseQueryBoost());
			}
		}
	}

	@Override
	public SearchQuery createSearchQueryFromTemplate(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final String queryTemplateName)
	{
		return createFreeTextSearchQueryFromTemplate(facetSearchConfig, indexedType, queryTemplateName, null);
	}

	@Override
	public SearchQuery createFreeTextSearchQueryFromTemplate(final FacetSearchConfig facetSearchConfig,
			final IndexedType indexedType, final String queryTemplateName, final String userQuery)
	{
		ServicesUtil.validateParameterNotNull(facetSearchConfig, "FacetSearchConfig cannot be null");
		ServicesUtil.validateParameterNotNull(indexedType, "IndexedType cannot be null");
		ServicesUtil.validateParameterNotNull(queryTemplateName, "QueryTemplateName cannot be null");

		final SearchQueryTemplate queryTemplate = findQueryTemplateForName(indexedType, queryTemplateName);
		if (queryTemplate != null)
		{
			final SearchQuery searchQuery = this.createSearchQuery(facetSearchConfig, indexedType);

			populateGroupCommandFields(facetSearchConfig, indexedType, queryTemplate, searchQuery);
			populateFacetFields(facetSearchConfig, indexedType, queryTemplate, searchQuery);
			populateFields(facetSearchConfig, indexedType, queryTemplate, searchQuery);
			populateHighlightingFields(facetSearchConfig, indexedType, queryTemplate, searchQuery);
			populateSortFields(facetSearchConfig, indexedType, queryTemplate, searchQuery);

			if (StringUtils.isNotBlank(userQuery))
			{
				populateFreeTextQuery(facetSearchConfig, indexedType, queryTemplate, searchQuery, userQuery);
			}

			return searchQuery;
		}
		else
		{
			return createFreeTextSearchQuery(facetSearchConfig, indexedType, userQuery);
		}
	}

	protected SearchQueryTemplate findQueryTemplateForName(final IndexedType indexedType, final String queryTemplateName)
	{
		final Map<String, SearchQueryTemplate> searchQueryTemplates = indexedType.getSearchQueryTemplates();
		if (searchQueryTemplates == null)
		{
			return null;
		}

		final SearchQueryTemplate queryTemplate = searchQueryTemplates.get(queryTemplateName);

		if (queryTemplate != null)
		{
			return queryTemplate;
		}

		return searchQueryTemplates.get(DEFAULT_QUERY_TEMPLATE_NAME);
	}

	protected void populateGroupCommandFields(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final SearchQueryTemplate searchQueryTemplate, final SearchQuery searchQuery)
	{
		if (searchQueryTemplate.isGroup() && searchQueryTemplate.getGroupProperty() != null)
		{
			searchQuery.addGroupCommand(searchQueryTemplate.getGroupProperty().getName(), searchQueryTemplate.getGroupLimit());
			searchQuery.setGroupFacets(searchQueryTemplate.isGroupFacets());
		}
	}

	protected void populateFacetFields(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final SearchQueryTemplate searchQueryTemplate, final SearchQuery searchQuery)
	{
		if (!searchQueryTemplate.isShowFacets())
		{
			return;
		}

		if (searchQueryTemplate.getSearchQueryProperties() != null)
		{
			searchQueryTemplate.getSearchQueryProperties().values().stream().filter(SearchQueryProperty::isFacet)
					.forEach(facetProperty -> {
						final FacetField facetField = new FacetField(facetProperty.getIndexedProperty(), facetProperty.getFacetType());
						facetField.setPriority(facetProperty.getPriority());
						facetField.setDisplayNameProvider(facetProperty.getFacetDisplayNameProvider());
						facetField.setSortProvider(facetProperty.getFacetSortProvider());
						facetField.setTopValuesProvider(facetProperty.getFacetTopValuesProvider());

						searchQuery.addFacet(facetField);
					});
		}

		final IndexedTypeFieldsValuesProvider provider = getFieldsValuesProvider(indexedType);
		if (provider != null)
		{
			provider.getFacets().forEach(searchQuery::addFacet);
		}
	}

	protected void populateFields(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final SearchQueryTemplate searchQueryTemplate, final SearchQuery searchQuery)
	{
		if (searchQueryTemplate.isRestrictFieldsInResponse())
		{
			if (MapUtils.isNotEmpty(searchQueryTemplate.getSearchQueryProperties()))
			{
				searchQueryTemplate.getSearchQueryProperties().values().stream()
						.filter(searchQueryProperty -> searchQueryProperty.isIncludeInResponse())
						.forEach(fieldIncluded -> searchQuery.addField(fieldIncluded.getIndexedProperty()));
			}

			final IndexedTypeFieldsValuesProvider provider = getFieldsValuesProvider(indexedType);
			if (provider != null)
			{
				provider.getFieldNamesMapping().keySet().forEach(searchQuery::addField);
			}
		}
	}

	protected void populateHighlightingFields(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final SearchQueryTemplate searchQueryTemplate, final SearchQuery searchQuery)
	{
		if (searchQueryTemplate.isEnableHighlighting() && MapUtils.isNotEmpty(searchQueryTemplate.getSearchQueryProperties()))
		{
			searchQueryTemplate.getSearchQueryProperties().values().stream()
					.filter(searchQueryProperty -> searchQueryProperty.isHighlight())
					.forEach(highlightingField -> searchQuery.addHighlightingField(highlightingField.getIndexedProperty()));
		}
	}

	protected void populateSortFields(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final SearchQueryTemplate searchQueryTemplate, final SearchQuery searchQuery)
	{
		if (CollectionUtils.isEmpty(searchQueryTemplate.getSearchQuerySorts()))
		{
			return;
		}

		for (final SearchQuerySort sort : searchQueryTemplate.getSearchQuerySorts())
		{
			searchQuery.addSort(sort.getField(), sort.isAscending() ? SortOrder.ASCENDING : SortOrder.DESCENDING);
		}
	}

	protected void populateFreeTextQuery(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final SearchQueryTemplate searchQueryTemplate, final SearchQuery searchQuery, final String userQuery)
	{
		searchQuery.setFreeTextQueryBuilder(searchQueryTemplate.getFtsQueryBuilder());

		if (MapUtils.isNotEmpty(searchQueryTemplate.getFtsQueryBuilderParameters()))
		{
			searchQuery.getFreeTextQueryBuilderParameters().putAll(searchQueryTemplate.getFtsQueryBuilderParameters());
		}

		searchQuery.setUserQuery(userQuery);

		for (final SearchQueryProperty searchQueryProperty : searchQueryTemplate.getSearchQueryProperties().values())
		{
			if (searchQueryProperty.isFtsQuery())
			{
				searchQuery.addFreeTextQuery(searchQueryProperty.getIndexedProperty(), searchQueryProperty.getFtsQueryMinTermLength(),
						searchQueryProperty.getFtsQueryBoost());
			}

			if (searchQueryProperty.isFtsFuzzyQuery())
			{
				searchQuery.addFreeTextFuzzyQuery(searchQueryProperty.getIndexedProperty(),
						searchQueryProperty.getFtsFuzzyQueryMinTermLength(), searchQueryProperty.getFtsFuzzyQueryFuzziness(),
						searchQueryProperty.getFtsFuzzyQueryBoost());
			}

			if (searchQueryProperty.isFtsWildcardQuery())
			{
				searchQuery.addFreeTextWildcardQuery(searchQueryProperty.getIndexedProperty(),
						searchQueryProperty.getFtsWildcardQueryMinTermLength(), searchQueryProperty.getFtsWildcardQueryType(),
						searchQueryProperty.getFtsWildcardQueryBoost());
			}

			if (searchQueryProperty.isFtsPhraseQuery())
			{
				searchQuery.addFreeTextPhraseQuery(searchQueryProperty.getIndexedProperty(),
						searchQueryProperty.getFtsPhraseQuerySlop(), searchQueryProperty.getFtsPhraseQueryBoost());
			}
		}
	}

	protected void populatePagination(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType,
			final SearchQueryTemplate searchQueryTemplate, final SearchQuery searchQuery)
	{
		searchQuery.setPageSize(searchQueryTemplate.getPageSize());
	}

	protected IndexedTypeFieldsValuesProvider getFieldsValuesProvider(final IndexedType indexedType)
	{
		if (!StringUtils.isEmpty(indexedType.getFieldsValuesProvider()))
		{
			final Object fieldsValueProvider = beanFactory.getBean(indexedType.getFieldsValuesProvider());
			if (fieldsValueProvider instanceof IndexedTypeFieldsValuesProvider)
			{
				return (IndexedTypeFieldsValuesProvider) fieldsValueProvider;
			}
		}

		return null;
	}
}
