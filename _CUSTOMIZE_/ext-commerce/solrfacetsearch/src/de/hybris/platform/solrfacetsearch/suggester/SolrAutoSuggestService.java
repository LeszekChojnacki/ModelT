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

import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedTypeModel;
import de.hybris.platform.solrfacetsearch.suggester.exceptions.SolrAutoSuggestException;


/**
 * Service responsible for updating solr's suggester dictionary configuration and also for retrieving solr server's
 * suggestions based on user query input, language and indexed type.
 */
public interface SolrAutoSuggestService
{
	/**
	 * Returns solr's suggestions based on the indexed type, language and user's input. Suggestions come from dictionary
	 * build per indexed type and which is based on the fields which are marked for building the dictionary.
	 *
	 * @param language
	 *           - the language
	 * @param indexedType
	 *           - the indexed type
	 * @param query
	 *           -the query
	 *
	 * @return {@link SolrSuggestion}
	 *
	 * @throws SolrAutoSuggestException
	 *            if an error occurs
	 */
	SolrSuggestion getAutoSuggestionsForQuery(LanguageModel language, SolrIndexedTypeModel indexedType, String query)
			throws SolrAutoSuggestException;

}
