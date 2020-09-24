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
package de.hybris.platform.solrfacetsearch.provider.impl;


import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.servicelayer.session.Session;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.util.ServicesUtil;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.spi.InputDocument;
import de.hybris.platform.solrfacetsearch.provider.Qualifier;
import de.hybris.platform.solrfacetsearch.provider.QualifierProvider;
import de.hybris.platform.solrfacetsearch.provider.QualifierProviderAware;
import de.hybris.platform.solrfacetsearch.provider.ValueFilter;
import de.hybris.platform.solrfacetsearch.provider.ValueResolver;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Abstract class for value resolvers that want to use a {@link QualifierProvider}
 *
 * @param <T>
 *           the type of the model
 * @param <M>
 *           the type of the data that is valid in the context of a model
 * @param <Q>
 *           the type of the data that is valid in the context of a model and qualifier
 */
public abstract class AbstractValueResolver<T extends ItemModel, M, Q> implements ValueResolver<T>, QualifierProviderAware
{
	private SessionService sessionService;
	private QualifierProvider qualifierProvider;
	private Collection<ValueFilter> valueFilters;

	public SessionService getSessionService()
	{
		return sessionService;
	}

	@Required
	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	@Override
	public QualifierProvider getQualifierProvider()
	{
		return qualifierProvider;
	}

	@Required
	@Override
	public void setQualifierProvider(final QualifierProvider qualifierProvider)
	{
		this.qualifierProvider = qualifierProvider;
	}

	public Collection<ValueFilter> getValueFilters()
	{
		return valueFilters;
	}

	public void setValueFilters(final Collection<ValueFilter> valueFilters)
	{
		this.valueFilters = valueFilters;
	}

	@Override
	public void resolve(final InputDocument document, final IndexerBatchContext batchContext,
			final Collection<IndexedProperty> indexedProperties, final T model) throws FieldValueProviderException
	{
		ServicesUtil.validateParameterNotNull("model", "model instance is null");

		try
		{
			createLocalSessionContext();
			doResolve(document, batchContext, indexedProperties, model);
		}
		finally
		{
			removeLocalSessionContext();
		}
	}

	protected void doResolve(final InputDocument document, final IndexerBatchContext batchContext,
			final Collection<IndexedProperty> indexedProperties, final T model) throws FieldValueProviderException
	{
		final M data = loadData(batchContext, indexedProperties, model);

		final ValueResolverContext<M, Q> resolverContext = new ValueResolverContext<>();
		resolverContext.setData(data);

		// index properties without qualifier
		for (final IndexedProperty indexedProperty : indexedProperties)
		{
			if (!qualifierProvider.canApply(indexedProperty))
			{
				addFieldValues(document, batchContext, indexedProperty, model, resolverContext);
			}
		}

		// index properties with qualifier
		final FacetSearchConfig facetSearchConfig = batchContext.getFacetSearchConfig();
		final IndexedType indexedType = batchContext.getIndexedType();
		final Collection<Qualifier> qualifiers = qualifierProvider.getAvailableQualifiers(facetSearchConfig, indexedType);

		for (final Qualifier qualifier : qualifiers)
		{
			qualifierProvider.applyQualifier(qualifier);

			final String fieldQualifier = qualifier.toFieldQualifier();
			final Q qualifierData = loadQualifierData(batchContext, indexedProperties, model, qualifier);

			resolverContext.setQualifier(qualifier);
			resolverContext.setFieldQualifier(fieldQualifier);
			resolverContext.setQualifierData(qualifierData);

			for (final IndexedProperty indexedProperty : indexedProperties)
			{
				if (qualifierProvider.canApply(indexedProperty))
				{
					addFieldValues(document, batchContext, indexedProperty, model, resolverContext);
				}
			}
		}
	}

	/**
	 * Loads data that is valid in the context of a model.
	 *
	 * @param batchContext
	 *           - the current indexer batch context
	 * @param indexedProperties
	 *           - the indexed properties that use the same value resolver
	 * @param model
	 *           - the values should be resolved for this model instance
	 * @throws FieldValueProviderException
	 *            if an error occurs
	 */
	protected M loadData(final IndexerBatchContext batchContext, final Collection<IndexedProperty> indexedProperties,
			final T model) throws FieldValueProviderException
	{
		return null;
	}

	/**
	 * Loads data that is valid in the context of a model and qualifier.
	 *
	 * @param batchContext
	 *           - the current indexer batch context
	 * @param indexedProperties
	 *           - the indexed properties that use the same value resolver
	 * @param model
	 *           - the values should be resolved for this model instance
	 * @throws FieldValueProviderException
	 *            if an error occurs
	 */
	protected Q loadQualifierData(final IndexerBatchContext batchContext, final Collection<IndexedProperty> indexedProperties,
			final T model, final Qualifier qualifier) throws FieldValueProviderException
	{
		return null;
	}

	protected abstract void addFieldValues(final InputDocument document, final IndexerBatchContext batchContext,
			final IndexedProperty indexedProperty, final T model, ValueResolverContext<M, Q> resolverContext)
			throws FieldValueProviderException;

	protected Object filterFieldValue(final IndexerBatchContext batchContext, final IndexedProperty indexedProperty,
			final Object value)
	{
		Object filedValue = value;
		if (valueFilters != null && !valueFilters.isEmpty())
		{
			for (final ValueFilter valueFilter : valueFilters)
			{
				filedValue = valueFilter.doFilter(batchContext, indexedProperty, filedValue);
			}
		}

		return filedValue;
	}

	protected boolean addFieldValue(final InputDocument document, final IndexerBatchContext batchContext,
			final IndexedProperty indexedProperty, final Object value, final String qualifier) throws FieldValueProviderException
	{
		final boolean isString = value instanceof String;
		if (isString && StringUtils.isBlank((String) value))
		{
			return false;
		}

		document.addField(indexedProperty, value, qualifier);

		return true;
	}

	protected boolean filterAndAddFieldValues(final InputDocument document, final IndexerBatchContext batchContext,
			final IndexedProperty indexedProperty, final Object value, final String qualifier) throws FieldValueProviderException
	{
		boolean hasValue = false;
		Object fieldValue = value;

		if (fieldValue != null)
		{
			fieldValue = filterFieldValue(batchContext, indexedProperty, fieldValue);

			if (fieldValue instanceof Collection)
			{
				final Collection<Object> values = (Collection<Object>) fieldValue;

				for (final Object singleValue : values)
				{
					hasValue |= addFieldValue(document, batchContext, indexedProperty, singleValue, qualifier);
				}
			}
			else
			{
				hasValue = addFieldValue(document, batchContext, indexedProperty, fieldValue, qualifier);
			}
		}

		return hasValue;
	}

	protected void createLocalSessionContext()
	{
		final Session session = sessionService.getCurrentSession();
		final JaloSession jaloSession = (JaloSession) sessionService.getRawSession(session);
		jaloSession.createLocalSessionContext();
	}

	protected void removeLocalSessionContext()
	{
		final Session session = sessionService.getCurrentSession();
		final JaloSession jaloSession = (JaloSession) sessionService.getRawSession(session);
		jaloSession.removeLocalSessionContext();
	}

	protected static final class ValueResolverContext<T, U>
	{
		private T data;
		private U qualifierData;
		private Qualifier qualifier;
		private String fieldQualifier;

		public T getData()
		{
			return data;
		}

		public void setData(final T data)
		{
			this.data = data;
		}

		public U getQualifierData()
		{
			return qualifierData;
		}

		public void setQualifierData(final U qualifierData)
		{
			this.qualifierData = qualifierData;
		}

		public Qualifier getQualifier()
		{
			return qualifier;
		}

		public void setQualifier(final Qualifier qualifier)
		{
			this.qualifier = qualifier;
		}

		public String getFieldQualifier()
		{
			return fieldQualifier;
		}

		public void setFieldQualifier(final String fieldQualifier)
		{
			this.fieldQualifier = fieldQualifier;
		}
	}
}
