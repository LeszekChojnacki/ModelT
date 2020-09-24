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

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfigService;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.exceptions.FacetConfigServiceException;
import de.hybris.platform.solrfacetsearch.model.config.SolrFacetSearchConfigModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedTypeModel;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.hybris.backoffice.solrsearch.cache.BackofficeFacetSearchConfigCache;
import com.hybris.backoffice.solrsearch.daos.SolrFacetSearchConfigDAO;
import com.hybris.backoffice.solrsearch.model.BackofficeIndexedTypeToSolrFacetSearchConfigModel;
import com.hybris.backoffice.solrsearch.services.BackofficeFacetSearchConfigService;


/**
 * Default implementation for {@link BackofficeFacetSearchConfigService}
 */
public class DefaultBackofficeFacetSearchConfigService implements BackofficeFacetSearchConfigService
{

	private SolrFacetSearchConfigDAO solrFacetSearchConfigDAO;
	private FacetSearchConfigService facetSearchConfigService;
	private TypeService typeService;
	private BackofficeFacetSearchConfigCache configCache;

	private static final Logger LOG = LoggerFactory.getLogger(DefaultBackofficeFacetSearchConfigService.class);


	@Override
	public FacetSearchConfig getFacetSearchConfig(final String typeCode) throws FacetConfigServiceException
	{
		final SolrFacetSearchConfigModel searchConfigModel = findSolrFacetSearchConfigModelInternal(typeCode);

		if (searchConfigModel != null)
		{
			return facetSearchConfigService.getConfiguration(searchConfigModel.getName());
		}
		return null;
	}

	@Override
	public Collection<FacetSearchConfig> getAllMappedFacetSearchConfigs()
	{
		final Collection<BackofficeIndexedTypeToSolrFacetSearchConfigModel> searchConfigMappings = solrFacetSearchConfigDAO
				.findAllSearchConfigs();
		if (searchConfigMappings != null)
		{
			return searchConfigMappings.stream().map(this::getFacetSearchConfigFromMapping).filter(Objects::nonNull)
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	protected FacetSearchConfig getFacetSearchConfigFromMapping(final BackofficeIndexedTypeToSolrFacetSearchConfigModel mapping)
	{
		try
		{
			return facetSearchConfigService.getConfiguration(mapping.getSolrFacetSearchConfig().getName());
		}
		catch (final FacetConfigServiceException e)
		{
			LOG.warn("No facet search configuration found for name {} mapped from type {}",
					mapping.getSolrFacetSearchConfig().getName(), mapping.getIndexedType().getCode(), e);
			return null;
		}
	}

	@Override
	public SolrFacetSearchConfigModel getSolrFacetSearchConfigModel(final String typeCode) throws FacetConfigServiceException
	{
		return findSolrFacetSearchConfigModelInternal(typeCode);
	}

	@Override
	public SolrIndexedTypeModel getSolrIndexedType(final String typeCode)
	{
		final BackofficeIndexedTypeToSolrFacetSearchConfigModel matchingConfig = findSearchConfigForTypeCode(typeCode);
		return findIndexedTypeInConfig(matchingConfig);
	}

	@Override
	public IndexedType getIndexedType(final FacetSearchConfig config, final String typeCode)
	{
		final ComposedTypeModel composedType = typeService.getComposedTypeForCode(typeCode);
		final Map<String, IndexedType> indexedTypeMap = config.getIndexConfig().getIndexedTypes().values().stream()
				.collect(Collectors.toMap(IndexedType::getCode, it -> it));
		return findMatchingIndexedTypeRecursively(composedType, indexedTypeMap);
	}

	@Override
	public boolean isSolrSearchConfiguredForType(final String typeCode)
	{
		final BackofficeIndexedTypeToSolrFacetSearchConfigModel searchConfig = findSearchConfigForTypeCode(typeCode);
		return findIndexedTypeInConfig(searchConfig) != null;
	}

	@Override
	public boolean isBackofficeSolrSearchConfiguredForName(final String facetSearchConfigName)
	{
		final Optional<BackofficeIndexedTypeToSolrFacetSearchConfigModel> searchConfig = solrFacetSearchConfigDAO
				.findSearchConfigurationsForName(facetSearchConfigName).stream().findFirst();
		return searchConfig.isPresent();
	}

	protected IndexedType findMatchingIndexedTypeRecursively(final ComposedTypeModel composedType,
			final Map<String, IndexedType> typesMap)
	{
		if (ItemModel._TYPECODE.equals(composedType.getCode()))
		{
			return null;
		}
		else if (typesMap.containsKey(composedType.getCode()))
		{
			return typesMap.get(composedType.getCode());
		}
		else
		{
			return findMatchingIndexedTypeRecursively(composedType.getSuperType(), typesMap);
		}
	}

	protected SolrIndexedTypeModel findIndexedTypeInConfig(final BackofficeIndexedTypeToSolrFacetSearchConfigModel searchConfig)
	{
		Optional<SolrIndexedTypeModel> indexedTypeModel = Optional.empty();
		if (searchConfig != null && CollectionUtils.isNotEmpty(searchConfig.getSolrFacetSearchConfig().getSolrIndexedTypes()))
		{
			final ComposedTypeModel indexedType = searchConfig.getIndexedType();
			indexedTypeModel = searchConfig.getSolrFacetSearchConfig().getSolrIndexedTypes().stream()
					.filter(idxType -> Objects.equals(idxType.getType(), indexedType)).findFirst();
		}
		return indexedTypeModel.orElse(null);
	}

	protected BackofficeIndexedTypeToSolrFacetSearchConfigModel findSearchConfigForTypeCode(final String typeCode)
	{
		final BackofficeIndexedTypeToSolrFacetSearchConfigModel searchConfig;
		if (configCache.containsSearchConfigForTypeCode(typeCode))
		{
			searchConfig = configCache.getSearchConfigForTypeCode(typeCode);
		}
		else
		{
			final ComposedTypeModel composedType = typeService.getComposedTypeForCode(typeCode);
			final List<ComposedTypeModel> typeCodes = getWithSuperTypeCodes(composedType);
			final Collection<BackofficeIndexedTypeToSolrFacetSearchConfigModel> searchConfigs = solrFacetSearchConfigDAO
					.findSearchConfigurationsForTypes(typeCodes);
			searchConfig = findMatchingConfig(composedType, searchConfigs);
			configCache.putSearchConfigForTypeCode(typeCode, searchConfig);
		}
		return searchConfig;
	}

	protected BackofficeIndexedTypeToSolrFacetSearchConfigModel findMatchingConfig(final ComposedTypeModel composedType,
			final Collection<BackofficeIndexedTypeToSolrFacetSearchConfigModel> configs)
	{
		if (configs.size() == 1)
		{
			return Iterators.getOnlyElement(configs.iterator());
		}
		else if (configs.size() > 1)
		{
			final Map<String, BackofficeIndexedTypeToSolrFacetSearchConfigModel> configMap = configs.stream()
					.collect(Collectors.toMap(c -> c.getIndexedType().getCode(), c -> c));
			findMatchingConfigRecursively(composedType, configMap);
		}
		return null;
	}

	protected BackofficeIndexedTypeToSolrFacetSearchConfigModel findMatchingConfigRecursively(final ComposedTypeModel composedType,
			final Map<String, BackofficeIndexedTypeToSolrFacetSearchConfigModel> configMap)
	{
		if (ItemModel._TYPECODE.equals(composedType.getCode()))
		{
			return null;
		}
		if (configMap.containsKey(composedType.getCode()))
		{
			return configMap.get(composedType.getCode());
		}
		else
		{
			return findMatchingConfigRecursively(composedType.getSuperType(), configMap);
		}
	}

	protected List<ComposedTypeModel> getWithSuperTypeCodes(final ComposedTypeModel composedType)
	{
		final List<ComposedTypeModel> typeCodes = Lists.newArrayList();
		typeCodes.add(composedType);
		typeCodes.addAll(composedType.getAllSuperTypes());
		return typeCodes;
	}

	protected SolrFacetSearchConfigModel findSolrFacetSearchConfigModelInternal(final String typeCode)
	{
		final BackofficeIndexedTypeToSolrFacetSearchConfigModel searchConfig = findSearchConfigForTypeCode(typeCode);
		if (searchConfig != null)
		{
			return searchConfig.getSolrFacetSearchConfig();
		}
		return null;
	}

	@Required
	public void setSolrFacetSearchConfigDAO(final SolrFacetSearchConfigDAO solrFacetSearchConfigDAO)
	{
		this.solrFacetSearchConfigDAO = solrFacetSearchConfigDAO;
	}

	@Required
	public void setFacetSearchConfigService(final FacetSearchConfigService facetSearchConfigService)
	{
		this.facetSearchConfigService = facetSearchConfigService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	@Required
	public void setConfigCache(final BackofficeFacetSearchConfigCache configCache)
	{
		this.configCache = configCache;
	}

}
