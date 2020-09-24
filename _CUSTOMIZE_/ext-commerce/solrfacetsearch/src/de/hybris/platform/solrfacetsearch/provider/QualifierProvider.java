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
package de.hybris.platform.solrfacetsearch.provider;

import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;

import java.util.Collection;
import java.util.Set;


/**
 * This interface provides support for different types of qualifiers.
 */
public interface QualifierProvider
{
	/**
	 * Returns all the supported types by this provider.
	 *
	 * @return the supported types
	 */
	Set<Class<?>> getSupportedTypes();

	/**
	 * Returns all the possible qualifiers for a given index configuration and indexed type.
	 *
	 * @param facetSearchConfig
	 *           - the facet search configuration
	 * @param indexedType
	 *           - the indexed type
	 *
	 * @return the available qualifiers
	 */
	Collection<Qualifier> getAvailableQualifiers(FacetSearchConfig facetSearchConfig, IndexedType indexedType);

	/**
	 * Checks if qualifiers can be applied/used with the indexed property passed as parameter.
	 *
	 * @param indexedProperty
	 *           - the indexed property
	 *
	 * @return {@code true} if qualifiers can be used, {@code false} otherwise
	 */
	boolean canApply(IndexedProperty indexedProperty);

	/**
	 * Applies the qualifier passed as parameter. This normally consists in setting some attributes on the session, e.g.
	 * by calling a service to set the current session language, currency, etc.
	 *
	 * @param qualifier
	 *           - the {@link Qualifier} to be applied
	 */
	void applyQualifier(Qualifier qualifier);

	/**
	 * Returns the current qualifier. This normally consists in getting some attributes from the session and creating the
	 * corresponding qualifier.
	 *
	 * @return the current qualifier
	 */
	Qualifier getCurrentQualifier();
}
