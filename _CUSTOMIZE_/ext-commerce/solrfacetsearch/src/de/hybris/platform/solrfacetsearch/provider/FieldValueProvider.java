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
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;

import java.util.Collection;

import org.apache.log4j.Logger;


/**
 * Implementors for this interface should provide the field values to be indexed.
 *
 * @deprecated Since 6.5, use {@link ValueResolver} instead.
 */
@Deprecated
public interface FieldValueProvider
{
	/**
	 * Logger that should be used {@link FieldValueProvider#getFieldValues(IndexConfig, IndexedProperty, Object)} method.
	 */
	Logger LOG = Logger.getLogger("solrIndexThreadLogger");

	/**
	 * Returns a collection of {@link FieldValue} of a given indexedProperty that are fetched from the model based on the
	 * indexConfig. Supports multi-language and multi-currencies.
	 *
	 * @param indexConfig
	 * @param indexedProperty
	 * @param model
	 *
	 * @return Collection<{@link FieldValue}>
	 *
	 * @deprecated Since 5.5, use {@link ValueResolver} instead.
	 */
	@Deprecated
	Collection<FieldValue> getFieldValues(IndexConfig indexConfig, IndexedProperty indexedProperty, Object model)
			throws FieldValueProviderException;
}
