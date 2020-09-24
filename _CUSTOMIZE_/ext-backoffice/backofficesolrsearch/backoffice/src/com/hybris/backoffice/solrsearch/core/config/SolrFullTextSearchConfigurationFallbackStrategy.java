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
package com.hybris.backoffice.solrsearch.core.config;

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.exceptions.FacetConfigServiceException;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.solrsearch.dataaccess.SolrSearchStrategy;
import com.hybris.backoffice.solrsearch.services.BackofficeFacetSearchConfigService;
import com.hybris.backoffice.widgets.fulltextsearch.FullTextSearchController;
import com.hybris.cockpitng.config.fulltextsearch.DefaultFullTextSearchConfigurationFallbackStrategy;
import com.hybris.cockpitng.config.fulltextsearch.jaxb.FieldListType;
import com.hybris.cockpitng.config.fulltextsearch.jaxb.FieldType;
import com.hybris.cockpitng.config.fulltextsearch.jaxb.FulltextSearch;
import com.hybris.cockpitng.core.config.ConfigContext;


public class SolrFullTextSearchConfigurationFallbackStrategy extends DefaultFullTextSearchConfigurationFallbackStrategy
{

	private static final Logger LOG = LoggerFactory.getLogger(SolrFullTextSearchConfigurationFallbackStrategy.class);

	private BackofficeFacetSearchConfigService facetSearchConfigService;

	@Override
	public FulltextSearch loadFallbackConfiguration(final ConfigContext context, final Class<FulltextSearch> configurationType)
	{
		final AtomicReference<FulltextSearch> result = new AtomicReference<>();
		final String contextAttribute = context.getAttribute(FullTextSearchController.CONFIG_CONTEXT_STRATEGY);
		if (StringUtils.isBlank(contextAttribute)
				|| StringUtils.equals(SolrSearchStrategy.PREFERRED_STRATEGY_NAME, contextAttribute))
		{
			try
			{
				final String typeCode = getTypeFromContext(context);
				final FacetSearchConfig searchConfig = getFacetSearchConfigService().getFacetSearchConfig(typeCode);
				if (searchConfig != null)
				{
					searchConfig.getIndexConfig().getIndexedTypes().values().stream()
							.filter(type -> StringUtils.equals(typeCode, type.getCode())).findFirst()
							.ifPresent(indexedType -> result.set(resolveFulltextSearch(indexedType)));
				}
			}
			catch (final FacetConfigServiceException e)
			{
				LOG.error(e.getLocalizedMessage(), e);
			}
		}
		if (result.get() == null)
		{
			result.set(super.loadFallbackConfiguration(context, configurationType));
		}
		LOG.debug("Solr fallback for {} has been called", FulltextSearch.class);

		return result.get();
	}

	protected FulltextSearch resolveFulltextSearch(final IndexedType indexedType)
	{
		if (indexedType != null && MapUtils.isNotEmpty(indexedType.getIndexedProperties()))
		{
			final FulltextSearch result = new FulltextSearch();
			result.setFieldList(new FieldListType());
			indexedType.getIndexedProperties().values().stream().map(attr -> {
				final FieldType fieldType = new FieldType();
				fieldType.setName(attr.getName());
				fieldType.setDisplayName(attr.getDisplayName());
				return fieldType;
			}).forEach(result.getFieldList().getField()::add);
			return result;
		}
		return null;
	}

	protected BackofficeFacetSearchConfigService getFacetSearchConfigService()
	{
		return facetSearchConfigService;
	}

	@Required
	public void setFacetSearchConfigService(final BackofficeFacetSearchConfigService facetSearchConfigService)
	{
		this.facetSearchConfigService = facetSearchConfigService;
	}
}
