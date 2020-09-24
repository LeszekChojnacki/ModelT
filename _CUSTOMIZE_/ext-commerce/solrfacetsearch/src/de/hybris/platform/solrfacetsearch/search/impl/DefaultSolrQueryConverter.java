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
package de.hybris.platform.solrfacetsearch.search.impl;

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.solrfacetsearch.config.FacetType;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider.FieldType;
import de.hybris.platform.solrfacetsearch.provider.IndexedTypeFieldsValuesProvider;
import de.hybris.platform.solrfacetsearch.search.CoupledQueryField;
import de.hybris.platform.solrfacetsearch.search.FacetSearchException;
import de.hybris.platform.solrfacetsearch.search.FacetValueField;
import de.hybris.platform.solrfacetsearch.search.FieldNameTranslator;
import de.hybris.platform.solrfacetsearch.search.QueryField;
import de.hybris.platform.solrfacetsearch.search.RawQuery;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.SearchQuery.Operator;
import de.hybris.platform.solrfacetsearch.search.SolrQueryConverter;
import de.hybris.platform.solrfacetsearch.search.SolrQueryPostProcessor;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchContext;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchContextFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.params.FacetParams;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default Implementation of {@link SolrQueryConverter}
 */
public class DefaultSolrQueryConverter implements SolrQueryConverter, BeanFactoryAware
{
	private static final Logger LOG = Logger.getLogger(DefaultSolrQueryConverter.class);

	protected static final String ALL_QUERY = "*:*";

	protected static final SearchQuery.Operator DEFAULT_FIELD_OPERATOR = Operator.AND;

	private FieldNameTranslator fieldNameTranslator;
	private List<SolrQueryPostProcessor> queryPostProcessors;
	private FacetSort facetSort;
	private SearchQuery.Operator fieldOperator;
	private Integer defaultLimit = Integer.valueOf(50);
	private String forbiddenChar = "_";

	private FacetSearchContextFactory<FacetSearchContext> facetSearchContextFactory;
	private Converter<SearchQueryConverterData, SolrQuery> legacyFacetSearchQueryConverter;

	private BeanFactory beanFactory;

	protected FieldNameTranslator getFieldNameTranslator()
	{
		return fieldNameTranslator;
	}

	@Required
	public void setFieldNameTranslator(final FieldNameTranslator fieldNameTranslator)
	{
		this.fieldNameTranslator = fieldNameTranslator;
	}

	public List<SolrQueryPostProcessor> getQueryPostProcessors()
	{
		return queryPostProcessors;
	}

	public void setQueryPostProcessors(final List<SolrQueryPostProcessor> queryPostProcessors)
	{
		this.queryPostProcessors = queryPostProcessors;
	}

	public FacetSort getFacetSort()
	{
		return facetSort;
	}

	@Required
	public void setFacetSort(final FacetSort facetSort)
	{
		this.facetSort = facetSort;
	}

	public SearchQuery.Operator getFieldOperator()
	{
		return fieldOperator;
	}

	public void setFieldOperator(final Operator fieldOperator)
	{
		this.fieldOperator = fieldOperator;
	}

	public Integer getDefaultLimit()
	{
		return defaultLimit;
	}

	public void setDefaultLimit(final Integer defaultLimit)
	{
		if (defaultLimit == null)
		{
			this.defaultLimit = Integer.valueOf(50);
		}
		else
		{
			this.defaultLimit = defaultLimit;
		}
	}

	public String getForbiddenChar()
	{
		return forbiddenChar;
	}

	public void setForbiddenChar(final String forbiddenChar)
	{
		if (forbiddenChar == null)
		{
			this.forbiddenChar = "_";
		}
		else
		{
			this.forbiddenChar = forbiddenChar;
		}
	}

	public FacetSearchContextFactory<FacetSearchContext> getFacetSearchContextFactory()
	{
		return facetSearchContextFactory;
	}

	@Required
	public void setFacetSearchContextFactory(final FacetSearchContextFactory<FacetSearchContext> facetSearchContextFactory)
	{
		this.facetSearchContextFactory = facetSearchContextFactory;
	}

	public Converter<SearchQueryConverterData, SolrQuery> getLegacyFacetSearchQueryConverter()
	{
		return legacyFacetSearchQueryConverter;
	}

	@Required
	public void setLegacyFacetSearchQueryConverter(
			final Converter<SearchQueryConverterData, SolrQuery> legacyFacetSearchQueryConverter)
	{
		this.legacyFacetSearchQueryConverter = legacyFacetSearchQueryConverter;
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

	@Override
	public SolrQuery convertSolrQuery(final SearchQuery searchQuery) throws FacetSearchException
	{
		checkQuery(searchQuery);

		final SolrQuery solrQuery = createSolrQuery(searchQuery);

		final List<QueryField> queries = new ArrayList<QueryField>();
		final List<QueryField> filterQueries = new ArrayList<QueryField>();

		final Map<String, IndexedFacetInfo> facetInfoMap = getFacetInfo(searchQuery);

		final List<CoupledQueryField> catalogVersionFilters = includeCatalogVersionFields(searchQuery);

		// Split the query fields into "q" and "fq" fields
		splitQueryFields(prepareQueryFields(searchQuery), queries, filterQueries, facetInfoMap);

		// Turn the query fields into a string
		final String[] convertedQueryFields = convertQueryFields(queries, null);
		final String[] convertedCoupledQueryFields = convertCoupledQueryFields(searchQuery, searchQuery.getCoupledFields());
		final String[] convertedRawQueries = convertRawQueries(searchQuery, searchQuery.getRawQueries());

		final List<String> combinedQueryFields = new ArrayList<String>();
		combinedQueryFields.addAll(Arrays.asList(convertedQueryFields));
		combinedQueryFields.addAll(Arrays.asList(convertedCoupledQueryFields));
		combinedQueryFields.addAll(Arrays.asList(convertedRawQueries));

		final String query = buildQuery(combinedQueryFields.toArray(new String[combinedQueryFields.size()]), searchQuery);

		solrQuery.setQuery(query);

		final IndexedType indexedType = searchQuery.getIndexedType();
		if (indexedType.isGroup())
		{
			final IndexedProperty groupIndexedProperty = indexedType.getIndexedProperties().get(indexedType.getGroupFieldName());
			if (groupIndexedProperty == null)
			{
				throw new FacetSearchException("Grouping is enabled but no groupFieldName is configured in the indexed type");
			}

			final String groupFieldName = getFieldNameTranslator().translate(searchQuery, groupIndexedProperty.getName(),
					FieldType.INDEX);
			solrQuery.add("group", "true");
			solrQuery.add("group.field", groupFieldName);
			solrQuery.add("group.limit", Integer.toString(indexedType.getGroupLimit()));
			solrQuery.add("group.facet", Boolean.toString(indexedType.isGroupFacets()));
			solrQuery.add("group.ngroups", "true");
		}

		if (searchQuery.isEnableSpellcheck() && StringUtils.isNotBlank(searchQuery.getUserQuery()))
		{
			solrQuery.add("spellcheck", "true");
			solrQuery.add("spellcheck.dictionary", searchQuery.getLanguage());
			solrQuery.add("spellcheck.collate", Boolean.TRUE.toString());
			solrQuery.add("spellcheck.q", searchQuery.getUserQuery());
		}

		final String[] convertedQueryFilters = convertQueryFields(filterQueries, facetInfoMap);
		final String[] convertedCatalogVersionFilters = convertCoupledQueryFields(searchQuery, catalogVersionFilters);
		final String[] combinedFilterFields = (String[]) ArrayUtils.addAll(convertedQueryFilters, convertedCatalogVersionFilters);

		// Add the filter queries
		if (ArrayUtils.isNotEmpty(combinedFilterFields))
		{
			solrQuery.addFilterQuery(combinedFilterFields);
		}

		final int start = searchQuery.getOffset() * searchQuery.getPageSize();
		solrQuery.setStart(Integer.valueOf(start));
		solrQuery.setRows(Integer.valueOf(searchQuery.getPageSize()));
		solrQuery.setFacet(true);
		this.addFacetFields(solrQuery, facetInfoMap);

		solrQuery.setFacetMinCount(1);
		solrQuery.setFacetLimit(defaultLimit.intValue());

		solrQuery.setFacetSort(facetSort.getName());

		if (searchQuery.getRawParams().size() > 0)
		{
			addSolrParams(solrQuery, searchQuery);
		}

		return applyPostProcessorsInOrder(solrQuery, searchQuery);
	}

	protected SolrQuery createSolrQuery(final SearchQuery searchQuery)
	{
		final FacetSearchContext facetSearchContext = facetSearchContextFactory.getContext();

		final SearchQueryConverterData searchQueryConverterData = new SearchQueryConverterData();
		searchQueryConverterData.setFacetSearchContext(facetSearchContext);
		searchQueryConverterData.setSearchQuery(searchQuery);

		return legacyFacetSearchQueryConverter.convert(searchQueryConverterData);
	}

	/**
	 * Includes the catalogId and catalogVersion query fields in the SOLR query, based on the
	 * {@link SearchQuery#getCatalogVersions()}.
	 *
	 * @return List of {@link CoupledQueryField}
	 */
	protected List<CoupledQueryField> includeCatalogVersionFields(final SearchQuery searchQuery)
	{
		final List<CatalogVersionModel> catalogVersions = searchQuery.getCatalogVersions();
		if (catalogVersions != null && !catalogVersions.isEmpty())
		{
			final List<CoupledQueryField> catalogVersionCoupledFields = new ArrayList<CoupledQueryField>(catalogVersions.size());
			for (final CatalogVersionModel catalogVersion : searchQuery.getCatalogVersions())
			{
				final QueryField catalogField = new QueryField("catalogId",
						"\"" + escape(catalogVersion.getCatalog().getId()) + "\"");
				final QueryField catalogVersionField = new QueryField("catalogVersion", escape(catalogVersion.getVersion()));
				final CoupledQueryField catalogCouple = new CoupledQueryField("catalogVersionCouple", catalogField,
						catalogVersionField, Operator.AND, Operator.OR);
				catalogVersionCoupledFields.add(catalogCouple);
			}
			return catalogVersionCoupledFields;
		}
		return Collections.<CoupledQueryField> emptyList();
	}

	protected String buildQuery(final String[] queries, final SearchQuery searchQuery)
	{
		// Turn the query fields into a string
		String query;
		if (queries.length == 0)
		{
			query = ALL_QUERY;
		}
		else
		{
			final Operator operator = resolveOperator(searchQuery);
			query = combine(queries, operator.getName());
		}

		if (LOG.isDebugEnabled())
		{
			LOG.debug("FIELDS : " + query);
		}

		return query;
	}

	/**
	 * Manage the OR and AND operator in a multi-select search
	 *
	 * @param source
	 * @param queries
	 * @param filterQueries
	 * @param facetInfoMap
	 */
	protected void splitQueryFields(final List<QueryField> source, final List<QueryField> queries,
			final List<QueryField> filterQueries, final Map<String, IndexedFacetInfo> facetInfoMap)
	{
		for (final QueryField queryField : source)
		{
			if (isFilterQueryField(queryField, facetInfoMap))
			{
				if (facetInfoMap.containsKey(queryField.getField()) && facetInfoMap.get(queryField.getField()).isMultiSelectOr())
				{
					// Change the operator to OR for multi-select
					queryField.setOperator(SearchQuery.Operator.OR);
				}
				filterQueries.add(queryField);
			}
			else
			{
				queries.add(queryField);
			}
		}
	}


	/**
	 * Retrieve info about the facet related to the searchQuery
	 *
	 * @param searchQuery
	 *
	 * @return Map of IndexedFacetInfo
	 */
	protected Map<String, IndexedFacetInfo> getFacetInfo(final SearchQuery searchQuery)
	{
		final Map<String, IndexedFacetInfo> results = new HashMap<String, IndexedFacetInfo>();

		int index = 0;

		final IndexedType indexedType = searchQuery.getIndexedType();
		final Set<String> facets = indexedType.getTypeFacets();
		for (final String facetName : facets)
		{
			final IndexedProperty indexedProperty = indexedType.getIndexedProperties().get(facetName);
			if (indexedProperty != null)
			{
				final IndexedFacetInfo facetInfo = new IndexedFacetInfo();
				final FacetType facetType = indexedProperty.getFacetType();
				if (FacetType.MULTISELECTAND.equals(facetType))
				{
					facetInfo.setMultiSelect(true);
					facetInfo.setMultiSelectAnd(true);
				}
				else if (FacetType.MULTISELECTOR.equals(facetType))
				{
					facetInfo.setMultiSelect(true);
					facetInfo.setMultiSelectOr(true);
				}

				facetInfo.setTranslatedFieldName(getFieldNameTranslator().translate(searchQuery, facetName, FieldType.INDEX));
				facetInfo.setKey("fk" + index);
				results.put(facetInfo.getTranslatedFieldName(), facetInfo);
			}

			index++;
		}

		return results;
	}


	protected String escape(final String text)
	{
		return ClientUtils.escapeQueryChars(text);
	}

	/**
	 * Test if the queryField part of qf
	 *
	 * @param queryField
	 * @param facetInfoMap
	 *
	 * @return true if the queryField is part of qf query
	 */
	protected boolean isFilterQueryField(final QueryField queryField, final Map<String, IndexedFacetInfo> facetInfoMap)
	{
		final String field = queryField.getField();

		// Special case for catalog and catalog version.
		return "catalogId".equals(field) || "catalogVersion".equals(field) || facetInfoMap.containsKey(field);
	}


	protected void checkQuery(final SearchQuery solrSearchQuery) throws FacetSearchException
	{
		if (solrSearchQuery.getLanguage() == null)
		{
			throw new FacetSearchException("query language must not be null");
		}
		if (solrSearchQuery.getCurrency() == null)
		{
			throw new FacetSearchException("query currency must not be null");
		}
	}

	/**
	 * Converts all query fields to the solr query string. Example: [hardware:cpu, hardware:intel, manufacture:sony(OR),
	 * manufacture:ibm(OR)] --> [(hardware:(cpu AND intel)) AND (manufacture:(sony OR ibm))]
	 *
	 * @return converted query string
	 */
	protected String[] convertQueryFields(final List<QueryField> queryFields, final Map<String, IndexedFacetInfo> facetInfoMap)
	{
		final List<String> joinedQueries = new ArrayList<String>();

		for (final QueryField qf : queryFields)
		{
			// Add a prefix to all the multi-select facet filter queries
			final IndexedFacetInfo indexedFacetInfo = facetInfoMap == null ? null : facetInfoMap.get(qf.getField());
			final String fieldPrefix = indexedFacetInfo == null || !indexedFacetInfo.isMultiSelect() ? ""
					: "{!tag=" + indexedFacetInfo.getKey() + "}";

			if (qf.getValues().size() == 1)
			{
				if (QueryField.ALL_FIELD.equals(qf.getField()))
				{
					joinedQueries.add(fieldPrefix + "(" + qf.getValues().iterator().next() + ")");
				}
				else
				{
					// NJC: Added escaping of field name
					joinedQueries.add(fieldPrefix + "(" + escape(qf.getField()) + ":" + qf.getValues().iterator().next() + ")");
				}
			}
			else
			{
				if (QueryField.ALL_FIELD.equals(qf.getField()))
				{
					joinedQueries.add(fieldPrefix + "("
							+ combine(qf.getValues().toArray(new String[qf.getValues().size()]), qf.getOperator().getName()) + "))");
				}
				else
				{
					// NJC: Added escaping of field name
					joinedQueries.add(fieldPrefix + "(" + escape(qf.getField()) + ":("
							+ combine(qf.getValues().toArray(new String[qf.getValues().size()]), qf.getOperator().getName()) + "))");
				}
			}

		}

		return joinedQueries.toArray(new String[joinedQueries.size()]);
	}

	protected String[] convertCoupledQueryFields(final SearchQuery searchQuery, final List<CoupledQueryField> coupledQueryFields)
	{
		if (CollectionUtils.isEmpty(coupledQueryFields))
		{
			return new String[0];
		}

		final List<String> joinedQueries = new ArrayList<String>();
		final Map<String, List<String>> couples = new HashMap<String, List<String>>();
		final Map<String, Operator> operatorMapping = new HashMap<String, SearchQuery.Operator>(coupledQueryFields.size());

		for (final CoupledQueryField qf : coupledQueryFields)
		{
			final StringBuilder couple = new StringBuilder();
			couple.append('(').append(prepareQueryField(qf.getField1())).append(qf.getInnerCouplingOperator().getName())
					.append(prepareQueryField(qf.getField2())).append(')');

			List<String> joinedCouples = couples.get(qf.getCoupleId());
			if (joinedCouples == null)
			{
				joinedCouples = new ArrayList<String>();
			}
			joinedCouples.add(couple.toString());
			couples.put(qf.getCoupleId(), joinedCouples);

			operatorMapping.put(qf.getCoupleId(), qf.getOuterCouplingOperator());
		}

		for (final Map.Entry<String, List<String>> entry : couples.entrySet())
		{
			final String coupleId = entry.getKey();
			final List<String> list = entry.getValue();
			joinedQueries.add("(" + combine(list.toArray(new String[list.size()]), operatorMapping.get(coupleId).getName()) + ")");
		}

		return joinedQueries.toArray(new String[joinedQueries.size()]);
	}

	protected String[] convertRawQueries(final SearchQuery searchQuery, final List<RawQuery> rawQueries)
	{
		if (CollectionUtils.isEmpty(rawQueries))
		{
			return new String[0];
		}

		final String[] queries = new String[rawQueries.size()];
		int index = 0;

		for (final RawQuery rawQuery : rawQueries)
		{
			final StringBuilder query = new StringBuilder();
			query.append('(');

			if (rawQuery.getField() != null)
			{
				final String convertedField = fieldNameTranslator.translate(searchQuery, rawQuery.getField(), FieldType.INDEX);
				query.append(convertedField);
				query.append(":(");
			}

			query.append(rawQuery.getQuery());

			if (rawQuery.getField() != null)
			{
				query.append(')');
			}

			query.append(')');

			queries[index] = query.toString();
			index++;
		}

		return queries;
	}

	protected String prepareQueryField(final QueryField field)
	{
		final StringBuilder result = new StringBuilder("(" + escape(field.getField()) + ":");
		if (field.getValues().size() == 1)
		{
			result.append(field.getValues().iterator().next()).append(")");
		}
		else
		{
			result.append("(")
					.append(combine(field.getValues().toArray(new String[field.getValues().size()]), field.getOperator().getName()))
					.append("))");
		}
		return result.toString();
	}

	protected IndexedTypeFieldsValuesProvider getFieldsValuesProvider(final IndexedType indexType)
	{
		final String name = indexType.getFieldsValuesProvider();
		return name == null ? null : beanFactory.getBean(name, IndexedTypeFieldsValuesProvider.class);
	}

	protected void addFacetFields(final SolrQuery solrQuery, final SearchQuery solrSearchQuery,
			final FieldNameProvider solrFieldNameProvider)
	{
		final Set<String> facets = solrSearchQuery.getIndexedType().getTypeFacets();
		for (final String facetName : facets)
		{
			solrQuery.addFacetField(getFieldNameTranslator().translate(solrSearchQuery, facetName, FieldType.INDEX));
		}
	}

	protected void addFacetFields(final SolrQuery solrQuery, final SearchQuery solrSearchQuery)
	{
		final Set<String> facets = solrSearchQuery.getIndexedType().getTypeFacets();
		for (final String facetName : facets)
		{
			solrQuery.addFacetField(getFieldNameTranslator().translate(solrSearchQuery, facetName, FieldType.INDEX));
		}
	}

	protected void addFacetFields(final SolrQuery solrQuery, final Map<String, IndexedFacetInfo> facetInfoMap)
	{
		for (final IndexedFacetInfo facetInfo : facetInfoMap.values())
		{
			if (facetInfo.isMultiSelect())
			{
				solrQuery.addFacetField("{!ex=" + facetInfo.getKey() + "}" + facetInfo.getTranslatedFieldName());
			}
			else
			{
				solrQuery.addFacetField(facetInfo.getTranslatedFieldName());
			}
		}
	}

	protected void addSolrParams(final SolrQuery solrQuery, final SearchQuery solrSearchQuery)
	{
		final Map<String, String[]> params = solrSearchQuery.getRawParams();
		final Set<String> keys = params.keySet();
		for (final String key : keys)
		{
			solrQuery.remove(key);
			solrQuery.add(key, params.get(key));
		}
	}

	protected SolrQuery applyPostProcessorsInOrder(final SolrQuery solrQuery, final SearchQuery solrSearchQuery)
	{
		if (solrQuery == null)
		{
			throw new IllegalArgumentException("SolrQuery may not be null!");
		}
		SolrQuery processedSolrQuery = solrQuery;
		if (queryPostProcessors != null)
		{
			for (final SolrQueryPostProcessor processor : queryPostProcessors)
			{
				processedSolrQuery = processor.process(processedSolrQuery, solrSearchQuery);
			}
		}
		return processedSolrQuery;
	}

	protected String combine(final String[] values, final String separator)
	{
		return StringUtils.join(values, separator);
	}

	protected List<QueryField> prepareQueryFields(final SearchQuery searchQuery)
	{
		final Map<String, QueryField> queryFields = new HashMap<String, QueryField>();
		// for compatibility, before it was being done by the addFacetValue method in the SearchQuery class

		prepareQueryFieldsFromFacetValues(searchQuery, queryFields);
		prepareQueryFieldsFromQueries(searchQuery, queryFields);

		for (final QueryField queryField : queryFields.values())
		{
			String field = queryField.getField();
			if (QueryField.ALL_FIELD.equals(field))
			{
				field = field + forbiddenChar + searchQuery.getLanguage();
			}
			else
			{
				field = getFieldNameTranslator().translate(searchQuery, field, FieldType.INDEX);
			}

			queryField.setField(field);
		}

		return new ArrayList<QueryField>(queryFields.values());
	}

	protected void prepareQueryFieldsFromFacetValues(final SearchQuery searchQuery, final Map<String, QueryField> queryFields)
	{
		for (final FacetValueField facetValue : searchQuery.getFacetValues())
		{
			QueryField queryField = queryFields.get(facetValue.getField());

			if (queryField == null)
			{
				queryField = new QueryField(facetValue.getField(), Operator.AND, new HashSet<String>());
				queryFields.put(facetValue.getField(), queryField);
			}

			if (CollectionUtils.isNotEmpty(facetValue.getValues()))
			{
				for (final String value : facetValue.getValues())
				{
					queryField.getValues().add(ClientUtils.escapeQueryChars(value));
				}
			}
		}
	}

	protected void prepareQueryFieldsFromQueries(final SearchQuery searchQuery, final Map<String, QueryField> queryFields)
	{
		for (final QueryField query : searchQuery.getQueries())
		{
			QueryField queryField = queryFields.get(query.getField());

			if (queryField == null)
			{
				queryField = new QueryField(query.getField(), query.getOperator(), new HashSet<String>());
				queryFields.put(query.getField(), queryField);
			}

			if (CollectionUtils.isNotEmpty(query.getValues()))
			{
				for (final String value : query.getValues())
				{
					queryField.getValues().add(ClientUtils.escapeQueryChars(value));
				}
			}
		}
	}

	protected Operator resolveOperator(final SearchQuery searchQuery)
	{
		if (searchQuery.getDefaultOperator() != null)
		{
			return searchQuery.getDefaultOperator();
		}

		if (getFieldOperator() != null)
		{
			return getFieldOperator();
		}

		return DEFAULT_FIELD_OPERATOR;
	}

	public enum FacetSort
	{
		INDEX(FacetParams.FACET_SORT_INDEX), COUNT(FacetParams.FACET_SORT_COUNT);

		private final String name;

		FacetSort(final String name)
		{
			this.name = name;
		}

		public String getName()
		{
			return name;
		}
	}

	public static class IndexedFacetInfo
	{
		private String translatedFieldName;
		private boolean multiSelect;
		private boolean multiSelectAnd;
		private boolean multiSelectOr;
		private String key;

		public String getTranslatedFieldName()
		{
			return translatedFieldName;
		}

		public void setTranslatedFieldName(final String translatedFieldName)
		{
			this.translatedFieldName = translatedFieldName;
		}

		public boolean isMultiSelect()
		{
			return multiSelect;
		}

		public void setMultiSelect(final boolean multiSelect)
		{
			this.multiSelect = multiSelect;
		}

		public boolean isMultiSelectAnd()
		{
			return multiSelectAnd;
		}

		public void setMultiSelectAnd(final boolean multiSelectAnd)
		{
			this.multiSelectAnd = multiSelectAnd;
		}

		public boolean isMultiSelectOr()
		{
			return multiSelectOr;
		}

		public void setMultiSelectOr(final boolean multiSelectOr)
		{
			this.multiSelectOr = multiSelectOr;
		}

		public String getKey()
		{
			return key;
		}

		public void setKey(final String key)
		{
			this.key = key;
		}
	}
}
