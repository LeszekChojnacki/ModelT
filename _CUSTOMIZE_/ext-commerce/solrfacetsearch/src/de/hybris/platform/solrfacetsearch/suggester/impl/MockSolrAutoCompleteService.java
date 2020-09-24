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
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedTypeModel;
import de.hybris.platform.solrfacetsearch.suggester.SolrAutoSuggestService;
import de.hybris.platform.solrfacetsearch.suggester.SolrSuggestion;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * Mock of {@link SolrAutoSuggestService}
 */
public class MockSolrAutoCompleteService implements SolrAutoSuggestService
{

	@Override
	public SolrSuggestion getAutoSuggestionsForQuery(final LanguageModel language, final SolrIndexedTypeModel indexedType,
			final String queryInput)
	{

		final Map<String, Collection<String>> suggestionMap = new HashMap<String, Collection<String>>();

		suggestionMap.put("alfa", Arrays.asList("", "beta"));
		suggestionMap.put("alfa", Arrays.asList("", "gamma"));
		suggestionMap.put("alfa", Arrays.asList("", "delta"));

		final Collection<String> collations = Arrays.asList("alfa beta", "alfa gamma", "alfa delta");

		return new SolrSuggestion(suggestionMap, collations);
	}

}
