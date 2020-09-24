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
package de.hybris.platform.adaptivesearchsolr.strategies;

import de.hybris.platform.adaptivesearch.enums.AsBoostOperator;
import de.hybris.platform.adaptivesearch.enums.AsBoostType;
import de.hybris.platform.adaptivesearch.enums.AsFacetType;
import de.hybris.platform.solrfacetsearch.config.FacetType;
import de.hybris.platform.solrfacetsearch.search.BoostField.BoostType;
import de.hybris.platform.solrfacetsearch.search.SearchQuery.QueryOperator;


/**
 * Bidirectional adaptive search and solr type mappings.
 */
public interface SolrAsTypeMappingRegistry
{
	/**
	 * Converts a {@link AsFacetType} to {@link FacetType}.
	 *
	 * @param asFacetType
	 *           - the {@link AsFacetType} to convert
	 */
	FacetType toFacetType(AsFacetType asFacetType);

	/**
	 * Converts a {@link FacetType} to {@link AsFacetType}.
	 *
	 * @param facetType
	 *           - the {@link FacetType} to convert
	 */
	AsFacetType toAsFacetType(FacetType facetType);

	/**
	 * Converts a {@link AsBoostOperator} to {@link QueryOperator}.
	 *
	 * @param asBoostOperator
	 *           - the {@link AsBoostOperator} to convert
	 */
	QueryOperator toQueryOperator(AsBoostOperator asBoostOperator);

	/**
	 * Converts a {@link QueryOperator} to {@link AsBoostOperator}.
	 *
	 * @param boostOperator
	 *           - the {@link QueryOperator} to convert
	 */
	AsBoostOperator toAsBoostOperator(QueryOperator boostOperator);

	/**
	 * Converts a {@link AsBoostType} to {@link BoostType}.
	 *
	 * @param asBoostType
	 *           - the {@link AsBoostType} to convert
	 */
	BoostType toBoostType(final AsBoostType asBoostType);

	/**
	 * Converts a {@link BoostType} to {@link AsBoostType}.
	 *
	 * @param boostType
	 *           - the {@link BoostType} to convert
	 */
	AsBoostType toAsBoostType(final BoostType boostType);
}
