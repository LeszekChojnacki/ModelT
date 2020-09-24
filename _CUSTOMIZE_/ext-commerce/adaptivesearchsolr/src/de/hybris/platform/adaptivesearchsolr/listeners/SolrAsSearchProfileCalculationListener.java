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
package de.hybris.platform.adaptivesearchsolr.listeners;

import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.DEFAULT_FACET_PRIORITY;
import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.DEFAULT_SORT_PRIORITY;
import static de.hybris.platform.adaptivesearchsolr.constants.AdaptivesearchsolrConstants.SCORE_FIELD;

import de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants;
import de.hybris.platform.adaptivesearch.context.AsKeyword;
import de.hybris.platform.adaptivesearch.context.AsKeywordModifier;
import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;
import de.hybris.platform.adaptivesearch.context.AsSearchProfileContextFactory;
import de.hybris.platform.adaptivesearch.data.AbstractAsBoostItemConfiguration;
import de.hybris.platform.adaptivesearch.data.AbstractAsBoostRuleConfiguration;
import de.hybris.platform.adaptivesearch.data.AbstractAsFacetConfiguration;
import de.hybris.platform.adaptivesearch.data.AbstractAsFacetValueConfiguration;
import de.hybris.platform.adaptivesearch.data.AbstractAsSortConfiguration;
import de.hybris.platform.adaptivesearch.data.AsBoostRule;
import de.hybris.platform.adaptivesearch.data.AsConfigurationHolder;
import de.hybris.platform.adaptivesearch.data.AsExcludedFacetValue;
import de.hybris.platform.adaptivesearch.data.AsExcludedItem;
import de.hybris.platform.adaptivesearch.data.AsFacet;
import de.hybris.platform.adaptivesearch.data.AsPromotedFacet;
import de.hybris.platform.adaptivesearch.data.AsPromotedFacetValue;
import de.hybris.platform.adaptivesearch.data.AsPromotedItem;
import de.hybris.platform.adaptivesearch.data.AsPromotedSort;
import de.hybris.platform.adaptivesearch.data.AsSearchProfileActivationGroup;
import de.hybris.platform.adaptivesearch.data.AsSearchProfileResult;
import de.hybris.platform.adaptivesearch.data.AsSort;
import de.hybris.platform.adaptivesearch.data.AsSortExpression;
import de.hybris.platform.adaptivesearch.enums.AsSortOrder;
import de.hybris.platform.adaptivesearch.services.AsSearchProfileActivationService;
import de.hybris.platform.adaptivesearch.services.AsSearchProfileCalculationService;
import de.hybris.platform.adaptivesearch.strategies.AsUidGenerator;
import de.hybris.platform.adaptivesearch.util.MergeMap;
import de.hybris.platform.adaptivesearchsolr.strategies.SolrAsCatalogVersionResolver;
import de.hybris.platform.adaptivesearchsolr.strategies.SolrAsCategoryPathResolver;
import de.hybris.platform.adaptivesearchsolr.strategies.SolrAsTypeMappingRegistry;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedTypeSort;
import de.hybris.platform.solrfacetsearch.config.IndexedTypeSortField;
import de.hybris.platform.solrfacetsearch.search.BoostField;
import de.hybris.platform.solrfacetsearch.search.Document;
import de.hybris.platform.solrfacetsearch.search.Facet;
import de.hybris.platform.solrfacetsearch.search.FacetField;
import de.hybris.platform.solrfacetsearch.search.FacetSearchException;
import de.hybris.platform.solrfacetsearch.search.Keyword;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.SearchResult;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchContext;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Listener for search profiles calculation/merging.
 */
public class SolrAsSearchProfileCalculationListener implements FacetSearchListener
{
	private static final Logger LOG = LoggerFactory.getLogger(SolrAsSearchProfileCalculationListener.class);

	protected static final String ADAPTIVE_SEARCH_RESULT = "adaptiveSearchResult";
	protected static final String ADAPTIVE_SEARCH_CATALOG_VERSIONS = "adaptiveSearchCatalogVersions";
	protected static final String ADAPTIVE_SEARCH_CATEGORY_PATH = "adaptiveSearchCategoryPath";

	private SolrAsCatalogVersionResolver solrAsCatalogVersionResolver;
	private SolrAsCategoryPathResolver solrAsCategoryPathResolver;
	private SolrAsTypeMappingRegistry solrAsTypeMappingRegistry;
	private AsSearchProfileContextFactory asSearchProfileContextFactory;
	private AsSearchProfileActivationService asSearchProfileActivationService;
	private AsSearchProfileCalculationService asSearchProfileCalculationService;
	private CommonI18NService commonI18NService;

	@Override
	public void beforeSearch(final FacetSearchContext facetSearchContext) throws FacetSearchException
	{
		if (facetSearchContext.getFacetSearchConfig().getSearchConfig().isLegacyMode())
		{
			LOG.warn("Adaptive search does not support search legacy mode: " + facetSearchContext.getFacetSearchConfig().getName()
					+ "/" + facetSearchContext.getIndexedType().getIdentifier());
			return;
		}

		final SearchQuery searchQuery = facetSearchContext.getSearchQuery();

		final String indexConfiguration = facetSearchContext.getFacetSearchConfig().getName();
		final String indexType = facetSearchContext.getIndexedType().getIdentifier();
		final List<CatalogVersionModel> catalogVersions = solrAsCatalogVersionResolver.resolveCatalogVersions(searchQuery);
		final List<CatalogVersionModel> sessionCatalogVersions = facetSearchContext.getParentSessionCatalogVersions() == null
				? Collections.emptyList()
				: new ArrayList<>(facetSearchContext.getParentSessionCatalogVersions());
		final List<CategoryModel> categoryPath = solrAsCategoryPathResolver.resolveCategoryPath(searchQuery, catalogVersions);
		final LanguageModel language = resolveLanguage(searchQuery);
		final CurrencyModel currency = resolveCurrency(searchQuery);

		final AsSearchProfileContext context = asSearchProfileContextFactory.createContext(indexConfiguration, indexType,
				catalogVersions, sessionCatalogVersions, categoryPath, language, currency);

		if (StringUtils.isNotBlank(searchQuery.getUserQuery()))
		{
			context.setQuery(searchQuery.getUserQuery());
			context.setKeywords(convertKeywords(searchQuery.getKeywords()));
		}

		final List<AsSearchProfileActivationGroup> groups = asSearchProfileActivationService
				.getSearchProfileActivationGroupsForContext(context);

		if (CollectionUtils.isNotEmpty(groups))
		{
			final AsSearchProfileResult searchQueryResult = createResultFromFacetSearchContext(context, facetSearchContext);
			final AsSearchProfileResult result = asSearchProfileCalculationService.calculateGroups(context, searchQueryResult,
					groups);

			final Map<String, Object> contextAttributes = facetSearchContext.getAttributes();
			contextAttributes.put(ADAPTIVE_SEARCH_RESULT, result);
			contextAttributes.put(ADAPTIVE_SEARCH_CATALOG_VERSIONS, catalogVersions);
			contextAttributes.put(ADAPTIVE_SEARCH_CATEGORY_PATH, categoryPath);

			applyResult(facetSearchContext, result);
		}
	}

	@Override
	public void afterSearch(final FacetSearchContext facetSearchContext) throws FacetSearchException
	{
		if (facetSearchContext.getFacetSearchConfig().getSearchConfig().isLegacyMode())
		{
			return;
		}

		final SearchResult searchResult = facetSearchContext.getSearchResult();

		final Map<String, Object> contextAttributes = facetSearchContext.getAttributes();
		final Map<String, Object> searchResultAttributes = searchResult.getAttributes();

		final AsSearchProfileResult searchProfileResult = (AsSearchProfileResult) contextAttributes.get(ADAPTIVE_SEARCH_RESULT);

		for (final Document document : searchResult.getDocuments())
		{
			final PK pk = extractPkFromDocument(document);

			if (searchProfileResult.getPromotedItems().containsKey(pk))
			{
				document.getTags().add(AdaptivesearchConstants.PROMOTED_TAG);
				if (searchResult.getCurrentNamedSort() != null && searchResult.getCurrentNamedSort().isHighlightPromotedItems())
				{
					document.getTags().add(AdaptivesearchConstants.HIGHLIGHTED_TAG);
				}

			}
		}

		for (final Facet facet : searchResult.getFacets())
		{
			if (searchProfileResult.getPromotedFacets().containsKey(facet.getName()))
			{
				facet.getTags().add(AdaptivesearchConstants.PROMOTED_TAG);
			}
		}

		searchResultAttributes.put(ADAPTIVE_SEARCH_RESULT, searchProfileResult);
		searchResultAttributes.put(ADAPTIVE_SEARCH_CATALOG_VERSIONS, contextAttributes.get(ADAPTIVE_SEARCH_CATALOG_VERSIONS));
		searchResultAttributes.put(ADAPTIVE_SEARCH_CATEGORY_PATH, contextAttributes.get(ADAPTIVE_SEARCH_CATEGORY_PATH));
	}

	@Override
	public void afterSearchError(final FacetSearchContext facetSearchContext) throws FacetSearchException
	{
		// No implementation.
	}

	protected LanguageModel resolveLanguage(final SearchQuery searchQuery)
	{
		if (searchQuery == null || searchQuery.getLanguage() == null)
		{
			return null;
		}

		return commonI18NService.getLanguage(searchQuery.getLanguage());
	}

	protected CurrencyModel resolveCurrency(final SearchQuery searchQuery)
	{
		if (searchQuery == null || searchQuery.getCurrency() == null)
		{
			return null;
		}

		return commonI18NService.getCurrency(searchQuery.getCurrency());
	}

	protected List<AsKeyword> convertKeywords(final List<Keyword> keywords)
	{
		if (CollectionUtils.isEmpty(keywords))
		{
			return Collections.emptyList();
		}

		return keywords.stream().map(this::convertKeyword).collect(Collectors.toList());
	}

	protected AsKeyword convertKeyword(final Keyword source)
	{
		final AsKeywordModifier[] modifiers = source.getModifiers().stream()
				.map(modifier -> AsKeywordModifier.valueOf(modifier.name())).toArray(AsKeywordModifier[]::new);

		return new AsKeyword(source.getValue(), modifiers);
	}

	protected AsSearchProfileResult createResultFromFacetSearchContext(final AsSearchProfileContext context,
			final FacetSearchContext facetSearchContext)
	{
		final SearchQuery searchQuery = facetSearchContext.getSearchQuery();

		final AsSearchProfileResult result = asSearchProfileCalculationService.createResult(context);
		final AsUidGenerator asUidGenerator = createUidGenerator();

		if (CollectionUtils.isNotEmpty(searchQuery.getFacets()))
		{
			final MergeMap<String, AsConfigurationHolder<AsFacet, AbstractAsFacetConfiguration>> facets = (MergeMap<String, AsConfigurationHolder<AsFacet, AbstractAsFacetConfiguration>>) result
					.getFacets();
			searchQuery.getFacets()
					.forEach(facet -> facets.mergeAfter(facet.getField(), convertFacet(context, facet, asUidGenerator)));
		}

		if (CollectionUtils.isNotEmpty(searchQuery.getBoosts()))
		{
			searchQuery.getBoosts().forEach(boost -> result.getBoostRules().add(convertBoostRule(context, boost, asUidGenerator)));
		}

		final MergeMap<PK, AsConfigurationHolder<AsPromotedItem, AbstractAsBoostItemConfiguration>> promotedItems = (MergeMap<PK, AsConfigurationHolder<AsPromotedItem, AbstractAsBoostItemConfiguration>>) result
				.getPromotedItems();
		if (CollectionUtils.isNotEmpty(searchQuery.getPromotedItems()))
		{
			searchQuery.getPromotedItems().forEach(promotedItemPk -> promotedItems.mergeAfter(promotedItemPk,
					convertBoostItem(context, promotedItemPk, asUidGenerator, AsPromotedItem::new)));
		}

		final MergeMap<PK, AsConfigurationHolder<AsExcludedItem, AbstractAsBoostItemConfiguration>> excludedItems = (MergeMap<PK, AsConfigurationHolder<AsExcludedItem, AbstractAsBoostItemConfiguration>>) result
				.getExcludedItems();
		if (CollectionUtils.isNotEmpty(searchQuery.getExcludedItems()))
		{
			searchQuery.getExcludedItems().forEach(excludedItemPk -> excludedItems.mergeAfter(excludedItemPk,
					convertBoostItem(context, excludedItemPk, asUidGenerator, AsExcludedItem::new)));
		}

		final MergeMap<String, AsConfigurationHolder<AsSort, AbstractAsSortConfiguration>> sorts = (MergeMap<String, AsConfigurationHolder<AsSort, AbstractAsSortConfiguration>>) result
				.getSorts();
		if (CollectionUtils.isNotEmpty(facetSearchContext.getAvailableNamedSorts()))
		{
			facetSearchContext.getAvailableNamedSorts()
					.forEach(sort -> sorts.mergeAfter(sort.getCode(), convertSort(context, sort, asUidGenerator)));
		}

		return result;
	}

	protected AsConfigurationHolder<AsFacet, AbstractAsFacetConfiguration> convertFacet(final AsSearchProfileContext context,
			final FacetField facetField, final AsUidGenerator asUidGenerator)
	{
		final AsFacet facet = new AsFacet();
		facet.setUid(asUidGenerator.generateUid());
		facet.setIndexProperty(facetField.getField());
		facet.setPriority(facetField.getPriority() != null ? facetField.getPriority() : Integer.valueOf(DEFAULT_FACET_PRIORITY));
		facet.setValuesDisplayNameProvider(facetField.getDisplayNameProvider());
		facet.setValuesSortProvider(facetField.getSortProvider());
		facet.setFacetType(solrAsTypeMappingRegistry.toAsFacetType(facetField.getFacetType()));

		facet.setPromotedValues(convertAll(facetField.getPromotedValues(),
				value -> convertFacetValue(value, asUidGenerator, AsPromotedFacetValue::new)));
		facet.setExcludedValues(convertAll(facetField.getExcludedValues(),
				value -> convertFacetValue(value, asUidGenerator, AsExcludedFacetValue::new)));

		return asSearchProfileCalculationService.createConfigurationHolder(context, facet, facetField);
	}

	protected <T extends AbstractAsFacetValueConfiguration> T convertFacetValue(final String value,
			final AsUidGenerator asUidGenerator, final Supplier<T> createSupplier)
	{
		final T facetValue = createSupplier.get();
		facetValue.setUid(asUidGenerator.generateUid());
		facetValue.setValue(value);

		return facetValue;
	}

	protected AsConfigurationHolder<AsBoostRule, AbstractAsBoostRuleConfiguration> convertBoostRule(
			final AsSearchProfileContext context, final BoostField boostField, final AsUidGenerator asUidGenerator)
	{
		final AsBoostRule boost = new AsBoostRule();
		boost.setUid(asUidGenerator.generateUid());
		boost.setIndexProperty(boostField.getField());
		boost.setOperator(solrAsTypeMappingRegistry.toAsBoostOperator(boostField.getQueryOperator()));
		boost.setValue(String.valueOf(boostField.getValue()));
		boost.setBoostType(solrAsTypeMappingRegistry.toAsBoostType(boostField.getBoostType()));
		boost.setBoost(boostField.getBoostValue());

		return asSearchProfileCalculationService.createConfigurationHolder(context, boost, boostField);
	}

	protected <T extends AbstractAsBoostItemConfiguration> AsConfigurationHolder<T, AbstractAsBoostItemConfiguration> convertBoostItem(
			final AsSearchProfileContext context, final PK promotedItemPk, final AsUidGenerator asUidGenerator,
			final Supplier<T> createSupplier)
	{
		final T boostItem = createSupplier.get();
		boostItem.setUid(asUidGenerator.generateUid());
		boostItem.setItemPk(promotedItemPk);

		return asSearchProfileCalculationService.createConfigurationHolder(context, boostItem);
	}

	protected AsConfigurationHolder<AsSort, AbstractAsSortConfiguration> convertSort(final AsSearchProfileContext context,
			final IndexedTypeSort indexedTypeSort, final AsUidGenerator asUidGenerator)
	{
		final AsSort sort = new AsSort();
		sort.setUid(asUidGenerator.generateUid());
		sort.setCode(indexedTypeSort.getCode());
		sort.setName(indexedTypeSort.getLocalizedName());
		sort.setPriority(Integer.valueOf(DEFAULT_SORT_PRIORITY));
		sort.setApplyPromotedItems(indexedTypeSort.isApplyPromotedItems());
		sort.setHighlightPromotedItems(indexedTypeSort.isHighlightPromotedItems());
		sort.setExpressions(indexedTypeSort.getFields().stream().map(this::convertSortExpression).collect(Collectors.toList()));

		return asSearchProfileCalculationService.createConfigurationHolder(context, sort, indexedTypeSort);

	}

	protected AsSortExpression convertSortExpression(final IndexedTypeSortField indexedTypeSortField)
	{
		final AsSortExpression asSortExpression = new AsSortExpression();
		asSortExpression.setExpression(indexedTypeSortField.getFieldName());
		asSortExpression.setOrder(indexedTypeSortField.isAscending() ? AsSortOrder.ASCENDING : AsSortOrder.DESCENDING);
		return asSortExpression;
	}

	protected void applyResult(final FacetSearchContext facetSearchContext, final AsSearchProfileResult result)
	{
		applyFacets(facetSearchContext, result);
		applyBoostItems(facetSearchContext, result);
		applyBoostRules(facetSearchContext, result);
		applySorts(facetSearchContext, result);
	}

	protected void applyFacets(final FacetSearchContext facetSearchContext, final AsSearchProfileResult result)
	{
		final SearchQuery searchQuery = facetSearchContext.getSearchQuery();
		searchQuery.getFacets().clear();

		int priority = Integer.MAX_VALUE;

		if (MapUtils.isNotEmpty(result.getPromotedFacets()))
		{

			final MergeMap<String, AsConfigurationHolder<AsPromotedFacet, AbstractAsFacetConfiguration>> promotedFacets = (MergeMap<String, AsConfigurationHolder<AsPromotedFacet, AbstractAsFacetConfiguration>>) result
					.getPromotedFacets();
			for (final AsConfigurationHolder<AsPromotedFacet, AbstractAsFacetConfiguration> promotedFacet : promotedFacets
					.orderedValues())
			{
				if (isValidFacet(facetSearchContext, promotedFacet))
				{
					final FacetField facetField = createFacetField(promotedFacet);
					facetField.setPriority(Integer.valueOf(priority));
					searchQuery.addFacet(facetField);

					priority--;
				}
			}
		}

		if (MapUtils.isNotEmpty(result.getFacets()))
		{
			final MergeMap<String, AsConfigurationHolder<AsFacet, AbstractAsFacetConfiguration>> facets = (MergeMap<String, AsConfigurationHolder<AsFacet, AbstractAsFacetConfiguration>>) result
					.getFacets();
			for (final AsConfigurationHolder<AsFacet, AbstractAsFacetConfiguration> facet : facets.orderedValues())
			{
				if (isValidFacet(facetSearchContext, facet))
				{
					final FacetField facetField = createFacetField(facet);
					facetField.setPriority(Integer.valueOf(priority));
					searchQuery.addFacet(facetField);

					priority--;
				}
			}
		}
	}

	protected boolean isValidFacet(final FacetSearchContext facetSearchContext,
			final AsConfigurationHolder<? extends AbstractAsFacetConfiguration, AbstractAsFacetConfiguration> facet)
	{
		final Map<String, IndexedProperty> indexedProperties = facetSearchContext.getIndexedType().getIndexedProperties();

		if (!indexedProperties.containsKey(facet.getConfiguration().getIndexProperty()))
		{
			LOG.warn("Facet {} is not longer valid!", facet.getConfiguration().getIndexProperty());
			return false;
		}

		return true;
	}

	protected FacetField createFacetField(
			final AsConfigurationHolder<? extends AbstractAsFacetConfiguration, AbstractAsFacetConfiguration> facetHolder)
	{
		if (facetHolder.getData() instanceof FacetField)
		{
			return (FacetField) facetHolder.getData();
		}

		final AbstractAsFacetConfiguration facet = facetHolder.getConfiguration();

		final FacetField facetField = new FacetField(facet.getIndexProperty(),
				solrAsTypeMappingRegistry.toFacetType(facet.getFacetType()));
		facetField.setPriority(facet.getPriority());
		facetField.setDisplayNameProvider(facet.getValuesDisplayNameProvider());
		facetField.setSortProvider(facet.getValuesSortProvider());
		facetField.setTopValuesProvider(facet.getTopValuesProvider());
		facetField.setPromotedValues(convertAll(facet.getPromotedValues(), AbstractAsFacetValueConfiguration::getValue));
		facetField.setExcludedValues(convertAll(facet.getExcludedValues(), AbstractAsFacetValueConfiguration::getValue));

		return facetField;
	}

	protected void applyBoostItems(final FacetSearchContext facetSearchContext, final AsSearchProfileResult result)
	{
		final SearchQuery searchQuery = facetSearchContext.getSearchQuery();
		searchQuery.getPromotedItems().clear();
		searchQuery.getExcludedItems().clear();

		if (MapUtils.isNotEmpty(result.getPromotedItems()))
		{
			final MergeMap<PK, AsConfigurationHolder<AsPromotedItem, AbstractAsBoostItemConfiguration>> promotedItems = (MergeMap<PK, AsConfigurationHolder<AsPromotedItem, AbstractAsBoostItemConfiguration>>) result
					.getPromotedItems();
			for (final AsConfigurationHolder<AsPromotedItem, AbstractAsBoostItemConfiguration> promotedItem : promotedItems
					.orderedValues())
			{
				searchQuery.addPromotedItem(promotedItem.getConfiguration().getItemPk());
			}
		}

		if (MapUtils.isNotEmpty(result.getExcludedItems()))
		{
			final MergeMap<PK, AsConfigurationHolder<AsExcludedItem, AbstractAsBoostItemConfiguration>> excludedItems = (MergeMap<PK, AsConfigurationHolder<AsExcludedItem, AbstractAsBoostItemConfiguration>>) result
					.getExcludedItems();
			for (final AsConfigurationHolder<AsExcludedItem, AbstractAsBoostItemConfiguration> excludedItem : excludedItems
					.orderedValues())
			{
				searchQuery.addExcludedItem(excludedItem.getConfiguration().getItemPk());
			}
		}
	}

	protected void applyBoostRules(final FacetSearchContext facetSearchContext, final AsSearchProfileResult result)
	{
		final SearchQuery searchQuery = facetSearchContext.getSearchQuery();
		searchQuery.getBoosts().clear();

		if (CollectionUtils.isNotEmpty(result.getBoostRules()))
		{
			result.getBoostRules().forEach(boostRule -> {
				if (isValidBoostRule(facetSearchContext, boostRule))
				{
					searchQuery.addBoost(createBoostField(boostRule));
				}
			});
		}
	}

	protected boolean isValidBoostRule(final FacetSearchContext facetSearchContext,
			final AsConfigurationHolder<AsBoostRule, AbstractAsBoostRuleConfiguration> boostRule)
	{
		final Map<String, IndexedProperty> indexedProperties = facetSearchContext.getIndexedType().getIndexedProperties();

		if (!indexedProperties.containsKey(boostRule.getConfiguration().getIndexProperty()))
		{
			LOG.warn("Boost rule {}{}{} is not longer valid!", boostRule.getConfiguration().getIndexProperty(),
					boostRule.getConfiguration().getOperator(), boostRule.getConfiguration().getValue());
			return false;
		}

		return true;
	}

	protected BoostField createBoostField(
			final AsConfigurationHolder<? extends AsBoostRule, AbstractAsBoostRuleConfiguration> boostRuleHolder)
	{
		if (boostRuleHolder.getData() instanceof BoostField)
		{
			return (BoostField) boostRuleHolder.getData();
		}

		final AsBoostRule boostRule = boostRuleHolder.getConfiguration();

		return new BoostField(boostRule.getIndexProperty(), solrAsTypeMappingRegistry.toQueryOperator(boostRule.getOperator()),
				boostRule.getValue(), boostRule.getBoost(), solrAsTypeMappingRegistry.toBoostType(boostRule.getBoostType()));
	}

	protected void applySorts(final FacetSearchContext facetSearchContext, final AsSearchProfileResult result)
	{
		facetSearchContext.getAvailableNamedSorts().clear();

		if (MapUtils.isNotEmpty(result.getPromotedSorts()))
		{
			final MergeMap<String, AsConfigurationHolder<AsPromotedSort, AbstractAsSortConfiguration>> promotedSorts = (MergeMap<String, AsConfigurationHolder<AsPromotedSort, AbstractAsSortConfiguration>>) result
					.getPromotedSorts();
			for (final AsConfigurationHolder<AsPromotedSort, AbstractAsSortConfiguration> sort : promotedSorts.orderedValues())
			{
				facetSearchContext.getAvailableNamedSorts().add(createIndexedTypeSort(facetSearchContext, sort));
			}
		}

		if (MapUtils.isNotEmpty(result.getSorts()))
		{
			final MergeMap<String, AsConfigurationHolder<AsSort, AbstractAsSortConfiguration>> sorts = (MergeMap<String, AsConfigurationHolder<AsSort, AbstractAsSortConfiguration>>) result
					.getSorts();
			for (final AsConfigurationHolder<AsSort, AbstractAsSortConfiguration> sort : sorts.orderedValues())
			{
				facetSearchContext.getAvailableNamedSorts().add(createIndexedTypeSort(facetSearchContext, sort));
			}
		}
	}

	protected IndexedTypeSort createIndexedTypeSort(final FacetSearchContext facetSearchContext,
			final AsConfigurationHolder<? extends AbstractAsSortConfiguration, AbstractAsSortConfiguration> sortHolder)
	{
		if (sortHolder.getData() instanceof IndexedTypeSort)
		{
			return (IndexedTypeSort) sortHolder.getData();
		}

		final AbstractAsSortConfiguration sort = sortHolder.getConfiguration();

		final IndexedTypeSort indexedTypeSort = new IndexedTypeSort();
		indexedTypeSort.setCode(sort.getCode());
		indexedTypeSort.setName(sort.getName().get(facetSearchContext.getSearchQuery().getLanguage()));
		indexedTypeSort.setApplyPromotedItems(sort.isApplyPromotedItems());
		indexedTypeSort.setHighlightPromotedItems(sort.isHighlightPromotedItems());
		indexedTypeSort
				.setFields(sort.getExpressions().stream().filter(expression -> isValidSortExpression(facetSearchContext, expression))
						.map(this::createIndexedTypeSortField).collect(Collectors.toList()));
		return indexedTypeSort;
	}

	protected boolean isValidSortExpression(final FacetSearchContext facetSearchContext, final AsSortExpression sortExpression)
	{
		final Map<String, IndexedProperty> indexedProperties = facetSearchContext.getIndexedType().getIndexedProperties();

		if (!(StringUtils.equals(SCORE_FIELD, sortExpression.getExpression())
				|| indexedProperties.containsKey(sortExpression.getExpression())))
		{
			LOG.warn("Sort expression {} is not longer valid!", sortExpression.getExpression());
			return false;
		}

		return true;
	}

	protected IndexedTypeSortField createIndexedTypeSortField(final AsSortExpression sortExpression)
	{
		final IndexedTypeSortField indexedTypeSortField = new IndexedTypeSortField();
		indexedTypeSortField.setFieldName(sortExpression.getExpression());
		indexedTypeSortField.setAscending(Objects.equals(sortExpression.getOrder(), AsSortOrder.ASCENDING));
		return indexedTypeSortField;
	}

	protected <S, T> List<T> convertAll(final Collection<? extends S> source, final Function<S, T> converter)
	{
		if (source == null || source.isEmpty())
		{
			return Collections.emptyList();
		}

		return source.stream().map(converter::apply).collect(Collectors.toList());
	}

	protected PK extractPkFromDocument(final Document document)
	{
		final Object pk = document.getFields().get(AdaptivesearchConstants.PK_FIELD);
		if (pk instanceof Long)
		{
			return PK.fromLong(((Long) pk).longValue());
		}

		return null;
	}

	public SolrAsCatalogVersionResolver getSolrAsCatalogVersionResolver()
	{
		return solrAsCatalogVersionResolver;
	}

	@Required
	public void setSolrAsCatalogVersionResolver(final SolrAsCatalogVersionResolver solrAsCatalogVersionResolver)
	{
		this.solrAsCatalogVersionResolver = solrAsCatalogVersionResolver;
	}

	public SolrAsCategoryPathResolver getSolrAsCategoryPathResolver()
	{
		return solrAsCategoryPathResolver;
	}

	@Required
	public void setSolrAsCategoryPathResolver(final SolrAsCategoryPathResolver solrAsCategoryPathResolver)
	{
		this.solrAsCategoryPathResolver = solrAsCategoryPathResolver;
	}

	public SolrAsTypeMappingRegistry getSolrAsTypeMappingRegistry()
	{
		return solrAsTypeMappingRegistry;
	}

	@Required
	public void setSolrAsTypeMappingRegistry(final SolrAsTypeMappingRegistry solrAsTypeMappingRegistry)
	{
		this.solrAsTypeMappingRegistry = solrAsTypeMappingRegistry;
	}

	public AsSearchProfileContextFactory getAsSearchProfileContextFactory()
	{
		return asSearchProfileContextFactory;
	}

	@Required
	public void setAsSearchProfileContextFactory(final AsSearchProfileContextFactory asSearchProfileContextFactory)
	{
		this.asSearchProfileContextFactory = asSearchProfileContextFactory;
	}

	public AsSearchProfileActivationService getAsSearchProfileActivationService()
	{
		return asSearchProfileActivationService;
	}

	@Required
	public void setAsSearchProfileActivationService(final AsSearchProfileActivationService asSearchProfileActivationService)
	{
		this.asSearchProfileActivationService = asSearchProfileActivationService;
	}

	public AsSearchProfileCalculationService getAsSearchProfileCalculationService()
	{
		return asSearchProfileCalculationService;
	}

	@Required
	public void setAsSearchProfileCalculationService(final AsSearchProfileCalculationService asSearchProfileCalculationService)
	{
		this.asSearchProfileCalculationService = asSearchProfileCalculationService;
	}

	public CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}

	protected AsUidGenerator createUidGenerator()
	{
		return new SearchQueryUidGenerator();
	}

	protected static class SearchQueryUidGenerator implements AsUidGenerator
	{
		private int nextUid = 0;

		@Override
		public String generateUid()
		{
			nextUid++;
			return "_query_uid" + nextUid + "_";
		}
	}
}
