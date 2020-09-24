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
package de.hybris.platform.solrfacetsearch.indexer.spi;

import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;

import java.util.Collection;


/**
 * This interface represents a document to be indexed.
 */
public interface InputDocument
{
	/**
	 * Adds a field with the given name and value. If a field with the same name already exists, then the given value is
	 * appended to the value of that field. If the value is a collection, then each of its values will be added to the
	 * field.
	 *
	 * @param fieldName
	 *           - the field name
	 * @param value
	 *           - the field value
	 */
	void addField(String fieldName, Object value) throws FieldValueProviderException;

	/**
	 * Same as {@link #addField(IndexedProperty, Object, String)} with implied null value for the qualifier.
	 *
	 * @param indexedProperty
	 *           - the indexed property
	 * @param value
	 *           - the field value
	 *
	 * @see #addField(IndexedProperty, Object)
	 */
	void addField(IndexedProperty indexedProperty, Object value) throws FieldValueProviderException;

	/**
	 * Adds a field for the given indexed property, value and qualifier. If a field for the same indexed property and
	 * qualifier already exists, then the given value is appended to the value of that field. If the value is a
	 * collection, then each of its values will be added to the field.
	 *
	 * @param indexedProperty
	 *           - the indexed property
	 * @param value
	 *           - the field value
	 * @param qualifier
	 *           - the qualifier
	 */
	void addField(IndexedProperty indexedProperty, Object value, String qualifier) throws FieldValueProviderException;

	/**
	 * Gets the value for a certain field name.
	 *
	 * @param fieldName
	 * 			 - the field name
	 *
	 * @return the field value.
	 */
	Object getFieldValue(String fieldName);

	/**
	 * Get collection of field names
	 *
	 * @return field name collection
	 */
	Collection<String> getFieldNames();
}
