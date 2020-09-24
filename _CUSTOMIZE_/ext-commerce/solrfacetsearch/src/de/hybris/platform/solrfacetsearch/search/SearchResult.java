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
package de.hybris.platform.solrfacetsearch.search;

import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.solrfacetsearch.config.IndexedTypeSort;
import de.hybris.platform.solrfacetsearch.enums.ConverterType;
import de.hybris.platform.solrfacetsearch.reporting.data.SearchQueryInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.response.QueryResponse;


public interface SearchResult
{
	/**
	 * Use for pagination
	 *
	 * @return the offset of the pages, 0 for the first page, 1 for the second, and so on
	 */
	int getOffset();

	/**
	 * Use for pagination
	 *
	 * @return size of the page
	 */
	int getPageSize();

	/**
	 * Use for pagination
	 *
	 * @return true if the next page is available, false otherwise.
	 */
	boolean hasNext();

	/**
	 * Use for pagination
	 *
	 * @return true if the previous page is available, false otherwise.
	 */
	boolean hasPrevious();

	/**
	 * Use for pagination
	 *
	 * @return number of pages
	 */
	long getNumberOfPages();

	/**
	 * Returns the number of results. If result grouping is being used, this is the number of groups.
	 *
	 * @return the number of results
	 */
	long getNumberOfResults();

	/**
	 * Searches for the identifiers saved in the index, for example, "sony_online", "ati_staged", or "just_name"
	 *
	 * @return collection of all identifiers
	 */
	List<String> getIdentifiers();

	/**
	 * Searches for all result PKs.
	 *
	 * @return resulting items PK
	 * @throws FacetSearchException
	 */
	List<PK> getResultPKs() throws FacetSearchException;

	/**
	 * Searches for item's codes.
	 *
	 * @return List items codes as Strings
	 * @throws FacetSearchException
	 */
	List<String> getResultCodes() throws FacetSearchException;

	/**
	 * Searches for all result items. Example, [product_01, product_02], or [cms_01, cms_02, cms_03]
	 *
	 * @return all items
	 * @throws FacetSearchException
	 */
	List<? extends ItemModel> getResults() throws FacetSearchException;

	/**
	 * Returns SOLR results in form of a simple data objects (DTO's)
	 */
	<T> List<T> getResultData(ConverterType converterType);

	/**
	 * Returns the document that represent the main results.
	 *
	 * @return the documents
	 */
	List<Document> getDocuments();

	/**
	 * Returns the group results.
	 *
	 * @return the group results
	 */
	List<SearchResultGroupCommand> getGroupCommands();

	/**
	 * Searches for all root facet names. Example: [manufacturers, prices, categories]
	 *
	 * @return all root facet names
	 */
	Set<String> getFacetNames();

	/**
	 * Checks if the specified name is a facet. For example, <b>manufacturers</b> --> true, <b>admin</b> --> false
	 *
	 * @param name
	 *           name of the root facet
	 * @return true if found, false otherwise
	 */
	boolean containsFacet(String name);

	/**
	 * Searches for the root of a facet tree which contains the Facet and all FacetValues, for example,
	 * <b>manufacturers</b>(Facet) contains [eizo, ati, ..](FacetValue)
	 *
	 * @param name
	 *           name of the facet
	 * @return found facet or null when facet with given name is not available
	 */
	Facet getFacet(String name);

	/**
	 * Searches for all available facets which contain the whole tree,
	 *
	 * manufacturers [eizo, ati, ..], prices [100-499,500-999],categories [online, staged]
	 *
	 * @return all available facets
	 */
	List<Facet> getFacets();

	/**
	 * Get the best suggestion from the spellchecker. Returns null if spell checker is not enabled, or no better suggestion
	 * than the user's query is found. Requires the UserQuery to be set on the SearchQuery.
	 */
	String getSpellingSuggestion();

	List<KeywordRedirectValue> getKeywordRedirects();

	/**
	 * Return list of available named sorts.
	 *
	 * @return List of {@link IndexedTypeSort}
	 */
	List<IndexedTypeSort> getAvailableNamedSorts();

	/**
	 * Return currently selected sort.
	 *
	 * @return current sort
	 */
	IndexedTypeSort getCurrentNamedSort();

	/**
	 * Return bread-crumbs for the current search result state.
	 *
	 * @return List of {@link Breadcrumb}
	 */
	List<Breadcrumb> getBreadcrumbs();

	/**
	 * Statistics Info about the Search Query - usable for logging
	 */
	SearchQueryInfo getQueryInfo();

	/**
	 * Searches for the underlying implementation.
	 *
	 * @return The result object of the underlying implementation.
	 */
	QueryResponse getSolrObject();

	/**
	 * Returns attributes for the current search result.
	 *
	 * @return the attributes
	 */
	Map<String, Object> getAttributes();

}
