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

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;

import java.util.List;


/**
 * Implementations of this interface are responsible for creating instances of {@link AsSearchProfileContext}.
 */
public interface AsSearchProfileContextFactory
{
	/**
	 * Creates a new instance of {@link AsSearchProfileContext}.
	 *
	 * @param indexConfiguration
	 *           - the index configuration
	 * @param indexType
	 *           - the index type
	 * @param catalogVersions
	 *           - list of the catalog versions
	 * @param categoryPath
	 *           - the category path
	 *
	 * @return the new instance
	 */
	AsSearchProfileContext createContext(String indexConfiguration, String indexType, List<CatalogVersionModel> catalogVersions,
			List<CategoryModel> categoryPath);


	/**
	 * Creates a new instance of {@link AsSearchProfileContext}.
	 *
	 * @param indexConfiguration
	 *           - the index configuration
	 * @param indexType
	 *           - the index type
	 * @param catalogVersions
	 *           - list of the catalog versions
	 * @param categoryPath
	 *           - the category path
	 * @param language
	 *           - the language
	 * @param currency
	 *           - the currency
	 *
	 * @return the new instance
	 */
	AsSearchProfileContext createContext(String indexConfiguration, String indexType, List<CatalogVersionModel> catalogVersions,
			List<CategoryModel> categoryPath, LanguageModel language, CurrencyModel currency);

	/**
	 * Creates a new instance of {@link AsSearchProfileContext}.
	 *
	 * @param indexConfiguration
	 *           - the index configuration
	 * @param indexType
	 *           - the index type
	 * @param catalogVersions
	 *           - the catalog versions
	 * @param sessionCatalogVersions
	 *           - the session catalog versions
	 * @param categoryPath
	 *           - the category path
	 * @param language
	 *           - the language
	 * @param currency
	 *           - the currency
	 *
	 * @return the new instance
	 */
	AsSearchProfileContext createContext(final String indexConfiguration, final String indexType,
			final List<CatalogVersionModel> catalogVersions, List<CatalogVersionModel> sessionCatalogVersions,
			List<CategoryModel> categoryPath, LanguageModel language, CurrencyModel currency);
}
