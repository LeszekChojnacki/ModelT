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
package de.hybris.platform.solrfacetsearch.solr.impl;

import de.hybris.platform.solrfacetsearch.search.SearchQuery.QueryOperator;
import de.hybris.platform.solrfacetsearch.solr.IndexedPropertyTypeInfo;
import de.hybris.platform.solrfacetsearch.solr.SolrIndexedPropertyTypeRegistry;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * Default implementation of {@link SolrIndexedPropertyTypeRegistry}
 */
public class DefaultSolrIndexedPropertyTypeRegistry
		implements SolrIndexedPropertyTypeRegistry, ApplicationContextAware, InitializingBean
{
	private static final Logger LOG = Logger.getLogger(DefaultSolrIndexedPropertyTypeRegistry.class);

	private ApplicationContext applicationContext = null;
	private final Map<String, Set<QueryOperator>> operatorsMap = new HashMap<>();
	private Map<String, String> indexPropertyTypeMapping;
	private List<String> unallowedFacetTypes;

	@Override
	public void afterPropertiesSet() throws Exception
	{
		final Map<String, IndexPropertyTypeOperatorsMapping> mappingBeans = applicationContext
				.getBeansOfType(IndexPropertyTypeOperatorsMapping.class);

		for (final Map.Entry<String, IndexPropertyTypeOperatorsMapping> entry : mappingBeans.entrySet())
		{
			for (final String type : entry.getValue().getPropertyTypes())
			{
				operatorsMap.put(type, entry.getValue().getOperators());
			}
		}
	}

	@Override
	public IndexedPropertyTypeInfo getIndexPropertyTypeInfo(final String propertyType)
	{
		final IndexedPropertyTypeInfo indexedPropertyType = new IndexedPropertyTypeInfo();

		final Set<QueryOperator> supportedQueryOperators = operatorsMap.get(propertyType);
		indexedPropertyType.setSupportedQueryOperators(
				supportedQueryOperators != null ? supportedQueryOperators : Collections.<QueryOperator> emptySet());
		indexedPropertyType.setAllowFacet(!unallowedFacetTypes.contains(propertyType));

		final String clazz = indexPropertyTypeMapping.get(propertyType);

		try
		{
			indexedPropertyType.setJavaType(Class.forName(clazz));
		}
		catch (final ClassNotFoundException e)
		{
			LOG.error(e);
			indexedPropertyType.setJavaType(null);
		}

		return indexedPropertyType;
	}

	public static class IndexPropertyTypeOperatorsMapping
	{

		private Set<String> propertyTypes;
		private Set<QueryOperator> operators;


		public Set<String> getPropertyTypes()
		{
			return propertyTypes;
		}

		public void setPropertyTypes(final Set<String> propertyTypes)
		{
			this.propertyTypes = propertyTypes;
		}


		public Set<QueryOperator> getOperators()
		{
			return operators;
		}

		public void setOperators(final Set<QueryOperator> operators)
		{
			this.operators = operators;
		}

		public void setPropertyType(final String attributeType)
		{
			this.propertyTypes = Collections.singleton(attributeType);
		}
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext)
	{
		this.applicationContext = applicationContext;
	}

	public Map<String, String> getIndexPropertyTypeMapping()
	{
		return indexPropertyTypeMapping;
	}

	@Required
	public void setIndexPropertyTypeMapping(final Map<String, String> indexPropertyTypeMapping)
	{
		this.indexPropertyTypeMapping = indexPropertyTypeMapping;
	}

	public List<String> getUnallowedFacetTypes()
	{
		return unallowedFacetTypes;
	}

	@Required
	public void setUnallowedFacetTypes(final List<String> unallowedFacetTypes)
	{
		this.unallowedFacetTypes = unallowedFacetTypes;
	}
}
