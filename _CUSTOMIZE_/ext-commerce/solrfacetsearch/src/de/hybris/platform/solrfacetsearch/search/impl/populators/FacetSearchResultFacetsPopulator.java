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
package de.hybris.platform.solrfacetsearch.search.impl.populators;

import de.hybris.platform.converters.Populator;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.FacetSortProvider;
import de.hybris.platform.solrfacetsearch.config.FacetType;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.SearchConfig;
import de.hybris.platform.solrfacetsearch.config.ValueRange;
import de.hybris.platform.solrfacetsearch.config.ValueRangeSet;
import de.hybris.platform.solrfacetsearch.constants.SolrfacetsearchConstants;
import de.hybris.platform.solrfacetsearch.provider.FacetDisplayNameProvider;
import de.hybris.platform.solrfacetsearch.provider.FacetTopValuesProvider;
import de.hybris.platform.solrfacetsearch.provider.FacetValueDisplayNameProvider;
import de.hybris.platform.solrfacetsearch.search.Facet;
import de.hybris.platform.solrfacetsearch.search.FacetField;
import de.hybris.platform.solrfacetsearch.search.FacetValue;
import de.hybris.platform.solrfacetsearch.search.FacetValueField;
import de.hybris.platform.solrfacetsearch.search.FieldNameTranslator;
import de.hybris.platform.solrfacetsearch.search.FieldNameTranslator.FieldInfo;
import de.hybris.platform.solrfacetsearch.search.FieldNameTranslator.FieldInfosMapping;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchContext;
import de.hybris.platform.solrfacetsearch.search.impl.SearchResultConverterData;
import de.hybris.platform.solrfacetsearch.search.impl.SolrSearchResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.GroupCommand;
import org.apache.solr.client.solrj.response.GroupResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Required;


/**
 * Populates facet values of {@link SolrSearchResult} from {@link SearchResultConverterData}
 */
public class FacetSearchResultFacetsPopulator implements Populator<SearchResultConverterData, SolrSearchResult>, BeanFactoryAware
{
	public static final String RESPONSE_HEADERS_PARAMS = "params";

	public static final String GROUP_PARAM = "group";
	public static final String GROUP_FACET_PARAM = "group.facet";

	protected static final int DEFAULT_PRIORITY = 0;

	private FieldNameTranslator fieldNameTranslator;
	private BeanFactory beanFactory;

	@Override
	public void populate(final SearchResultConverterData source, final SolrSearchResult target)
	{
		final QueryResponse queryResponse = source.getQueryResponse();
		if (queryResponse == null || CollectionUtils.isEmpty(queryResponse.getFacetFields()))
		{
			return;
		}

		final List<Facet> facets = new ArrayList<>();

		final FacetSearchContext facetSearchContext = source.getFacetSearchContext();
		final SearchQuery searchQuery = facetSearchContext.getSearchQuery();
		final Map<String, FacetInfo> facetInfos = buildFacetInfos(searchQuery);

		final FieldInfosMapping fieldInfosMapping = fieldNameTranslator.getFieldInfos(source.getFacetSearchContext());
		final Map<String, FieldInfo> fieldInfos = fieldInfosMapping.getInvertedFieldInfos();

		final long maxFacetValueCount = getMaxFacetValueCount(queryResponse);

		for (final org.apache.solr.client.solrj.response.FacetField sourceFacet : queryResponse.getFacetFields())
		{
			final FieldInfo fieldInfo = fieldInfos.get(sourceFacet.getName());
			final String field = fieldInfo != null ? fieldInfo.getFieldName() : sourceFacet.getName();
			final FacetInfo facetInfo = facetInfos.get(field);

			if (facetInfo != null)
			{
				facets.add(buildFacet(searchQuery, facetInfo, maxFacetValueCount, sourceFacet));
			}
		}

		Collections.sort(facets, this::compareFacets);

		facets.forEach(target::addFacet);
	}

	protected Map<String, FacetInfo> buildFacetInfos(final SearchQuery searchQuery)
	{
		final Map<String, FacetInfo> facetInfos = new HashMap<>();
		final IndexedType indexedType = searchQuery.getIndexedType();

		final boolean isLegacyMode = searchQuery.getFacetSearchConfig().getSearchConfig().isLegacyMode();
		if (isLegacyMode)
		{
			for (final String facet : indexedType.getTypeFacets())
			{
				final IndexedProperty indexedProperty = indexedType.getIndexedProperties().get(facet);

				// we create a fake FacetFied to avoid 2 different code paths
				final FacetField facetField = new FacetField(facet);
				if (indexedProperty != null)
				{
					facetField.setPriority(indexedProperty.getPriority());
					facetField.setFacetType(indexedProperty.getFacetType());
					facetField.setDisplayNameProvider(indexedProperty.getFacetDisplayNameProvider());
					facetField.setSortProvider(indexedProperty.getFacetSortProvider());
					facetField.setTopValuesProvider(indexedProperty.getTopValuesProvider());
				}

				final FacetInfo facetInfo = new FacetInfo(facetField, indexedProperty);
				facetInfos.put(facet, facetInfo);
			}
		}
		else
		{
			for (final FacetField facet : searchQuery.getFacets())
			{
				final IndexedProperty indexedProperty = indexedType.getIndexedProperties().get(facet.getField());
				final FacetInfo facetInfo = new FacetInfo(facet, indexedProperty);
				facetInfos.put(facet.getField(), facetInfo);
			}
		}

		for (final FacetValueField facetValue : searchQuery.getFacetValues())
		{
			final FacetInfo facetInfo = facetInfos.get(facetValue.getField());

			// in some cases filter queries are still sent as facet values
			// we ignore values for facets that are not in the search query
			if (facetInfo != null && CollectionUtils.isNotEmpty(facetValue.getValues()))
			{
				facetInfo.getSelectedValues().addAll(facetValue.getValues());
			}
		}

		return facetInfos;
	}

	protected Facet buildFacet(final SearchQuery searchQuery, final FacetInfo facetInfo, final long maxFacetValueCount,
			final org.apache.solr.client.solrj.response.FacetField sourceFacet)
	{
		final FacetField facetField = facetInfo.getFacetField();
		final IndexedProperty indexedProperty = facetInfo.getIndexedProperty();
		final String facetDisplayName = indexedProperty != null ? indexedProperty.getDisplayName() : null;
		final int facetPriority = facetField.getPriority() != null ? facetField.getPriority().intValue() : DEFAULT_PRIORITY;
		final FacetType facetType = facetField.getFacetType();

		final int size = sourceFacet.getValueCount();
		final List<FacetValue> topFacetValues = new ArrayList<>();
		final List<FacetValue> facetValues = new ArrayList<>(size);
		final List<FacetValue> selectedFacetValues = new ArrayList<>();
		final List<FacetValue> allFacetValues = new ArrayList<>(size);

		if (CollectionUtils.isNotEmpty(sourceFacet.getValues()))
		{
			final boolean showFacet = facetInfo.isMultiselect() || isAllFacetValuesInResponse(searchQuery);
			final Object facetValueDisplayNameProvider = resolveFacetValuesDisplayNameProvider(facetInfo);

			final Map<String, FacetValue> valuesMapping = new LinkedHashMap<>();

			for (final Count sourceFacetValue : sourceFacet.getValues())
			{
				// if the facet is multi-select then we always display all the facet values, if not, we don't show the facet value when it has 100% coverage of the results
				final boolean showFacetValue = showFacet || sourceFacetValue.getCount() < maxFacetValueCount;
				final boolean selected = isFacetValueSelected(facetInfo, sourceFacetValue);

				final String displayName = resolveFacetValueDisplayName(searchQuery, facetInfo, facetValueDisplayNameProvider,
						sourceFacetValue.getName());
				final FacetValue facetValue = new FacetValue(sourceFacetValue.getName(), displayName, sourceFacetValue.getCount(),
						selected);

				if (showFacetValue)
				{
					valuesMapping.put(facetValue.getName(), facetValue);
				}

				if (selected)
				{
					selectedFacetValues.add(facetValue);
				}

				allFacetValues.add(facetValue);
			}

			final List<FacetValue> promotedValues = new ArrayList<>();

			removeFacetValues(facetInfo.getFacetField().getPromotedValues(), valuesMapping, value -> {
				value.addTag(SolrfacetsearchConstants.PROMOTED_TAG);
				promotedValues.add(value);
			});

			removeFacetValues(facetInfo.getFacetField().getExcludedValues(), valuesMapping,
					value -> value.addTag(SolrfacetsearchConstants.EXCLUDED_TAG));

			// promoted values should not be affected by sorting, top values provider should not reorder the values
			facetValues.addAll(valuesMapping.values());
			sortFacetValues(facetInfo, searchQuery, facetValues);
			facetValues.addAll(0, promotedValues);
			buildTopFacetValues(facetInfo, facetValues, topFacetValues);

		}

		final Facet facet = new Facet(facetField.getField(), facetDisplayName, facetValues, topFacetValues, selectedFacetValues,
				facetType, facetPriority);
		facet.setAllFacetValues(allFacetValues);
		facet.setMultiselect(facetInfo.isMultiselect());

		return facet;
	}

	protected long getMaxFacetValueCount(final QueryResponse queryResponse)
	{
		final GroupResponse groupResponse = queryResponse.getGroupResponse();
		if (groupResponse != null)
		{
			final GroupCommand groupCommand = groupResponse.getValues().get(0);
			final SimpleOrderedMap<String> params = (SimpleOrderedMap) queryResponse.getResponseHeader()
					.get(RESPONSE_HEADERS_PARAMS);
			final boolean groupFacets = Boolean.parseBoolean(params.get(GROUP_FACET_PARAM));

			return groupFacets ? groupCommand.getNGroups() : groupCommand.getMatches();
		}
		else
		{
			return queryResponse.getResults().getNumFound();
		}
	}

	protected boolean isAllFacetValuesInResponse(final SearchQuery searchQuery)
	{
		final FacetSearchConfig facetSearchConfig = searchQuery.getFacetSearchConfig();
		final SearchConfig searchConfig = facetSearchConfig.getSearchConfig();
		return searchConfig.isAllFacetValuesInResponse();
	}

	protected boolean isFacetValueSelected(final FacetInfo facetInfo, final Count sourceFacetValue)
	{
		return facetInfo.getSelectedValues().contains(sourceFacetValue.getName());
	}

	protected String resolveFacetValueDisplayName(final SearchQuery searchQuery, final FacetInfo facetInfo,
			final Object facetDisplayNameProvider, final String facetValue)
	{
		if (facetDisplayNameProvider != null)
		{
			if (facetDisplayNameProvider instanceof FacetValueDisplayNameProvider)
			{
				return ((FacetValueDisplayNameProvider) facetDisplayNameProvider).getDisplayName(searchQuery,
						facetInfo.getIndexedProperty(), facetValue);
			}
			else if (facetDisplayNameProvider instanceof FacetDisplayNameProvider)
			{
				return ((FacetDisplayNameProvider) facetDisplayNameProvider).getDisplayName(searchQuery, facetValue);
			}
		}

		return facetValue;
	}

	protected void removeFacetValues(final List<String> keys, final Map<String, FacetValue> mapping,
			final Consumer<FacetValue> consumer)
	{
		if (CollectionUtils.isNotEmpty(keys))
		{
			for (final String key : keys)
			{
				final FacetValue value = mapping.remove(key);
				if (value != null)
				{
					consumer.accept(value);
				}
			}
		}
	}

	protected void sortFacetValues(final FacetInfo facetInfo, final SearchQuery searchQuery, final List<FacetValue> facetValues)
	{
		final FacetSortProvider sortProvider = resolveFacetValuesSortProvider(facetInfo);

		if (sortProvider != null)
		{
			final Comparator<FacetValue> comparator = sortProvider.getComparatorForTypeAndProperty(searchQuery.getIndexedType(),
					facetInfo.getIndexedProperty());
			Collections.sort(facetValues, comparator);
		}
		else if (facetInfo.isRanged())
		{
			final IndexedProperty indexedProperty = facetInfo.getIndexedProperty();
			final List<ValueRange> valueRanges = resolveFacetValueRanges(indexedProperty,
					indexedProperty.isCurrency() ? searchQuery.getCurrency() : null);

			final Map<String, FacetValue> facetValuesMap = facetValues.stream()
					.collect(Collectors.toMap(FacetValue::getName, Function.identity()));

			facetValues.clear();

			for (final ValueRange valueRange : valueRanges)
			{
				final FacetValue facetValue = facetValuesMap.get(valueRange.getName());
				if (facetValue != null)
				{
					facetValues.add(facetValue);
				}
			}
		}
	}

	protected List<ValueRange> resolveFacetValueRanges(final IndexedProperty property, final String qualifier)
	{
		ValueRangeSet valueRangeSet;
		if (qualifier == null)
		{
			valueRangeSet = property.getValueRangeSets().get("default");
		}
		else
		{
			valueRangeSet = property.getValueRangeSets().get(qualifier);
			if (valueRangeSet == null)
			{
				valueRangeSet = property.getValueRangeSets().get("default");
			}
		}

		if (valueRangeSet != null)
		{
			return valueRangeSet.getValueRanges();
		}
		else
		{
			return Collections.emptyList();
		}
	}

	protected void buildTopFacetValues(final FacetInfo facetInfo, final List<FacetValue> facetValues,
			final List<FacetValue> topFacetValues)
	{
		final FacetTopValuesProvider topValuesProvider = resolveFacetTopValuesProvider(facetInfo);
		if (topValuesProvider != null && !facetInfo.isRanged())
		{
			topFacetValues.addAll(topValuesProvider.getTopValues(facetInfo.getIndexedProperty(), facetValues));
		}
	}

	protected int compareFacets(final Facet facet1, final Facet facet2)
	{
		int result = Integer.compare(facet2.getPriority(), facet1.getPriority());
		if (result == 0)
		{
			result = facet2.getDisplayName().compareToIgnoreCase(facet1.getDisplayName());
			if (result == 0)
			{
				result = facet2.getName().compareToIgnoreCase(facet1.getName());
			}
		}

		return result;
	}

	protected Object resolveFacetValuesDisplayNameProvider(final FacetInfo facetInfo)
	{
		final String beanName = facetInfo.getFacetField().getDisplayNameProvider();
		return beanName != null ? beanFactory.getBean(beanName) : null;
	}

	protected FacetSortProvider resolveFacetValuesSortProvider(final FacetInfo facetInfo)
	{
		final String beanName = facetInfo.getFacetField().getSortProvider();
		final Object bean = beanName != null ? beanFactory.getBean(beanName) : null;

		if (bean instanceof FacetSortProvider)
		{
			return (FacetSortProvider) bean;
		}
		else
		{
			//	we have to keep this because we had/have some wrong sample data
			return null;
		}
	}

	protected FacetTopValuesProvider resolveFacetTopValuesProvider(final FacetInfo facetInfo)
	{
		final String beanName = facetInfo.getFacetField().getTopValuesProvider();
		return beanName != null ? beanFactory.getBean(beanName, FacetTopValuesProvider.class) : null;
	}

	public FieldNameTranslator getFieldNameTranslator()
	{
		return fieldNameTranslator;
	}

	@Required
	public void setFieldNameTranslator(final FieldNameTranslator fieldNameTranslator)
	{
		this.fieldNameTranslator = fieldNameTranslator;
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

	protected static class FacetInfo
	{
		private final FacetField facetField;
		private final IndexedProperty indexedProperty;
		private final Set<String> selectedValues;

		public FacetInfo(final FacetField facetField, final IndexedProperty indexedProperty)
		{
			this.facetField = facetField;
			this.indexedProperty = indexedProperty;
			selectedValues = new LinkedHashSet<>();
		}

		public FacetField getFacetField()
		{
			return facetField;
		}

		public IndexedProperty getIndexedProperty()
		{
			return indexedProperty;
		}

		public Set<String> getSelectedValues()
		{
			return selectedValues;
		}

		public boolean isMultiselect()
		{
			return facetField != null
					&& (facetField.getFacetType() == FacetType.MULTISELECTAND || facetField.getFacetType() == FacetType.MULTISELECTOR);
		}

		public boolean isRanged()
		{
			return indexedProperty != null && MapUtils.isNotEmpty(indexedProperty.getValueRangeSets());
		}
	}
}
