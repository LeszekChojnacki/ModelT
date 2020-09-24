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

import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.IndexerQueryContext;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.provider.RangeNameProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;


/**
 * Implementation of {@link IndexerQueryContext} for partial updates.
 */
public class DefaultSolrPartialUpdateInputDocument extends DefaultSolrInputDocument
{
	private final Set<String> indexedFields;
	private Set<String> notUpdatedIndexedFields;

	public DefaultSolrPartialUpdateInputDocument(final SolrInputDocument delegate, final IndexerBatchContext batchContext,
			final FieldNameProvider fieldNameProvider, final RangeNameProvider rangeNameProvider, final Set<String> indexedFields)
	{
		super(delegate, batchContext, fieldNameProvider, rangeNameProvider);
		this.indexedFields = indexedFields;
	}

	public Set<String> getIndexedFields()
	{
		return indexedFields;
	}

	public Set<String> getNotUpdatedIndexedFields()
	{
		return notUpdatedIndexedFields;
	}

	@Override
	public void addField(final String fieldName, final Object value) throws FieldValueProviderException
	{
		notUpdatedIndexedFields.remove(fieldName);

		Map<String, Object> fieldModifier = (Map<String, Object>) getDelegate().getFieldValue(fieldName);
		if (fieldModifier == null)
		{
			fieldModifier = new HashMap<>();
			fieldModifier.put("set", value instanceof Collection ? new ArrayList((Collection) value) : value);

			getDelegate().addField(fieldName, fieldModifier);

			return;
		}

		final Object fieldValue = fieldModifier.get("set");
		Collection newFieldValue;

		if (fieldValue instanceof Collection)
		{
			newFieldValue = (Collection) fieldValue;
		}
		else
		{
			newFieldValue = new ArrayList();
			newFieldValue.add(fieldValue);

			fieldModifier.put("set", newFieldValue);
		}

		if (value instanceof Collection)
		{
			newFieldValue.addAll((Collection) value);
		}
		else
		{
			newFieldValue.add(value);
		}
	}

	@Override
	public Object getFieldValue(final String fieldName)
	{
		final SolrInputField field = getDelegate().getField(fieldName);

		if (field == null)
		{
			return null;
		}

		final Object value = field.getValue();

		if (value instanceof Map)
		{
			return ((Map<String, Object>) value).get("set");
		}
		else
		{
			return value;
		}
	}

	@Override
	protected void startDocument()
	{
		notUpdatedIndexedFields = new HashSet<>(indexedFields);
	}

	@Override
	protected void endDocument()
	{
		// removes all fields that were not updated from the index (for the qualifiers that no longer exist)
		for (final String fieldName : notUpdatedIndexedFields)
		{
			final Map<String, Object> fieldModifier = new HashMap<>(1);
			fieldModifier.put("set", null);
			getDelegate().addField(fieldName, fieldModifier);
		}
	}
}
