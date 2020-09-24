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
package de.hybris.platform.adaptivesearchsolr.strategies.impl;

import static de.hybris.platform.adaptivesearchsolr.constants.AdaptivesearchsolrConstants.ALL_CATEGORIES_FIELD;
import static de.hybris.platform.adaptivesearchsolr.constants.AdaptivesearchsolrConstants.SCORE_FIELD;

import de.hybris.platform.adaptivesearch.AsException;
import de.hybris.platform.adaptivesearch.AsRuntimeException;
import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;
import de.hybris.platform.adaptivesearch.data.AsDocumentData;
import de.hybris.platform.adaptivesearch.data.AsExpressionData;
import de.hybris.platform.adaptivesearch.data.AsFacetData;
import de.hybris.platform.adaptivesearch.data.AsFacetValueData;
import de.hybris.platform.adaptivesearch.data.AsFacetVisibility;
import de.hybris.platform.adaptivesearch.data.AsIndexConfigurationData;
import de.hybris.platform.adaptivesearch.data.AsIndexPropertyData;
import de.hybris.platform.adaptivesearch.data.AsIndexTypeData;
import de.hybris.platform.adaptivesearch.data.AsSearchProfileResult;
import de.hybris.platform.adaptivesearch.data.AsSearchQueryData;
import de.hybris.platform.adaptivesearch.data.AsSearchResultData;
import de.hybris.platform.adaptivesearch.data.AsSortData;
import de.hybris.platform.adaptivesearch.enums.AsBoostOperator;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProvider;
import de.hybris.platform.adaptivesearchsolr.strategies.SolrAsTypeMappingRegistry;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfigService;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.IndexedTypeSort;
import de.hybris.platform.solrfacetsearch.config.exceptions.FacetConfigServiceException;
import de.hybris.platform.solrfacetsearch.daos.SolrFacetSearchConfigDao;
import de.hybris.platform.solrfacetsearch.daos.SolrIndexedPropertyDao;
import de.hybris.platform.solrfacetsearch.daos.SolrIndexedTypeDao;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedPropertyModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedTypeModel;
import de.hybris.platform.solrfacetsearch.search.Document;
import de.hybris.platform.solrfacetsearch.search.Facet;
import de.hybris.platform.solrfacetsearch.search.FacetSearchException;
import de.hybris.platform.solrfacetsearch.search.FacetSearchService;
import de.hybris.platform.solrfacetsearch.search.FacetValue;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.SearchResult;
import de.hybris.platform.solrfacetsearch.solr.IndexedPropertyTypeInfo;
import de.hybris.platform.solrfacetsearch.solr.SolrIndexedPropertyTypeRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation for {@link AsSearchProvider}
 */
public class SolrAsSearchProvider implements AsSearchProvider
{
	private static final Logger LOG = LoggerFactory.getLogger(SolrAsSearchProvider.class);

	protected static final String INDEX_CONFIGURATION_PARAM = "indexConfiguration";
	protected static final String INDEX_TYPE_PARAM = "indexType";
	protected static final String CODE_PARAM = "code";
	protected static final String EXPRESSION_PARAM = "expression";

	protected static final String DEFAULT_QUERY_TEMPLATE = "DEFAULT";
	protected static final String ADAPTIVE_SEARCH_RESULT = "adaptiveSearchResult";
	protected static final String ADAPTIVE_SEARCH_CATALOG_VERSIONS = "adaptiveSearchCatalogVersions";
	protected static final String ADAPTIVE_SEARCH_CATEGORY_PATH = "adaptiveSearchCategoryPath";
	protected static final String ADAPTIVE_SEARCH_KEEP_EXCLUDED_VALUES_KEY = "adaptiveSearchKeepExcludedValues";

	protected static final Pattern VALID_INDEX_PROPERTY_NAME_REGEX_PATTERN = Pattern.compile("^(?![0-9_])[a-zA-Z0-9_]*(?<!_)$");

	private SolrFacetSearchConfigDao solrFacetSearchConfigDao;
	private SolrIndexedTypeDao solrIndexedTypeDao;
	private SolrIndexedPropertyDao solrIndexedPropertyDao;
	private SolrIndexedPropertyTypeRegistry solrIndexedPropertyTypeRegistry;
	private SolrAsTypeMappingRegistry solrAsTypeMappingRegistry;
	private FacetSearchService facetSearchService;
	private FacetSearchConfigService facetSearchConfigService;

	private SessionService sessionService;
	private I18NService i18nService;
	private CommonI18NService commonI18NService;

	@Override
	public List<AsIndexConfigurationData> getIndexConfigurations()
	{
		final List<SolrFacetSearchConfigModel> solrIndexConfigurations = solrFacetSearchConfigDao.findAllFacetSearchConfigs();

		return solrIndexConfigurations.stream().map(this::convertIndexConfiguration).collect(Collectors.toList());
	}

	@Override
	public Optional<AsIndexConfigurationData> getIndexConfigurationForCode(final String code)
	{
		ServicesUtil.validateParameterNotNullStandardMessage(CODE_PARAM, code);

		try
		{
			final SolrFacetSearchConfigModel solrIndexConfiguration = solrFacetSearchConfigDao.findFacetSearchConfigByName(code);

			return Optional.of(convertIndexConfiguration(solrIndexConfiguration));
		}
		catch (final UnknownIdentifierException e)
		{
			LOG.debug("Index configuration not found", e);
			return Optional.empty();
		}
	}

	@Override
	public List<AsIndexTypeData> getIndexTypes()
	{
		final List<SolrIndexedTypeModel> solrIndexTypes = solrIndexedTypeDao.findAllIndexedTypes();

		return solrIndexTypes.stream().map(this::convertIndexType).collect(Collectors.toList());
	}

	@Override
	public List<AsIndexTypeData> getIndexTypes(final String indexConfiguration)
	{
		ServicesUtil.validateParameterNotNullStandardMessage(INDEX_CONFIGURATION_PARAM, indexConfiguration);

		final SolrFacetSearchConfigModel solrIndexConfiguration = solrFacetSearchConfigDao
				.findFacetSearchConfigByName(indexConfiguration);
		final List<SolrIndexedTypeModel> solrIndexTypes = solrIndexConfiguration.getSolrIndexedTypes();

		if (CollectionUtils.isEmpty(solrIndexTypes))
		{
			return Collections.emptyList();
		}

		return solrIndexTypes.stream().map(this::convertIndexType).collect(Collectors.toList());
	}

	@Override
	public Optional<AsIndexTypeData> getIndexTypeForCode(final String code)
	{
		ServicesUtil.validateParameterNotNullStandardMessage(CODE_PARAM, code);

		try
		{
			final SolrIndexedTypeModel solrIndexType = solrIndexedTypeDao.findIndexedTypeByIdentifier(code);

			return Optional.of(convertIndexType(solrIndexType));
		}
		catch (final UnknownIdentifierException e)
		{
			LOG.debug("Index type not found", e);
			return Optional.empty();
		}
	}

	@Override
	public List<AsIndexPropertyData> getIndexProperties(final String indexType)
	{
		ServicesUtil.validateParameterNotNullStandardMessage(INDEX_TYPE_PARAM, indexType);

		final SolrIndexedTypeModel solrIndexType = solrIndexedTypeDao.findIndexedTypeByIdentifier(indexType);
		final List<SolrIndexedPropertyModel> solrIndexProperties = solrIndexedPropertyDao
				.findIndexedPropertiesByIndexedType(solrIndexType);

		return solrIndexProperties.stream().map(this::convertIndexProperty).sorted(this::compareIndexProperties)
				.collect(Collectors.toList());
	}

	@Override
	public Optional<AsIndexPropertyData> getIndexPropertyForCode(final String indexType, final String code)
	{
		ServicesUtil.validateParameterNotNullStandardMessage(INDEX_TYPE_PARAM, indexType);
		ServicesUtil.validateParameterNotNullStandardMessage(CODE_PARAM, code);

		try
		{
			final SolrIndexedTypeModel solrIndexType = solrIndexedTypeDao.findIndexedTypeByIdentifier(indexType);
			final SolrIndexedPropertyModel solrIndexProperty = solrIndexedPropertyDao.findIndexedPropertyByName(solrIndexType, code);

			return Optional.of(convertIndexProperty(solrIndexProperty));
		}
		catch (final UnknownIdentifierException e)
		{
			LOG.debug("Index property not found", e);
			return Optional.empty();
		}
	}

	@Override
	public List<CatalogVersionModel> getSupportedCatalogVersions(final String indexConfiguration, final String indexType)
	{
		ServicesUtil.validateParameterNotNullStandardMessage(INDEX_CONFIGURATION_PARAM, indexConfiguration);
		ServicesUtil.validateParameterNotNullStandardMessage(INDEX_TYPE_PARAM, indexType);

		final SolrFacetSearchConfigModel solrIndexConfiguration = solrFacetSearchConfigDao
				.findFacetSearchConfigByName(indexConfiguration);

		return solrIndexConfiguration.getCatalogVersions();
	}

	@Override
	public List<LanguageModel> getSupportedLanguages(final String indexConfiguration, final String indexType)
	{
		ServicesUtil.validateParameterNotNullStandardMessage(INDEX_CONFIGURATION_PARAM, indexConfiguration);
		ServicesUtil.validateParameterNotNullStandardMessage(INDEX_TYPE_PARAM, indexType);

		final SolrFacetSearchConfigModel solrIndexConfiguration = solrFacetSearchConfigDao
				.findFacetSearchConfigByName(indexConfiguration);

		return solrIndexConfiguration.getLanguages();
	}

	@Override
	public List<CurrencyModel> getSupportedCurrencies(final String indexConfiguration, final String indexType)
	{
		ServicesUtil.validateParameterNotNullStandardMessage(INDEX_CONFIGURATION_PARAM, indexConfiguration);
		ServicesUtil.validateParameterNotNullStandardMessage(INDEX_TYPE_PARAM, indexType);

		final SolrFacetSearchConfigModel solrIndexConfiguration = solrFacetSearchConfigDao
				.findFacetSearchConfigByName(indexConfiguration);

		return solrIndexConfiguration.getCurrencies();
	}

	@Override
	public List<AsIndexPropertyData> getSupportedFacetIndexProperties(final String indexType)
	{
		ServicesUtil.validateParameterNotNullStandardMessage(INDEX_TYPE_PARAM, indexType);

		final SolrIndexedTypeModel solrIndexType = solrIndexedTypeDao.findIndexedTypeByIdentifier(indexType);
		final List<SolrIndexedPropertyModel> solrIndexProperties = solrIndexedPropertyDao
				.findIndexedPropertiesByIndexedType(solrIndexType);

		return solrIndexProperties.stream().filter(this::isValidFacetIndexProperty).map(this::convertIndexProperty)
				.sorted(this::compareIndexProperties).collect(Collectors.toList());
	}

	@Override
	public boolean isValidFacetIndexProperty(final String indexType, final String code)
	{
		ServicesUtil.validateParameterNotNullStandardMessage(INDEX_TYPE_PARAM, indexType);
		ServicesUtil.validateParameterNotNullStandardMessage(CODE_PARAM, code);

		try
		{
			final SolrIndexedTypeModel solrIndexType = solrIndexedTypeDao.findIndexedTypeByIdentifier(indexType);
			final SolrIndexedPropertyModel solrIndexProperty = solrIndexedPropertyDao.findIndexedPropertyByName(solrIndexType, code);

			return isValidFacetIndexProperty(solrIndexProperty);
		}
		catch (final UnknownIdentifierException e)
		{
			LOG.debug("Index property not found", e);
			return false;
		}
	}

	protected boolean isValidFacetIndexProperty(final SolrIndexedPropertyModel indexProperty)
	{
		final IndexedPropertyTypeInfo indexedPropertyType = solrIndexedPropertyTypeRegistry
				.getIndexPropertyTypeInfo(indexProperty.getType().getCode());

		return indexedPropertyType.isAllowFacet();
	}

	@Override
	public List<AsExpressionData> getSupportedSortExpressions(final String indexType)
	{
		ServicesUtil.validateParameterNotNullStandardMessage(INDEX_TYPE_PARAM, indexType);

		final SolrIndexedTypeModel solrIndexType = solrIndexedTypeDao.findIndexedTypeByIdentifier(indexType);
		final List<SolrIndexedPropertyModel> solrIndexProperties = solrIndexedPropertyDao
				.findIndexedPropertiesByIndexedType(solrIndexType);

		final List<AsExpressionData> expressions = solrIndexProperties.stream().filter(this::isValidSortIndexProperty)
				.map(this::convertSortExpression).sorted(this::compareSortExpressions).collect(Collectors.toList());

		// adds score to the top
		expressions.add(0, createScoreSortExpression());

		return expressions;
	}

	@Override
	public boolean isValidSortExpression(final String indexType, final String expression)
	{
		ServicesUtil.validateParameterNotNullStandardMessage(INDEX_TYPE_PARAM, indexType);
		ServicesUtil.validateParameterNotNullStandardMessage(EXPRESSION_PARAM, expression);

		if (StringUtils.equals(SCORE_FIELD, expression))
		{
			return true;
		}

		try
		{
			final SolrIndexedTypeModel solrIndexType = solrIndexedTypeDao.findIndexedTypeByIdentifier(indexType);
			final SolrIndexedPropertyModel solrIndexProperty = solrIndexedPropertyDao.findIndexedPropertyByName(solrIndexType,
					expression);

			return isValidSortIndexProperty(solrIndexProperty);
		}
		catch (final UnknownIdentifierException e)
		{
			LOG.debug("Index property not found", e);
			return false;
		}
	}



	protected boolean isValidSortIndexProperty(final SolrIndexedPropertyModel indexProperty)
	{
		return !indexProperty.isMultiValue() && VALID_INDEX_PROPERTY_NAME_REGEX_PATTERN.matcher(indexProperty.getName()).matches();
	}

	protected AsIndexConfigurationData convertIndexConfiguration(final SolrFacetSearchConfigModel solrIndexConfiguration)
	{
		final AsIndexConfigurationData indexConfiguration = new AsIndexConfigurationData();
		indexConfiguration.setCode(solrIndexConfiguration.getName());
		indexConfiguration.setName(solrIndexConfiguration.getDescription() != null ? solrIndexConfiguration.getDescription()
				: solrIndexConfiguration.getName());

		return indexConfiguration;
	}

	protected AsIndexTypeData convertIndexType(final SolrIndexedTypeModel source)
	{
		final ComposedTypeModel itemType = source.getType();

		final AsIndexTypeData target = new AsIndexTypeData();
		target.setCode(source.getIdentifier());
		target.setName(source.getIdentifier());
		target.setItemType(itemType.getCode());
		target.setCatalogVersionAware(BooleanUtils.isTrue(itemType.getCatalogItemType()));

		return target;
	}

	protected AsIndexPropertyData convertIndexProperty(final SolrIndexedPropertyModel source)
	{
		final AsIndexPropertyData target = new AsIndexPropertyData();
		target.setCode(source.getName());
		target.setName(source.getDisplayName());

		final IndexedPropertyTypeInfo indexedPropertyType = solrIndexedPropertyTypeRegistry
				.getIndexPropertyTypeInfo(source.getType().getCode());
		target.setType(indexedPropertyType.getJavaType());

		if (CollectionUtils.isNotEmpty(indexedPropertyType.getSupportedQueryOperators()))
		{
			final EnumSet<AsBoostOperator> supportedBoostOperators = indexedPropertyType.getSupportedQueryOperators().stream()
					.map(solrAsTypeMappingRegistry::toAsBoostOperator).filter(Objects::nonNull)
					.collect(Collectors.toCollection(() -> EnumSet.noneOf(AsBoostOperator.class)));

			target.setSupportedBoostOperators(supportedBoostOperators);
		}
		else
		{
			target.setSupportedBoostOperators(Collections.emptySet());
		}

		return target;
	}

	protected int compareIndexProperties(final AsIndexPropertyData indexProperty1, final AsIndexPropertyData indexProperty2)
	{
		return indexProperty1.getCode().compareTo(indexProperty2.getCode());
	}

	protected AsExpressionData createScoreSortExpression()
	{
		final AsExpressionData target = new AsExpressionData();
		target.setExpression(SCORE_FIELD);
		target.setName(SCORE_FIELD);

		return target;
	}

	protected AsExpressionData convertSortExpression(final SolrIndexedPropertyModel source)
	{
		final AsExpressionData target = new AsExpressionData();
		target.setExpression(source.getName());
		target.setName(source.getDisplayName());

		return target;
	}

	protected int compareSortExpressions(final AsExpressionData expression1, final AsExpressionData expression2)
	{
		return expression1.getExpression().compareTo(expression2.getExpression());
	}

	@Override
	public AsSearchResultData search(final AsSearchProfileContext context, final AsSearchQueryData searchQuery) throws AsException
	{
		if (context.getLanguage() == null)
		{
			return performSearch(context, searchQuery);
		}
		else
		{
			final Locale localeForLanguage = commonI18NService.getLocaleForLanguage(context.getLanguage());

			return executeInLocalViewWithLocale(localeForLanguage, () -> {
				try
				{
					return performSearch(context, searchQuery);
				}
				catch (final AsException a)
				{
					throw new AsRuntimeException(a);
				}
			});
		}
	}

	protected AsSearchResultData executeInLocalViewWithLocale(final Locale localeForLanguage,
			final Supplier<AsSearchResultData> action) throws AsException
	{
		try
		{
			return sessionService.executeInLocalView(new SessionExecutionBody()
			{
				@Override
				public AsSearchResultData execute()
				{
					i18nService.setLocalizationFallbackEnabled(true);
					i18nService.setCurrentLocale(localeForLanguage);

					return action.get();
				}
			});
		}
		catch (final RuntimeException e)
		{
			if (e.getCause() instanceof AsException)
			{
				throw (AsException) e.getCause();
			}
			else
			{
				throw e;
			}
		}
	}


	public AsSearchResultData performSearch(final AsSearchProfileContext context, final AsSearchQueryData searchQuery)
			throws AsException
	{
		try
		{
			final SearchQuery query = convertSearchQuery(context, searchQuery);
			final SearchResult result = facetSearchService.search(query);

			return convertSearchResult(context, result);
		}
		catch (FacetConfigServiceException | FacetSearchException e)
		{
			throw new AsException(e);
		}
	}

	protected IndexedType resolveIndexedType(final FacetSearchConfig facetSearchConfig, final String identifier) throws AsException
	{
		final Optional<IndexedType> indexedTypeOptional = facetSearchConfig.getIndexConfig().getIndexedTypes().values().stream()
				.filter(type -> type.getIdentifier().equals(identifier)).findFirst();

		if (!indexedTypeOptional.isPresent())
		{
			throw new AsException("Cannot find IndexedType");
		}

		return indexedTypeOptional.get();
	}

	protected CategoryModel resolveSelectedCategory(final List<CategoryModel> categoryPath)
	{
		if (CollectionUtils.isEmpty(categoryPath))
		{
			return null;
		}

		return categoryPath.get(categoryPath.size() - 1);
	}

	protected SearchQuery convertSearchQuery(final AsSearchProfileContext context, final AsSearchQueryData searchQuery)
			throws FacetConfigServiceException, AsException
	{
		final FacetSearchConfig facetSearchConfig = facetSearchConfigService.getConfiguration(context.getIndexConfiguration());
		final IndexedType indexedType = resolveIndexedType(facetSearchConfig, context.getIndexType());

		final SearchQuery query = facetSearchService.createFreeTextSearchQueryFromTemplate(facetSearchConfig, indexedType,
				DEFAULT_QUERY_TEMPLATE, searchQuery.getQuery());
		query.setOffset(searchQuery.getActivePage());
		query.setPageSize(searchQuery.getPageSize());

		if (CollectionUtils.isNotEmpty(context.getCatalogVersions()))
		{
			query.setCatalogVersions(context.getCatalogVersions());
		}

		final LanguageModel language = context.getLanguage();
		if (language != null)
		{
			query.setLanguage(language.getIsocode());
		}

		final CurrencyModel currency = context.getCurrency();
		if (currency != null)
		{
			query.setCurrency(currency.getIsocode());
		}

		query.getHighlightingFields().clear();
		query.addHighlightingField(SearchQuery.ALL_FIELDS);

		if (MapUtils.isNotEmpty(searchQuery.getFacetValues()))
		{
			for (final Entry<String, Set<String>> entry : searchQuery.getFacetValues().entrySet())
			{
				final String indexProperty = entry.getKey();
				final Set<String> facetValues = entry.getValue();

				if (StringUtils.isNotBlank(indexProperty) && CollectionUtils.isNotEmpty(facetValues))
				{
					query.addFacetValue(indexProperty, facetValues);
				}
			}
		}

		final CategoryModel selectedCategory = resolveSelectedCategory(context.getCategoryPath());
		if (selectedCategory != null)
		{
			query.addFilterQuery(ALL_CATEGORIES_FIELD, selectedCategory.getCode());
		}

		query.setNamedSort(searchQuery.getSort());

		return query;
	}

	protected AsSearchResultData convertSearchResult(final AsSearchProfileContext context, final SearchResult result)
	{
		final AsSearchResultData searchResult = new AsSearchResultData();

		searchResult.setActivePage(result.getOffset());
		searchResult.setPageCount((int) result.getNumberOfPages());
		searchResult.setPageSize(result.getPageSize());
		searchResult.setResultCount((int) result.getNumberOfResults());

		searchResult.setResults(convertAll(result.getDocuments(), this::createDocumentData));
		searchResult.setFacets(convertAll(result.getFacets(), this::createFacetData));

		searchResult.setCurrentSort(result.getCurrentNamedSort() != null ? createSortData(result.getCurrentNamedSort()) : null);
		searchResult.setAvailableSorts(convertAll(result.getAvailableNamedSorts(), this::createSortData));

		final Map<String, Object> resultAttributes = result.getAttributes();
		final AsSearchProfileResult searchProfileResult = (AsSearchProfileResult) resultAttributes.get(ADAPTIVE_SEARCH_RESULT);
		final List<CatalogVersionModel> catalogVersions = (List<CatalogVersionModel>) resultAttributes
				.get(ADAPTIVE_SEARCH_CATALOG_VERSIONS);
		final List<CategoryModel> categoryPath = (List<CategoryModel>) resultAttributes.get(ADAPTIVE_SEARCH_CATEGORY_PATH);

		searchResult.setSearchProfileResult(searchProfileResult);
		searchResult.setCatalogVersions(catalogVersions);
		searchResult.setCategoryPath(categoryPath);

		return searchResult;
	}

	protected AsDocumentData createDocumentData(final Document document)
	{
		final AsDocumentData newDoc = new AsDocumentData();
		newDoc.setFields(document.getFields());

		return newDoc;
	}

	protected AsFacetData createFacetData(final Facet facet)
	{
		final AsFacetData newFacet = new AsFacetData();
		newFacet.setIndexProperty(facet.getName());
		newFacet.setName(facet.getName());
		newFacet.setTopValues(convertAll(facet.getTopFacetValues(), this::createFacetValueData));
		newFacet.setValues(convertAll(facet.getFacetValues(), this::createFacetValueData));
		newFacet.setSelectedValues(convertAll(facet.getSelectedFacetValues(), this::createFacetValueData));
		newFacet.setAllValues(convertAll(facet.getAllFacetValues(), this::createFacetValueData));
		newFacet.setVisibility(AsFacetVisibility.SHOW);

		return newFacet;
	}

	protected AsFacetValueData createFacetValueData(final FacetValue facetValue)
	{
		final AsFacetValueData newFacetValue = new AsFacetValueData();
		newFacetValue.setValue(facetValue.getName());
		newFacetValue.setName(facetValue.getDisplayName());
		newFacetValue.setCount(facetValue.getCount());
		newFacetValue.setSelected(facetValue.isSelected());
		newFacetValue.setTags(facetValue.getTags());

		return newFacetValue;
	}

	protected <S, T> List<T> convertAll(final Collection<? extends S> source, final Function<S, T> converter)
	{
		if (source == null || source.isEmpty())
		{
			return Collections.emptyList();
		}

		return source.stream().map(converter::apply).collect(Collectors.toList());
	}

	protected AsSortData createSortData(final IndexedTypeSort sort)
	{
		final AsSortData newSort = new AsSortData();
		newSort.setCode(sort.getCode());
		newSort.setName(sort.getName());
		newSort.setApplyPromotedItems(sort.isApplyPromotedItems());
		newSort.setHighlightPromotedItems(sort.isHighlightPromotedItems());

		return newSort;
	}

	public SolrFacetSearchConfigDao getSolrFacetSearchConfigDao()
	{
		return solrFacetSearchConfigDao;
	}

	@Required
	public void setSolrFacetSearchConfigDao(final SolrFacetSearchConfigDao solrFacetSearchConfigDao)
	{
		this.solrFacetSearchConfigDao = solrFacetSearchConfigDao;
	}

	public SolrIndexedTypeDao getSolrIndexedTypeDao()
	{
		return solrIndexedTypeDao;
	}

	@Required
	public void setSolrIndexedTypeDao(final SolrIndexedTypeDao solrIndexedTypeDao)
	{
		this.solrIndexedTypeDao = solrIndexedTypeDao;
	}

	public SolrIndexedPropertyDao getSolrIndexedPropertyDao()
	{
		return solrIndexedPropertyDao;
	}

	@Required
	public void setSolrIndexedPropertyDao(final SolrIndexedPropertyDao solrIndexedPropertyDao)
	{
		this.solrIndexedPropertyDao = solrIndexedPropertyDao;
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

	public SolrAsTypeMappingRegistry getSolrAsTypeMappingRegistry()
	{
		return solrAsTypeMappingRegistry;
	}

	@Required
	public void setSolrAsTypeMappingRegistry(final SolrAsTypeMappingRegistry solrAsTypeMappingRegistry)
	{
		this.solrAsTypeMappingRegistry = solrAsTypeMappingRegistry;
	}

	public FacetSearchService getFacetSearchService()
	{
		return facetSearchService;
	}

	@Required
	public void setFacetSearchService(final FacetSearchService facetSearchService)
	{
		this.facetSearchService = facetSearchService;
	}

	public FacetSearchConfigService getFacetSearchConfigService()
	{
		return facetSearchConfigService;
	}

	@Required
	public void setFacetSearchConfigService(final FacetSearchConfigService facetSearchConfigService)
	{
		this.facetSearchConfigService = facetSearchConfigService;
	}

	public SessionService getSessionService()
	{
		return sessionService;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}


	public I18NService getI18nService()
	{
		return i18nService;
	}

	@Required
	public void setI18nService(final I18NService i18nService)
	{
		this.i18nService = i18nService;
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
}
