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
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.search.FieldNameTranslator;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.impl.SearchQueryConverterData;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.beans.factory.annotation.Required;


/**
 * Populates search query with fields used for highlighting the search term in results.
 */
public class FacetSearchQueryHighlightingFieldsPopulator implements Populator<SearchQueryConverterData, SolrQuery>
{
	public static final String HIGHLIGHTING_METHOD = "solrfacetsearch.search.highlighting.method";
	public static final String HIGHLIGHTING_METHOD_UNIFIED = "unified";
	public static final String HIGHLIGHTING_TAG_PRE = "solrfacetsearch.search.highlighting.tag.pre";
	public static final String HIGHLIGHTING_TAG_PRE_EM = "<em>";
	public static final String HIGHLIGHTING_TAG_POST = "solrfacetsearch.search.highlighting.tag.post";
	public static final String HIGHLIGHTING_TAG_POST_EM = "</em>";
	public static final String HIGHLIGHTING_REQUIRE_FIELD_MATCH = "solrfacetsearch.search.highlighting.requireFieldMatch";
	public static final String HIGHLIGHTING_SNIPPETS = "solrfacetsearch.search.highlighting.snippets";
	public static final int HIGHLIGHTING_SNIPPETS_DEFAULT = 3;

	public static final String SOLR_HIGHLIGHTING_METHOD_PARAM = "hl.method";
	public static final String SOLR_HIGHLIGHTING_TAG_PRE = "hl.tag.pre";
	public static final String SOLR_HIGHLIGHTING_TAG_POST = "hl.tag.post";

	private FieldNameTranslator fieldNameTranslator;
	private ConfigurationService configurationService;

	@Override
	public void populate(final SearchQueryConverterData source, final SolrQuery target)
	{
		final SearchQuery searchQuery = source.getSearchQuery();
		if (CollectionUtils.isEmpty(searchQuery.getHighlightingFields()))
		{
			return;
		}

		final Configuration configuration = configurationService.getConfiguration();
		final String highlightingMethod = configuration.getString(HIGHLIGHTING_METHOD, HIGHLIGHTING_METHOD_UNIFIED);
		final String highlightingTagPre = configuration.getString(HIGHLIGHTING_TAG_PRE, HIGHLIGHTING_TAG_PRE_EM);
		final String highlightingTagPost = configuration.getString(HIGHLIGHTING_TAG_POST, HIGHLIGHTING_TAG_POST_EM);
		final int highlightingSnippets = configuration.getInt(HIGHLIGHTING_SNIPPETS, HIGHLIGHTING_SNIPPETS_DEFAULT);
		final boolean requireFieldMatch = configuration.getBoolean(HIGHLIGHTING_REQUIRE_FIELD_MATCH, true);

		searchQuery.getHighlightingFields().stream().forEach(field -> target
				.addHighlightField(fieldNameTranslator.translate(searchQuery, field, FieldNameProvider.FieldType.INDEX)));
		target.setHighlight(true);
		target.setHighlightSnippets(highlightingSnippets);
		target.setHighlightRequireFieldMatch(requireFieldMatch);
		target.add(SOLR_HIGHLIGHTING_METHOD_PARAM, highlightingMethod);
		target.add(SOLR_HIGHLIGHTING_TAG_PRE, highlightingTagPre);
		target.add(SOLR_HIGHLIGHTING_TAG_POST, highlightingTagPost);
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

	public ConfigurationService getConfigurationService()
	{
		return configurationService;
	}

	@Required
	public void setConfigurationService(final ConfigurationService configurationService)
	{
		this.configurationService = configurationService;
	}
}
