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
package de.hybris.platform.adaptivesearch.context;

import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;

import java.util.List;
import java.util.Map;


/**
 * This interface represents a context used for search profile related operations.
 */
public interface AsSearchProfileContext
{
	/**
	 * Returns the index configuration.
	 *
	 * @return the index configuration
	 */
	String getIndexConfiguration();

	/**
	 * Returns the index type.
	 *
	 * @return the index type
	 */
	String getIndexType();

	/**
	 * Returns the catalog versions.
	 *
	 * @return the catalog versions
	 */
	List<CatalogVersionModel> getCatalogVersions();

	/**
	 * Returns the session catalog versions. Sometimes the search query runs in a local session context which sets the
	 * catalog versions in the session to the ones used in the search query, for this reason this method should be used
	 * instead of {@link CatalogVersionService#getSessionCatalogVersions()}.
	 *
	 * @return the session catalog versions
	 */
	List<CatalogVersionModel> getSessionCatalogVersions();

	/**
	 * Returns the category path.
	 *
	 * @return the category path
	 */
	List<CategoryModel> getCategoryPath();

	/**
	 * Returns the language.
	 *
	 * @return the language
	 */
	LanguageModel getLanguage();

	/**
	 * Returns the currency.
	 *
	 * @return the currency
	 */
	CurrencyModel getCurrency();

	/**
	 * Returns the keywords.
	 *
	 * @return the keywords
	 */
	List<AsKeyword> getKeywords();

	/**
	 * Sets the keywords.
	 *
	 * @param keywords
	 *           - the keywords
	 *
	 */
	void setKeywords(List<AsKeyword> keywords);

	/**
	 * Returns the query.
	 *
	 * @return the query
	 */
	String getQuery();

	/**
	 * Sets the query.
	 *
	 * @param query
	 *           - the query
	 */
	void setQuery(String query);

	/**
	 * Returns a {@link Map} instance with the qualifiers.
	 *
	 * @return the map containing the attributes
	 */
	Map<String, List<String>> getQualifiers();

	/**
	 * Returns a {@link Map} instance that can be used to store attributes.
	 *
	 * @return the map containing the attributes
	 */
	Map<String, Object> getAttributes();
}
