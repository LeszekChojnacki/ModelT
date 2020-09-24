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

import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FacetConfigServiceException;
import de.hybris.platform.solrfacetsearch.model.config.SolrIndexedPropertyModel;

import java.util.Collection;


/**
 * Generates the Solr field names for a given indexed property and a qualifier and field type. A qualifier can be
 * something like the language ISO code or currency. With this it's possible to store multiple values for one property,
 * such as localized values or multi-currency values. Field type denotes whether the field is used to store the value of
 * a field or sort it
 */
public interface FieldNameProvider
{

	enum FieldType
	{
		INDEX, SORT
	}

	/**
	 * Returns all field name for a given property and qualifier. (see FieldNameProvider class comment.)
	 *
	 * @param indexedProperty
	 *           the property to provide a field name for
	 * @param qualifier
	 *           the qualifier
	 * @return the Solr field name
	 */
	Collection<String> getFieldNames(IndexedProperty indexedProperty, String qualifier);

	/**
	 * Returns field name for a given property, qualifier and FieldType. (see FieldNameProvider class comment.)
	 *
	 * @param prop
	 *           the property to provide a field name for
	 * @param qualifier
	 *           the qualifier
	 * @return the Solr field name
	 * @throws FacetConfigServiceException
	 */
	String getFieldName(SolrIndexedPropertyModel prop, String qualifier, final FieldType fieldType)
			throws FacetConfigServiceException;

	/**
	 * Returns field name for a given property, qualifier and FieldType. (see FieldNameProvider class comment.)
	 *
	 * @param indexedProperty
	 *           the property to provide a field name for
	 * @param qualifier
	 *           the qualifier
	 * @return the Solr field name
	 */
	String getFieldName(final IndexedProperty indexedProperty, final String qualifier, FieldType fieldType);

	/**
	 * Takes a SolrDocument field name and returns the property name
	 *
	 * @param fieldName
	 *           the SolrDocumentField name
	 * @return the property name
	 */
	String getPropertyName(String fieldName);

}
