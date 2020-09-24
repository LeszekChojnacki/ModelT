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
package de.hybris.platform.solrfacetsearch.jalo.config;

import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.c2l.Language;
import de.hybris.platform.solrfacetsearch.jalo.redirect.SolrFacetSearchKeywordRedirect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class SolrFacetSearchConfig extends GeneratedSolrFacetSearchConfig
{
	@Override
	public List<SolrSynonymConfig> getLanguageSynonymMapping(final SessionContext ctx)
	{
		final List<SolrSynonymConfig> synonyms = getAllLanguageSynonymMapping(ctx).get(ctx.getLanguage());
		return synonyms == null ? Collections.emptyList() : synonyms;
	}

	@Override
	public Map<Language, List<SolrSynonymConfig>> getAllLanguageSynonymMapping(final SessionContext ctx)
	{
		final Map<Language, List<SolrSynonymConfig>> map = new TreeMap<Language, List<SolrSynonymConfig>>();
		final List<SolrSynonymConfig> synonyms = getSynonyms();
		for (final SolrSynonymConfig syn : synonyms)
		{
			final Language lang = syn.getLanguage();
			if (!map.containsKey(lang))
			{
				map.put(lang, new ArrayList());
			}
			map.get(lang).add(syn);
		}
		return map;
	}

	@Override
	public void setLanguageSynonymMapping(final SessionContext ctx, final List<SolrSynonymConfig> value)
	{
		final List<SolrSynonymConfig> synonyms = new ArrayList<SolrSynonymConfig>(getSynonyms());
		synonyms.removeAll(getLanguageSynonymMapping(ctx));
		synonyms.addAll(value);
		setSynonyms(synonyms);
	}

	@Override
	public void setAllLanguageSynonymMapping(final SessionContext ctx, final Map<Language, List<SolrSynonymConfig>> value)
	{
		final List<SolrSynonymConfig> synonyms = new ArrayList();
		for (final Map.Entry<Language, List<SolrSynonymConfig>> langValue : value.entrySet())
		{
			synonyms.addAll(langValue.getValue());
		}

		setSynonyms(synonyms);
	}

	@Override
	public List<SolrFacetSearchKeywordRedirect> getLanguageKeywordRedirectMapping(final SessionContext ctx)
	{
		final List<SolrFacetSearchKeywordRedirect> keywordRedirects = getAllLanguageKeywordRedirectMapping(ctx).get(
				ctx.getLanguage());
		return keywordRedirects == null ? Collections.emptyList() : keywordRedirects;
	}

	@Override
	public Map<Language, List<SolrFacetSearchKeywordRedirect>> getAllLanguageKeywordRedirectMapping(final SessionContext ctx)
	{
		final Map<Language, List<SolrFacetSearchKeywordRedirect>> map = new TreeMap<Language, List<SolrFacetSearchKeywordRedirect>>();
		final Collection<SolrFacetSearchKeywordRedirect> keywordRedirects = getKeywordRedirects();
		for (final SolrFacetSearchKeywordRedirect syn : keywordRedirects)
		{
			final Language lang = syn.getLanguage();
			if (!map.containsKey(lang))
			{
				map.put(lang, new ArrayList());
			}
			map.get(lang).add(syn);
		}
		return map;
	}

	@Override
	public void setLanguageKeywordRedirectMapping(final SessionContext ctx, final List<SolrFacetSearchKeywordRedirect> value)
	{
		final List<SolrFacetSearchKeywordRedirect> keywordRedirects = new ArrayList<SolrFacetSearchKeywordRedirect>(
				getKeywordRedirects());
		keywordRedirects.removeAll(getLanguageKeywordRedirectMapping(ctx));
		keywordRedirects.addAll(value);
		setKeywordRedirects(keywordRedirects);
	}

	@Override
	public void setAllLanguageKeywordRedirectMapping(final SessionContext ctx,
			final Map<Language, List<SolrFacetSearchKeywordRedirect>> value)
	{
		// YTODO Auto-generated method stub

	}

	@Override
	public List<SolrStopWord> getLanguageStopWordMapping(final SessionContext ctx)
	{
		final List<SolrStopWord> stopWords = getAllLanguageStopWordMapping(ctx).get(ctx.getLanguage());
		return stopWords == null ? Collections.emptyList() : stopWords;
	}

	@Override
	public Map<Language, List<SolrStopWord>> getAllLanguageStopWordMapping(final SessionContext ctx)
	{
		final Map<Language, List<SolrStopWord>> map = new TreeMap<Language, List<SolrStopWord>>();
		final Collection<SolrStopWord> stopWords = getStopWords();
		for (final SolrStopWord syn : stopWords)
		{
			final Language lang = syn.getLanguage();
			if (!map.containsKey(lang))
			{
				map.put(lang, new ArrayList());
			}
			map.get(lang).add(syn);
		}
		return map;
	}

	@Override
	public void setLanguageStopWordMapping(final SessionContext ctx, final List<SolrStopWord> value)
	{
		final List<SolrStopWord> stopWords = new ArrayList<SolrStopWord>(getStopWords());
		stopWords.removeAll(getLanguageStopWordMapping(ctx));
		stopWords.addAll(value);
		setStopWords(stopWords);

	}

	@Override
	public void setAllLanguageStopWordMapping(final SessionContext ctx, final Map<Language, List<SolrStopWord>> value)
	{
		// YTODO Auto-generated method stub

	}

}
