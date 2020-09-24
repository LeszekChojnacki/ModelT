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

import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider.FieldType;
import de.hybris.platform.solrfacetsearch.search.impl.SolrResult;

import org.springframework.beans.factory.annotation.Required;


/**
 * Defines common implementation of conversion from SolrResult to Data (DTO) object.
 *
 * @see de.hybris.platform.solrfacetsearch.search.impl.SolrResult
 *
 *
 */
public abstract class AbstractSolrConverter<T> implements Converter<SolrResult, T>
{

	private FieldNameTranslator fieldNameTranslator;

	@Override
	public T convert(final SolrResult source)
	{
		return convert(source, createDataObject());
	}


	/**
	 * Returns empty template instance for the conversion target
	 */
	protected abstract T createDataObject();


	/**
	 * Returns value of the indexed property from the SOLR search result by (not translated) name .
	 *
	 * @param solrResult
	 *           {@link SolrResult}
	 * @param propertyName
	 *           - non-translated property name
	 *
	 */
	protected <TYPE> TYPE getValue(final SolrResult solrResult, final String propertyName)
	{
		final IndexedProperty indexedProperty = solrResult.getQuery().getIndexedType().getIndexedProperties().get(propertyName);
		if (indexedProperty == null)
		{
			//try to get by property name (like pk, which has no indexedProperty)
			return (TYPE) solrResult.getDocument().getFirstValue(propertyName);
		}

		// DO NOT REMOVE the cast (TYPE) below, while it should be unnecessary it is required by the javac compiler
		return (TYPE) getValue(solrResult, indexedProperty);
	}

	/**
	 * Returns value of the indexed property from the SOLR search result by {@link IndexedProperty} instance.
	 *
	 * @param solrResult
	 *           {@link SolrResult}
	 * @param property
	 *           - {@link IndexedProperty}
	 *
	 */
	protected <TYPE> TYPE getValue(final SolrResult solrResult, final IndexedProperty property)
	{
		final String fieldName = translateFieldName(solrResult.getQuery(), property);
		if (property.isMultiValue())
		{
			return (TYPE) solrResult.getDocument().getFieldValues(fieldName);
		}
		return (TYPE) solrResult.getDocument().getFirstValue(fieldName);
	}


	protected String translateFieldName(final SearchQuery searchQuery, final IndexedProperty property)
	{
		return getFieldNameTranslator().translate(searchQuery, property.getName(), FieldType.INDEX);
	}

	protected FieldNameTranslator getFieldNameTranslator()
	{
		return fieldNameTranslator;
	}

	@Required
	public void setFieldNameTranslator(final FieldNameTranslator fieldNameTranslator)
	{
		this.fieldNameTranslator = fieldNameTranslator;
	}
}
