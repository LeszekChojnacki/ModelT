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
package de.hybris.platform.solrfacetsearch.suggester.impl;

import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.solrfacetsearch.common.AbstractYSolrService;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.exceptions.FacetConfigServiceException;
import de.hybris.platform.solrfacetsearch.indexer.SolrIndexedTypeCodeResolver;
import de.hybris.platform.solrfacetsearch.model.SolrIndexModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedPropertyModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedTypeModel;
import de.hybris.platform.solrfacetsearch.solr.Index;
import de.hybris.platform.solrfacetsearch.solr.SolrIndexService;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProvider;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProviderFactory;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;
import de.hybris.platform.solrfacetsearch.suggester.SolrAutoSuggestService;
import de.hybris.platform.solrfacetsearch.suggester.SolrSuggestion;
import de.hybris.platform.solrfacetsearch.suggester.exceptions.SolrAutoSuggestException;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Collation;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Suggestion;
import org.apache.solr.client.solrj.response.SuggesterResponse;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation for {@link SolrAutoSuggestService}
 */
public class DefaultSolrAutoSuggestService extends AbstractYSolrService implements SolrAutoSuggestService
{
	private static final Logger LOG = Logger.getLogger(DefaultSolrAutoSuggestService.class);
	private static final String ENCODING = "UTF-8";

	public static final String SUGGESTER_QUERY_TYPE = "/suggest";

	public static final String SUGGEST_QUERY = "suggest.q";
	public static final String SUGGEST_DICTIONARY = "suggest.dictionary";

	public static final String SPELLCHECK_QUERY = "spellcheck.q";
	public static final String SPELLCHECK_DICTIONARY = "spellcheck.dictionary";

	private SolrIndexService solrIndexService;
	private SolrSearchProviderFactory solrSearchProviderFactory;
	private SolrIndexedTypeCodeResolver solrIndexedTypeCodeResolver;

	@Override
	public SolrSuggestion getAutoSuggestionsForQuery(final LanguageModel language, final SolrIndexedTypeModel solrIndexedType,
			final String queryInput) throws SolrAutoSuggestException
	{
		SolrClient solrClient = null;

		if (StringUtils.isNotBlank(queryInput))
		{

			try
			{
				final String dictionary = language.getIsocode();

				final String configName = solrIndexedType.getSolrFacetSearchConfig().getName();
				final FacetSearchConfig facetSearchConfig = facetSearchConfigService.getConfiguration(configName);
				final IndexedType indexedType = facetSearchConfig.getIndexConfig().getIndexedTypes()
						.get(solrIndexedTypeCodeResolver.resolveIndexedTypeCode(solrIndexedType));

				final SolrSearchProvider solrSearchProvider = solrSearchProviderFactory.getSearchProvider(facetSearchConfig,
						indexedType);

				final SolrIndexModel solrIndex = solrIndexService.getActiveIndex(facetSearchConfig.getName(),
						indexedType.getIdentifier());
				final Index index = solrSearchProvider.resolveIndex(facetSearchConfig, indexedType, solrIndex.getQualifier());
				solrClient = solrSearchProvider.getClient(index);

				final SolrQuery query = new SolrQuery();
				query.setQuery(queryInput);
				query.setRequestHandler(SUGGESTER_QUERY_TYPE);

				// used when the suggest component is enabled
				query.set(SUGGEST_QUERY, queryInput);
				query.set(SUGGEST_DICTIONARY, dictionary);

				// used when the spellcheck component is enabled
				query.set(SPELLCHECK_QUERY, queryInput);
				query.set(SPELLCHECK_DICTIONARY, dictionary);

				if (LOG.isDebugEnabled())
				{
					LOG.debug("Solr Suggest Query: \n" + URLDecoder.decode(query.toString(), ENCODING));
				}

				final QueryResponse response = solrClient.query(index.getName(), query);

				if (LOG.isDebugEnabled())
				{
					LOG.debug("Solr Suggest Response: \n" + response);
				}

				final SuggesterResponse suggesterResponse = response.getSuggesterResponse();
				if (suggesterResponse != null)
				{
					return createResultFromSuggesterResponse(suggesterResponse, dictionary);
				}

				final SpellCheckResponse spellCheckResponse = response.getSpellCheckResponse();
				if (spellCheckResponse != null)
				{
					return createResultFromSpellCheckResponse(spellCheckResponse);
				}
			}
			catch (final SolrServiceException | FacetConfigServiceException | SolrServerException | IOException e)
			{
				throw new SolrAutoSuggestException("Error issuing suggestion query", e);
			}
			finally
			{
				IOUtils.closeQuietly(solrClient);
			}
		}

		return new SolrSuggestion(new HashMap<>(), new ArrayList<>());
	}

	@Override
	protected boolean checkIfIndexPropertyQualifies(final SolrIndexedPropertyModel indexedProperty)
	{
		return Boolean.TRUE.equals(indexedProperty.getUseForAutocomplete()) && !indexedProperty.isCurrency();
	}

	protected SolrSuggestion createResultFromSuggesterResponse(final SuggesterResponse suggesterResponse, final String dictionary)
	{
		final Map<String, Collection<String>> resultSuggestions = new HashMap<>();
		final Collection<String> resultCollations = new LinkedHashSet<>();

		final Map<String, List<String>> suggestedTerms = suggesterResponse.getSuggestedTerms();
		if (MapUtils.isNotEmpty(suggestedTerms))
		{
			final Collection<String> collations = suggestedTerms.get(dictionary);
			if (CollectionUtils.isNotEmpty(collations))
			{
				resultCollations.addAll(collations);
			}
		}

		return new SolrSuggestion(resultSuggestions, resultCollations);
	}

	protected SolrSuggestion createResultFromSpellCheckResponse(final SpellCheckResponse spellCheckResponse)
	{
		final Map<String, Collection<String>> resultSuggestions = new HashMap<>();
		final Collection<String> resultCollations = new LinkedHashSet<>();

		final List<Suggestion> suggestions = spellCheckResponse.getSuggestions();
		for (final Suggestion suggestion : suggestions)
		{
			final List<String> alternatives = suggestion.getAlternatives();
			resultSuggestions.put(suggestion.getToken(), alternatives);
		}

		final List<Collation> collatedResults = spellCheckResponse.getCollatedResults();
		if (collatedResults != null)
		{
			for (final Collation collation : collatedResults)
			{
				resultCollations.add(collation.getCollationQueryString());
			}
		}

		return new SolrSuggestion(resultSuggestions, resultCollations);
	}

	protected void populateSuggestionsFromResponse(final Map<String, Collection<String>> resultSuggestionMap,
			final Collection<String> resultCollations, final SpellCheckResponse spellCheckResponse)
	{
		final List<Suggestion> suggestions = spellCheckResponse.getSuggestions();
		for (final Suggestion suggestion : suggestions)
		{
			final List<String> alternatives = suggestion.getAlternatives();
			resultSuggestionMap.put(suggestion.getToken(), alternatives);
		}

		final List<Collation> collatedResults = spellCheckResponse.getCollatedResults();
		if (collatedResults != null)
		{
			for (final Collation collation : collatedResults)
			{
				resultCollations.add(collation.getCollationQueryString());
			}
		}
	}

	public SolrIndexService getSolrIndexService()
	{
		return solrIndexService;
	}

	@Required
	public void setSolrIndexService(final SolrIndexService solrIndexService)
	{
		this.solrIndexService = solrIndexService;
	}

	public SolrSearchProviderFactory getSolrSearchProviderFactory()
	{
		return solrSearchProviderFactory;
	}

	@Required
	public void setSolrSearchProviderFactory(final SolrSearchProviderFactory solrSearchProviderFactory)
	{
		this.solrSearchProviderFactory = solrSearchProviderFactory;
	}

	public SolrIndexedTypeCodeResolver getSolrIndexedTypeCodeResolver()
	{
		return solrIndexedTypeCodeResolver;
	}

	@Required
	public void setSolrIndexedTypeCodeResolver(final SolrIndexedTypeCodeResolver solrIndexedTypeCodeResolver)
	{
		this.solrIndexedTypeCodeResolver = solrIndexedTypeCodeResolver;
	}

}
