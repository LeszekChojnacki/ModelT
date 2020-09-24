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

import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;

import java.util.Collection;
import java.util.Map;
import java.util.Set;


/**
 * Fields values provider for the entire model.
 */
public interface IndexedTypeFieldsValuesProvider
{
	/**
	 * Returns a collection of {@link FieldValue} of a given model that are fetched from it based on the indexConfig and
	 * customized implemented logic. Supports multi-language and multi-currencies.
	 * 
	 * @param indexConfig
	 * @param model
	 * @throws FieldValueProviderException
	 * @return Collection<{@link FieldValue}>
	 */
	Collection<FieldValue> getFieldValues(IndexConfig indexConfig, Object model) throws FieldValueProviderException;

	/**
	 * Returns set of facets names that are provided.
	 * 
	 * @return Set
	 */
	Set<String> getFacets();

	/**
	 * Returns field to index field names mapping for the properties provided.
	 * 
	 * @return Map
	 */
	Map<String, String> getFieldNamesMapping();
}
