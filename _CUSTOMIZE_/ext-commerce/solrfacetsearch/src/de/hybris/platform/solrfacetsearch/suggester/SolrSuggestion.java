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
package de.hybris.platform.solrfacetsearch.suggester;

import java.util.Collection;
import java.util.Map;


/**
 * Pojo class that stores suggestions (autosuggestion/autocomplete) for a given query.
 */
public class SolrSuggestion
{
	private final Map<String, Collection<String>> suggestions;
	private final Collection<String> collations;

	public SolrSuggestion(final Map<String, Collection<String>> suggestions, final Collection<String> collations)
	{
		super();
		this.suggestions = suggestions;
		this.collations = collations;
	}

	/**
	 * @return the suggestions
	 */
	public Map<String, Collection<String>> getSuggestions()
	{
		return suggestions;
	}

	/**
	 * @return the collates
	 */
	public Collection<String> getCollates()
	{
		return collations;
	}

}
