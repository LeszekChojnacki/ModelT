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
package com.hybris.backoffice.solrsearch.dataaccess;

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.exceptions.FacetConfigServiceException;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.solrsearch.dataaccess.facades.DefaultSolrFieldSearchFacadeStrategy;
import com.hybris.backoffice.solrsearch.services.BackofficeFacetSearchConfigService;
import com.hybris.backoffice.widgets.fulltextsearch.FullTextSearchStrategy;
import com.hybris.cockpitng.search.data.ValueComparisonOperator;


public class SolrSearchStrategy implements FullTextSearchStrategy
{
	/**
	 * Expected value received through <code>strategy</code> socket of FullTextSearch widget that point to Solr strategy.
	 */
	public static final String PREFERRED_STRATEGY_NAME = "solr";

	private static final String DEFAULT_TYPE = "default";

	private static final Logger LOGGER = LoggerFactory.getLogger(SolrSearchStrategy.class);

	private BackofficeFacetSearchConfigService backofficeFacetSearchConfigService;

	private Map<String, String> typeMappings;

	private Map<String, Set<String>> operatorConfig;

	protected IndexedProperty getIndexedProperty(final String typeCode, final String name)
	{
		try
		{
			final FacetSearchConfig facetSearchConfig = getBackofficeFacetSearchConfigService().getFacetSearchConfig(typeCode);
			final IndexedType indexedType = facetSearchConfig.getIndexConfig().getIndexedTypes().values().stream()
					.filter(index -> StringUtils.equals(index.getComposedType().getCode(), typeCode)).findFirst()
					.orElse(null);
			if (indexedType != null)
			{
				return indexedType.getIndexedProperties().get(name);
			}
		}
		catch (final FacetConfigServiceException e)
		{
			LOGGER.error(e.getLocalizedMessage(), e);
		}
		return null;
	}

	@Override
	public String getFieldType(final String typeCode, final String fieldName)
	{
		final IndexedProperty indexedProperty = getIndexedProperty(typeCode, fieldName);
		if (indexedProperty != null)
		{
			return getTypeMappings().get(indexedProperty.getType());
		}
		return null;
	}

	@Override
	public boolean isLocalized(final String typeCode, final String fieldName)
	{
		final IndexedProperty indexedProperty = getIndexedProperty(typeCode, fieldName);
		return indexedProperty != null && indexedProperty.isLocalized();
	}

	protected BackofficeFacetSearchConfigService getBackofficeFacetSearchConfigService()
	{
		return backofficeFacetSearchConfigService;
	}

	@Required
	public void setBackofficeFacetSearchConfigService(final BackofficeFacetSearchConfigService backofficeFacetSearchConfigService)
	{
		this.backofficeFacetSearchConfigService = backofficeFacetSearchConfigService;
	}

	@Override
	public Collection<ValueComparisonOperator> getAvailableOperators(final String typeCode, final String fieldName)
	{
		return getAvailableOperators(getFieldType(typeCode, fieldName));
	}

	protected Collection<ValueComparisonOperator> getAvailableOperators(final String fieldType)
	{
		final Set<String> operators = getOperatorConfig().getOrDefault(fieldType, getOperatorConfig().get(DEFAULT_TYPE));
		return operators.stream().map(ValueComparisonOperator::valueOf).collect(Collectors.toList());
	}

	protected Map<String, String> getTypeMappings()
	{
		return typeMappings;
	}

	@Required
	public void setTypeMappings(final Map<String, String> typeMappings)
	{
		this.typeMappings = new LinkedHashMap<>(typeMappings);
	}

	protected Map<String, Set<String>> getOperatorConfig()
	{
		return operatorConfig;
	}

	@Required
	public void setOperatorConfig(final Map<String, Set<String>> operatorConfig)
	{
		this.operatorConfig = operatorConfig;
	}

	@Override
	public String getStrategyName()
	{
		return DefaultSolrFieldSearchFacadeStrategy.STRATEGY_NAME;
	}
}
