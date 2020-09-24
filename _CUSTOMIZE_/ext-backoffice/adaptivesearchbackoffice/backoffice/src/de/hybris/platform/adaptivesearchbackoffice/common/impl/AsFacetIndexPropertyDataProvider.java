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
package de.hybris.platform.adaptivesearchbackoffice.common.impl;

import static de.hybris.platform.adaptivesearch.constants.AdaptivesearchConstants.SEARCH_CONFIGURATION_ATTRIBUTE;
import static de.hybris.platform.adaptivesearchbackoffice.constants.AdaptivesearchbackofficeConstants.PARENT_OBJECT_KEY;

import de.hybris.platform.adaptivesearch.data.AsIndexPropertyData;
import de.hybris.platform.adaptivesearch.model.AbstractAsConfigurableSearchConfigurationModel;
import de.hybris.platform.adaptivesearch.model.AbstractAsFacetConfigurationModel;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProvider;
import de.hybris.platform.adaptivesearch.strategies.AsSearchProviderFactory;
import de.hybris.platform.adaptivesearchbackoffice.common.DataProvider;
import de.hybris.platform.adaptivesearchbackoffice.editors.EditorRuntimeException;
import de.hybris.platform.servicelayer.model.ModelService;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.zkoss.lang.Objects;


/**
 * Implementation of {@link DataProvider} for facet index properties.
 */
public class AsFacetIndexPropertyDataProvider implements DataProvider<AsIndexPropertyData, String>
{
	protected static final String INDEX_TYPE = "indexType";

	private AsSearchProviderFactory asSearchProviderFactory;
	private ModelService modelService;

	@Override
	public List<AsIndexPropertyData> getData(final Map<String, Object> parameters)
	{
		final String indexType = resolveIndexType(parameters);

		if (StringUtils.isBlank(indexType))
		{
			return Collections.emptyList();
		}

		final AsSearchProvider searchProvider = asSearchProviderFactory.getSearchProvider();
		final List<AsIndexPropertyData> indexProperties = searchProvider.getSupportedFacetIndexProperties(indexType);

		final AbstractAsFacetConfigurationModel facetConfiguration = resolveFacetConfiguration(parameters);
		final AbstractAsConfigurableSearchConfigurationModel searchConfiguration = resolveSearchConfiguration(facetConfiguration);
		final Set<String> usedIndexProperties = collectUsedIndexProperties(searchConfiguration);

		return indexProperties.stream().filter(indexProperty -> !usedIndexProperties.contains(indexProperty.getCode()))
				.collect(Collectors.toList());
	}

	protected Set<String> collectUsedIndexProperties(final AbstractAsConfigurableSearchConfigurationModel searchConfiguration)
	{
		final Set<String> indexProperties = new HashSet<>();

		if (CollectionUtils.isNotEmpty(searchConfiguration.getPromotedFacets()))
		{
			indexProperties.addAll(searchConfiguration.getPromotedFacets().stream()
					.map(AbstractAsFacetConfigurationModel::getIndexProperty).collect(Collectors.toList()));
		}

		if (CollectionUtils.isNotEmpty(searchConfiguration.getFacets()))
		{
			indexProperties.addAll(searchConfiguration.getFacets().stream().map(AbstractAsFacetConfigurationModel::getIndexProperty)
					.collect(Collectors.toList()));
		}

		if (CollectionUtils.isNotEmpty(searchConfiguration.getExcludedFacets()))
		{
			indexProperties.addAll(searchConfiguration.getExcludedFacets().stream()
					.map(AbstractAsFacetConfigurationModel::getIndexProperty).collect(Collectors.toList()));
		}

		return indexProperties;
	}

	@Override
	public String getValue(final AsIndexPropertyData data, final Map<String, Object> parameters)
	{
		if (data == null)
		{
			return null;
		}

		return data.getCode();
	}

	@Override
	public String getLabel(final AsIndexPropertyData data, final Map<String, Object> parameters)
	{
		if (data == null)
		{
			return StringUtils.EMPTY;
		}

		return data.getCode();
	}

	protected String resolveIndexType(final Map<String, Object> parameters)
	{
		return Objects.toString(parameters.get(INDEX_TYPE));
	}

	protected AbstractAsFacetConfigurationModel resolveFacetConfiguration(final Map<String, Object> parameters)
	{
		final Object facetConfiguration = parameters.get(PARENT_OBJECT_KEY);

		if (!(facetConfiguration instanceof AbstractAsFacetConfigurationModel))
		{
			throw new EditorRuntimeException("Facet configuration not valid");
		}

		return (AbstractAsFacetConfigurationModel) facetConfiguration;
	}

	protected AbstractAsConfigurableSearchConfigurationModel resolveSearchConfiguration(
			final AbstractAsFacetConfigurationModel facetConfiguration)
	{
		final Object searchConfiguration = modelService.getAttributeValue(facetConfiguration, SEARCH_CONFIGURATION_ATTRIBUTE);

		if (!(searchConfiguration instanceof AbstractAsConfigurableSearchConfigurationModel))
		{
			throw new EditorRuntimeException("Search configuration not valid");
		}

		return (AbstractAsConfigurableSearchConfigurationModel) searchConfiguration;
	}

	public AsSearchProviderFactory getAsSearchProviderFactory()
	{
		return asSearchProviderFactory;
	}

	@Required
	public void setAsSearchProviderFactory(final AsSearchProviderFactory asSearchProviderFactory)
	{
		this.asSearchProviderFactory = asSearchProviderFactory;
	}

	public ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}
}
