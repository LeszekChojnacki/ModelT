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
package com.hybris.backoffice.solrsearch.converters.impl;


import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.search.Breadcrumb;
import de.hybris.platform.solrfacetsearch.search.Facet;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.hybris.backoffice.solrsearch.converters.FullTextSearchDataConverter;
import com.hybris.cockpitng.search.data.facet.FacetData;
import com.hybris.cockpitng.search.data.facet.FacetType;
import com.hybris.cockpitng.search.data.facet.FacetValueData;


public class DefaultFullTextSearchDataConverter implements FullTextSearchDataConverter
{
	@Override
	public Collection<FacetData> convertFacets(final Collection<Facet> facets, final List<Breadcrumb> breadcrumbs,
			final IndexedType indexedType)
	{
		final List<FacetData> convertedFacets = facets.stream()
				.map(f -> convertFacet(f, indexedType.getIndexedProperties().get(f.getName()))).collect(Collectors.toList());

		return mergeFacetsWithBreadcrumbs(convertedFacets, breadcrumbs, indexedType);
	}

	protected Collection<FacetData> mergeFacetsWithBreadcrumbs(final List<FacetData> facets,
			final Collection<Breadcrumb> breadcrumbs, final IndexedType indexedType)
	{
		final Map<String, FacetData> facetsMap = new LinkedHashMap<>();
		facets.forEach(facet -> facetsMap.put(facet.getName(), facet));

		for (final Breadcrumb breadcrumb : breadcrumbs)
		{
			FacetData facetData = facetsMap.get(breadcrumb.getFieldName());
			if (facetData == null)
			{
				final IndexedProperty indexedProperty = indexedType.getIndexedProperties().get(breadcrumb.getFieldName());
				if (indexedProperty != null)
				{
					final FacetValueData facetValue = new FacetValueData(breadcrumb.getValue(), breadcrumb.getDisplayValue(), 0, true);

					facetData = new FacetData(breadcrumb.getFieldName(), indexedProperty.getBackofficeDisplayName(),
							extractFacetType(indexedProperty), Lists.newArrayList(facetValue));
					facetsMap.put(facetData.getName(), facetData);
				}
			}
			else
			{
				final FacetValueData facetValue = facetData.getFacetValue(breadcrumb.getValue());
				if (facetValue == null)
				{
					facetData.addFacetValue(new FacetValueData(breadcrumb.getValue(), breadcrumb.getDisplayValue(),
							FacetValueData.BREADCRUMB_COUNT, true));
				}
				else if (!facetValue.isSelected())
				{
					facetData.addFacetValue(new FacetValueData(breadcrumb.getValue(), breadcrumb.getDisplayValue(), facetValue
							.getCount(), true));
				}
			}
		}
		return facetsMap.values();
	}

	protected FacetData convertFacet(final de.hybris.platform.solrfacetsearch.search.Facet facet,
			final IndexedProperty indexedProperty)
	{
		final List<FacetValueData> values = facet.getFacetValues().stream().map(this::convertFacetValue)
				.collect(Collectors.toList());
		final FacetType facetType = extractFacetType(indexedProperty);
		return new FacetData(facet.getName(), indexedProperty.getBackofficeDisplayName(), facetType, values);
	}

	protected FacetValueData convertFacetValue(final de.hybris.platform.solrfacetsearch.search.FacetValue facetValue)
	{
		return new FacetValueData(facetValue.getName(), facetValue.getDisplayName(), facetValue.getCount(), facetValue.isSelected());
	}

	protected FacetType extractFacetType(final IndexedProperty indexedProperty)
	{
		if (indexedProperty != null)
		{
			final de.hybris.platform.solrfacetsearch.config.FacetType facetType = indexedProperty.getFacetType();
			if (facetType != null)
			{
				switch (facetType)
				{
					case MULTISELECTAND:
						return FacetType.MULTISELECTAND;
					case MULTISELECTOR:
						return FacetType.MULTISELECTOR;
					default:
						return FacetType.REFINE;
				}
			}
		}
		return FacetType.REFINE;
	}
}
