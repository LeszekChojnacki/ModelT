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
package com.hybris.backoffice.solrsearch.services.impl;

import de.hybris.platform.catalog.CatalogTypeService;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.model.AbstractItemModel;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.exceptions.FacetConfigServiceException;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.SearchQuery.Operator;
import de.hybris.platform.solrfacetsearch.search.impl.DefaultFacetSearchService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.OrderComparator;

import com.hybris.backoffice.solrsearch.constants.BackofficesolrsearchConstants;
import com.hybris.backoffice.solrsearch.converters.SearchConditionDataConverter;
import com.hybris.backoffice.solrsearch.converters.impl.DefaultSearchQueryConditionsConverter;
import com.hybris.backoffice.solrsearch.dataaccess.BackofficeSearchQuery;
import com.hybris.backoffice.solrsearch.dataaccess.SearchConditionData;
import com.hybris.backoffice.solrsearch.dataaccess.SolrSearchCondition;
import com.hybris.backoffice.solrsearch.decorators.SearchConditionDecorator;
import com.hybris.backoffice.solrsearch.services.BackofficeFacetSearchConfigService;
import com.hybris.backoffice.solrsearch.services.BackofficeFacetSearchService;
import com.hybris.backoffice.solrsearch.utils.BackofficeSolrUtil;
import com.hybris.cockpitng.search.data.SearchQueryData;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;


/**
 * Backoffice implementation for {@link de.hybris.platform.solrfacetsearch.search.FacetSearchService}
 */
public class DefaultBackofficeFacetSearchService extends DefaultFacetSearchService implements BackofficeFacetSearchService
{

	private static final Logger LOG = LoggerFactory.getLogger(DefaultBackofficeFacetSearchService.class);

	private SearchConditionDataConverter searchConditionDataConverter;
	private DefaultSearchQueryConditionsConverter searchQueryConditionsConverter;
	private BackofficeFacetSearchConfigService facetSearchConfigService;
	private List<SearchConditionDecorator> conditionsDecorators;
	private TypeService typeService;
	private CatalogVersionService catalogVersionService;
	private CatalogTypeService catalogTypeService;
	private UserService userService;
	private Map<String, String> indexedTypeToCatalogVersionPropertyMapping;


	@Override
	public BackofficeSearchQuery createSearchQuery(final FacetSearchConfig facetSearchConfig, final IndexedType indexedType)
	{
		return new BackofficeSearchQuery(facetSearchConfig, indexedType);
	}

	@Override
	public BackofficeSearchQuery createBackofficeSolrSearchQuery(final SearchQueryData queryData)
	{
		try
		{
			final FacetSearchConfig facetSearchConfig = facetSearchConfigService.getFacetSearchConfig(queryData.getSearchType());

			if (facetSearchConfig == null)
			{
				return null;
			}

			final IndexedType indexedType = facetSearchConfigService.getIndexedType(facetSearchConfig, queryData.getSearchType());
			if (indexedType == null)
			{
				return null;
			}

			final BackofficeSearchQuery searchQuery = createSearchQuery(facetSearchConfig, indexedType);
			populateGroupCommandFields(facetSearchConfig, indexedType, searchQuery);
			populateFields(facetSearchConfig, indexedType, searchQuery);
			populateSortFields(facetSearchConfig, indexedType, searchQuery);
			populateFacetFields(facetSearchConfig, indexedType, searchQuery);
			populateSelectedFacets(queryData.getSelectedFacets(), searchQuery);

			if (StringUtils.isNotBlank(queryData.getSearchQueryText()))
			{
				populateFreeTextQuery(facetSearchConfig, indexedType, searchQuery, queryData.getSearchQueryText());
				searchQuery.setUserQuery(queryData.getSearchQueryText());
				searchQuery.setEnableSpellcheck(true);
			}

			searchQuery.setSearchConditionData(prepareSearchConditionData(queryData, indexedType));
			searchQuery.setDefaultOperator(BackofficeSolrUtil.convertToSolrOperator(queryData.getGlobalComparisonOperator()));
			searchQuery.setPageSize(queryData.getPageSize());
			return searchQuery;
		}
		catch (final FacetConfigServiceException e)
		{
			LOG.error("Cannot create solr search pageable", e);
		}
		return null;
	}

	protected void populateSelectedFacets(final Map<String, Set<String>> selectedFacets, final SearchQuery searchQuery)
	{
		if (MapUtils.isNotEmpty(selectedFacets))
		{
			selectedFacets.forEach(searchQuery::addFacetValue);
		}
	}

	protected SearchConditionData prepareSearchConditionData(final SearchQueryData queryData, final IndexedType indexedType)
	{
		final Operator globalOperator = BackofficeSolrUtil.convertToSolrOperator(queryData.getGlobalComparisonOperator());
		final List<SolrSearchCondition> conditions = searchQueryConditionsConverter.convert(queryData.getConditions(),
				globalOperator, indexedType);

		final SearchConditionData searchConditionData = searchConditionDataConverter.convertConditions(conditions, globalOperator);
		prepareTypeCondition(indexedType, queryData).ifPresent(searchConditionData::addFilterQueryCondition);
		if (isCatalogVersionAware(indexedType))
		{
			final UserModel currentUser = getUserService().getCurrentUser();
			if (!getUserService().isAdmin(currentUser))
			{
				final Collection<CatalogVersionModel> readableCVs = getCatalogVersionService()
						.getAllReadableCatalogVersions(currentUser);
				if (CollectionUtils.isNotEmpty(readableCVs))
				{
					prepareCatalogVersionCondition(indexedType, searchConditionData, queryData, readableCVs);
				}
			}
		}

		if (CollectionUtils.isNotEmpty(getConditionsDecorators()))
		{
			getConditionsDecorators().forEach(decorator -> decorator.decorate(searchConditionData, queryData, indexedType));
		}

		return searchConditionData;
	}

	protected boolean isCatalogVersionAware(final IndexedType indexedType)
	{
		final ComposedTypeModel type = indexedType.getComposedType();
		if (getCatalogTypeService().isCatalogVersionAwareType(type))
		{
			final String catalogVersionProperty = resolveCatalogVersionProperty(indexedType);
			final boolean hasCatalogVersion = indexedType.getIndexedProperties().entrySet().stream()
					.anyMatch(entry -> catalogVersionProperty.equals(entry.getKey()));
			if (!hasCatalogVersion)
			{
				LOG.debug("Catalog Version data not found for type: {}", indexedType.getCode());
			}
			return hasCatalogVersion;
		}
		return false;
	}

	private String resolveCatalogVersionProperty(final IndexedType indexedType)
	{

		if (getIndexedTypeToCatalogVersionPropertyMapping().containsKey(indexedType.getIdentifier()))
		{
			return StringUtils.defaultIfBlank(getIndexedTypeToCatalogVersionPropertyMapping().get(indexedType.getIdentifier()),
					BackofficesolrsearchConstants.CATALOG_VERSION_PK);
		}
		return BackofficesolrsearchConstants.CATALOG_VERSION_PK;
	}

	protected Optional<SolrSearchCondition> prepareTypeCondition(final IndexedType indexedType, final SearchQueryData queryData)
	{
		if (!indexedType.getIndexedProperties().containsKey(ComposedTypeModel.ITEMTYPE))
		{
			LOG.warn("No '{}' field found for '{}' indexed type. Too many results may be returned.", ComposedTypeModel.ITEMTYPE,
					indexedType.getIdentifier());
			return Optional.empty();
		}
		return Optional.of(prepareTypeCondition(queryData));
	}

	/**
	 * @deprecated since 1808, use {@link #prepareTypeCondition(IndexedType, SearchQueryData)} instead
	 */
	@Deprecated
	protected SolrSearchCondition prepareTypeCondition(final SearchQueryData queryData)
	{
		final ComposedTypeModel composedTypeForCode = typeService.getComposedTypeForCode(queryData.getSearchType());

		final SolrSearchCondition typeCondition = new SolrSearchCondition(ComposedTypeModel.ITEMTYPE, null,
				SearchQuery.Operator.OR);
		typeCondition.addConditionValue(queryData.getSearchType(), ValueComparisonOperator.EQUALS);
		if (queryData.isIncludeSubtypes())
		{
			composedTypeForCode.getAllSubTypes()
					.forEach(ct -> typeCondition.addConditionValue(ct.getCode(), ValueComparisonOperator.EQUALS));
		}

		return typeCondition;
	}

	protected void prepareCatalogVersionCondition(final IndexedType indexedType, final SearchConditionData searchConditionData,
			final SearchQueryData queryData, final Collection<CatalogVersionModel> readableCatalogVersions)
	{
		final String catalogVersionProperty = resolveCatalogVersionProperty(indexedType);
		final Set<PK> cvPKs = readableCatalogVersions.stream().map(AbstractItemModel::getPk).collect(Collectors.toSet());

		final SolrSearchCondition cvCondition = new SolrSearchCondition(catalogVersionProperty, null, SearchQuery.Operator.OR);
		if (cvPKs.isEmpty())
		{
			cvCondition.addConditionValue(-1, ValueComparisonOperator.EQUALS);
		}
		else
		{
			if (CollectionUtils.isNotEmpty(cvPKs))
			{
				cvPKs.forEach(pk -> cvCondition.addConditionValue(pk.getLong(), ValueComparisonOperator.EQUALS));
			}
		}
		searchConditionData.addFilterQueryCondition(cvCondition);
	}

	@Required
	public void setSearchQueryConditionsConverter(final DefaultSearchQueryConditionsConverter searchQueryConditionsConverter)
	{
		this.searchQueryConditionsConverter = searchQueryConditionsConverter;
	}

	@Required
	public void setFacetSearchConfigService(final BackofficeFacetSearchConfigService facetSearchConfigService)
	{
		this.facetSearchConfigService = facetSearchConfigService;
	}

	@Required
	public void setSearchConditionDataConverter(final SearchConditionDataConverter searchConditionDataConverter)
	{
		this.searchConditionDataConverter = searchConditionDataConverter;
	}

	public List<SearchConditionDecorator> getConditionsDecorators()
	{
		return conditionsDecorators;
	}

	public void setConditionsDecorators(final List<SearchConditionDecorator> conditionsDecorators)
	{
		conditionsDecorators.sort(OrderComparator.INSTANCE);
		this.conditionsDecorators = conditionsDecorators;
	}

	public TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	public CatalogVersionService getCatalogVersionService()
	{
		return catalogVersionService;
	}

	@Required
	public void setCatalogVersionService(final CatalogVersionService catalogVersionService)
	{
		this.catalogVersionService = catalogVersionService;
	}

	public UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	public CatalogTypeService getCatalogTypeService()
	{
		return catalogTypeService;
	}

	@Required
	public void setCatalogTypeService(final CatalogTypeService catalogTypeService)
	{
		this.catalogTypeService = catalogTypeService;
	}

	public Map<String, String> getIndexedTypeToCatalogVersionPropertyMapping()
	{
		return indexedTypeToCatalogVersionPropertyMapping;
	}

	@Required
	public void setIndexedTypeToCatalogVersionPropertyMapping(final Map<String, String> indexedTypeToCatalogVersionPropertyMapping)
	{
		this.indexedTypeToCatalogVersionPropertyMapping = indexedTypeToCatalogVersionPropertyMapping;
	}
}
