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
package de.hybris.platform.solrfacetsearch.indexer.impl;

import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.spi.InputDocument;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.provider.RangeNameProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;


/**
 * Default implementation of {@link InputDocument}.
 */
public class DefaultSolrInputDocument implements InputDocument
{
	private final SolrInputDocument delegate;
	private final IndexerBatchContext batchContext;
	private final FieldNameProvider fieldNameProvider;
	private final RangeNameProvider rangeNameProvider;

	public DefaultSolrInputDocument(final SolrInputDocument delegate, final IndexerBatchContext batchContext,
			final FieldNameProvider fieldNameProvider, final RangeNameProvider rangeNameProvider)
	{
		this.delegate = delegate;
		this.batchContext = batchContext;
		this.fieldNameProvider = fieldNameProvider;
		this.rangeNameProvider = rangeNameProvider;
	}

	public SolrInputDocument getDelegate()
	{
		return delegate;
	}

	public IndexerBatchContext getBatchContext()
	{
		return batchContext;
	}

	public FieldNameProvider getFieldNameProvider()
	{
		return fieldNameProvider;
	}

	public RangeNameProvider getRangeNameProvider()
	{
		return rangeNameProvider;
	}

	@Override
	public void addField(final String fieldName, final Object value) throws FieldValueProviderException
	{
		if (value instanceof Collection)
		{
			delegate.addField(fieldName, new ArrayList((Collection) value));
		}
		else
		{
			delegate.addField(fieldName, value);
		}
	}

	@Override
	public void addField(final IndexedProperty indexedProperty, final Object value) throws FieldValueProviderException
	{
		addField(indexedProperty, value, null);
	}

	@Override
	public void addField(final IndexedProperty indexedProperty, final Object value, final String qualifier)
			throws FieldValueProviderException
	{
		final Collection<String> fieldNames = fieldNameProvider.getFieldNames(indexedProperty, qualifier);
		final List<String> rangeNameList = rangeNameProvider.getRangeNameList(indexedProperty, value, qualifier);

		for (final String fieldName : fieldNames)
		{
			if (rangeNameList.isEmpty())
			{
				addField(fieldName, value);
			}
			else
			{
				for (final String rangeName : rangeNameList)
				{
					addField(fieldName, rangeName == null ? value : rangeName);
				}
			}
		}
	}
	@Override
	public Object getFieldValue(final String fieldName)
	{
		final SolrInputField field = delegate.getField(fieldName);

		if (field == null)
		{
			return null;
		}

		return field.getValue();
	}

	@Override
	public Collection<String> getFieldNames()
	{
		return delegate.getFieldNames();
	}

	protected void startDocument()
	{
		// default implementation
	}

	protected void endDocument()
	{
		// default implementation
	}
}
