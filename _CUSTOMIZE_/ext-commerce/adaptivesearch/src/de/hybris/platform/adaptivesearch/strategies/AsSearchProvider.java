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
package de.hybris.platform.adaptivesearch.strategies;

import de.hybris.platform.adaptivesearch.AsException;
import de.hybris.platform.adaptivesearch.context.AsSearchProfileContext;
import de.hybris.platform.adaptivesearch.data.AsExpressionData;
import de.hybris.platform.adaptivesearch.data.AsIndexConfigurationData;
import de.hybris.platform.adaptivesearch.data.AsIndexPropertyData;
import de.hybris.platform.adaptivesearch.data.AsIndexTypeData;
import de.hybris.platform.adaptivesearch.data.AsSearchQueryData;
import de.hybris.platform.adaptivesearch.data.AsSearchResultData;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;

import java.util.List;
import java.util.Optional;


/**
 * Represent the different search providers.
 */
public interface AsSearchProvider
{
	/**
	 * Returns a list of index configurations.
	 *
	 * @return the index configurations
	 */
	List<AsIndexConfigurationData> getIndexConfigurations();

	/**
	 * Returns the index configuration for the provided code.
	 *
	 * @param code
	 *           - the index configuration code
	 *
	 * @return the index type
	 */
	Optional<AsIndexConfigurationData> getIndexConfigurationForCode(String code);

	/**
	 * Returns a list of index types.
	 *
	 * @return the index types
	 */
	List<AsIndexTypeData> getIndexTypes();

	/**
	 * Returns a list of index types for a given index configuration.
	 *
	 * @param indexConfiguration
	 *           - the index configuration code
	 *
	 * @return the index types
	 */
	List<AsIndexTypeData> getIndexTypes(String indexConfiguration);

	/**
	 * Returns the index type for the provided code.
	 *
	 * @param code
	 *           - the index type code
	 *
	 * @return the index type
	 */
	Optional<AsIndexTypeData> getIndexTypeForCode(String code);

	/**
	 * Returns a list of index properties for a given index type.
	 *
	 * @param indexType
	 *           - the index type code
	 *
	 * @return the index properties
	 */
	List<AsIndexPropertyData> getIndexProperties(String indexType);

	/**
	 * Returns the index property for the provided code.
	 *
	 * @param code
	 *           - the index property code
	 *
	 * @return the index property
	 */
	Optional<AsIndexPropertyData> getIndexPropertyForCode(final String indexType, String code);

	/**
	 * Returns a list of catalog versions for a given index configuration and type.
	 *
	 * @param indexConfiguration
	 *           - the index configuration
	 * @param indexType
	 *           - the index type
	 *
	 * @return the catalog versions
	 */
	List<CatalogVersionModel> getSupportedCatalogVersions(String indexConfiguration, String indexType);

	/**
	 * Returns a list of languages for a given index configuration and type.
	 *
	 * @param indexConfiguration
	 *           - the index configuration
	 * @param indexType
	 *           - the index type
	 *
	 * @return the languages
	 */
	List<LanguageModel> getSupportedLanguages(String indexConfiguration, String indexType);

	/**
	 * Returns a list of currencies for a given index configuration and type.
	 *
	 * @param indexConfiguration
	 *           - the index configuration
	 * @param indexType
	 *           - the index type
	 *
	 * @return the currencies
	 */
	List<CurrencyModel> getSupportedCurrencies(String indexConfiguration, String indexType);

	/**
	 * Returns a list of index properties for a given index type that can be used for facets.
	 *
	 * @param indexType
	 *           - the index type code
	 *
	 * @return the index properties
	 */
	List<AsIndexPropertyData> getSupportedFacetIndexProperties(String indexType);

	/**
	 * Checks is a given index property can be used for facets.
	 *
	 * @param indexType
	 *           - the index type code
	 * @param code
	 *           - the code of the index property
	 *
	 * @return <code>true</code> if the index property can be used for facets, <code>false</code> otherwise
	 */
	boolean isValidFacetIndexProperty(String indexType, String code);

	/**
	 * Returns a list of expressions for a given index type that can be used for sorts.
	 *
	 * @param indexType
	 *           - the index type code
	 *
	 * @return the expressions
	 */
	List<AsExpressionData> getSupportedSortExpressions(String indexType);

	/**
	 * Checks is a given expression can be used for facets.
	 *
	 * @param indexType
	 *           - the index type code
	 * @param expression
	 *           - the sort expression
	 *
	 * @return <code>true</code> if expression can be used for sorts, <code>false</code> otherwise
	 */
	boolean isValidSortExpression(String indexType, String expression);

	/**
	 * Performs a search query.
	 *
	 * @param context
	 *           - the search profile context
	 * @param searchQuery
	 *           - the search query
	 *
	 * @result the search result
	 *
	 * @throws AsException
	 *            if and exception occurs during the search
	 *
	 * @since 6.7
	 */
	AsSearchResultData search(AsSearchProfileContext context, AsSearchQueryData searchQuery) throws AsException;
}
