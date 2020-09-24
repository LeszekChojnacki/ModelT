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

import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.constants.SolrfacetsearchConstants;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContextFactory;
import de.hybris.platform.solrfacetsearch.indexer.spi.InputDocument;
import de.hybris.platform.solrfacetsearch.indexer.spi.SolrDocumentFactory;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.provider.FieldValue;
import de.hybris.platform.solrfacetsearch.provider.FieldValueProvider;
import de.hybris.platform.solrfacetsearch.provider.IdentityProvider;
import de.hybris.platform.solrfacetsearch.provider.IndexedTypeFieldsValuesProvider;
import de.hybris.platform.solrfacetsearch.provider.RangeNameProvider;
import de.hybris.platform.solrfacetsearch.provider.TypeValueResolver;
import de.hybris.platform.solrfacetsearch.provider.ValueProviderSelectionStrategy;
import de.hybris.platform.solrfacetsearch.provider.ValueResolver;
import de.hybris.platform.solrfacetsearch.solr.Index;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProvider;
import de.hybris.platform.solrfacetsearch.solr.SolrSearchProviderFactory;
import de.hybris.platform.solrfacetsearch.solr.exceptions.SolrServiceException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.LukeRequest;
import org.apache.solr.client.solrj.response.LukeResponse;
import org.apache.solr.client.solrj.response.LukeResponse.FieldInfo;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link SolrDocumentFactory}.
 *
 * @deprecated since 18.08, functionality is moved to {@link DefaultIndexer}
 */
@Deprecated

public class DefaultSolrDocumentFactory implements SolrDocumentFactory, BeanFactoryAware
{
	private static final Logger LOG = Logger.getLogger("solrIndexThreadLogger");

	protected static final String VALUE_PROVIDERS_KEY = "solrfacetsearch.valueProviders";
	protected static final String INDEXED_FIELDS_KEY = "solrfacetsearch.indexedFields";

	private ModelService modelService;
	private TypeService typeService;
	private SolrSearchProviderFactory solrSearchProviderFactory;
	private IndexerBatchContextFactory<?> indexerBatchContextFactory;
	private FieldNameProvider fieldNameProvider;
	private RangeNameProvider rangeNameProvider;
	private ValueProviderSelectionStrategy valueProviderSelectionStrategy;
	private BeanFactory beanFactory;

	public ModelService getModelService()
	{
		return modelService;
	}

	@Required
	public void setModelService(final ModelService modelService)
	{
		this.modelService = modelService;
	}

	public TypeService getTypeService()
	{
		return typeService;
	}

	@Required
	public void setTypeService(final TypeService typeService)
	{
		this.typeService = typeService;
	}

	public SolrSearchProviderFactory getSolrSearchProviderFactory()
	{
		return solrSearchProviderFactory;
	}

	@Required
	public void setSolrSearchProviderFactory(final SolrSearchProviderFactory solrSearchProviderFactory)
	{
		this.solrSearchProviderFactory = solrSearchProviderFactory;
	}

	public IndexerBatchContextFactory getIndexerBatchContextFactory()
	{
		return indexerBatchContextFactory;
	}

	@Required
	public void setIndexerBatchContextFactory(final IndexerBatchContextFactory<?> indexerBatchContextFactory)
	{
		this.indexerBatchContextFactory = indexerBatchContextFactory;
	}

	public FieldNameProvider getFieldNameProvider()
	{
		return fieldNameProvider;
	}

	@Required
	public void setFieldNameProvider(final FieldNameProvider fieldNameProvider)
	{
		this.fieldNameProvider = fieldNameProvider;
	}

	public RangeNameProvider getRangeNameProvider()
	{
		return rangeNameProvider;
	}

	@Required
	public void setRangeNameProvider(final RangeNameProvider rangeNameProvider)
	{
		this.rangeNameProvider = rangeNameProvider;
	}

	public ValueProviderSelectionStrategy getValueProviderSelectionStrategy()
	{
		return valueProviderSelectionStrategy;
	}

	@Required
	public void setValueProviderSelectionStrategy(final ValueProviderSelectionStrategy valueProviderSelectionStrategy)
	{
		this.valueProviderSelectionStrategy = valueProviderSelectionStrategy;
	}

	public BeanFactory getBeanFactory()
	{
		return beanFactory;
	}

	@Override
	public void setBeanFactory(final BeanFactory beanFactory)
	{
		this.beanFactory = beanFactory;
	}

	@Override
	public SolrInputDocument createInputDocument(final ItemModel model, final IndexConfig indexConfig,
			final IndexedType indexedType) throws FieldValueProviderException
	{
		validateCommonRequiredParameters(model, indexConfig, indexedType);

		final IndexerBatchContext batchContext = indexerBatchContextFactory.getContext();
		final SolrInputDocument doc = new SolrInputDocument();
		final DefaultSolrInputDocument wrappedDoc = createWrappedDocument(batchContext, doc);

		wrappedDoc.startDocument();

		// this field should not be added for partial updates
		doc.addField(SolrfacetsearchConstants.INDEX_OPERATION_ID_FIELD, Long.valueOf(batchContext.getIndexOperationId()));

		addCommonFields(doc, batchContext, model);
		addIndexedPropertyFields(wrappedDoc, batchContext, model);
		addIndexedTypeFields(wrappedDoc, batchContext, model);

		wrappedDoc.endDocument();

		return doc;
	}

	@Override
	public SolrInputDocument createInputDocument(final ItemModel model, final IndexConfig indexConfig,
			final IndexedType indexedType, final Collection<IndexedProperty> indexedProperties) throws FieldValueProviderException
	{
		validateCommonRequiredParameters(model, indexConfig, indexedType);

		final IndexerBatchContext batchContext = indexerBatchContextFactory.getContext();
		final Set<String> indexedFields = getIndexedFields(batchContext);
		final SolrInputDocument doc = new SolrInputDocument();
		final DefaultSolrInputDocument wrappedDoc = createWrappedDocumentForPartialUpdates(batchContext, doc, indexedFields);

		wrappedDoc.startDocument();

		addCommonFields(doc, batchContext, model);
		addIndexedPropertyFields(wrappedDoc, batchContext, model);

		wrappedDoc.endDocument();

		return doc;
	}

	protected void validateCommonRequiredParameters(final ItemModel item, final IndexConfig indexConfig,
			final IndexedType indexedType)
	{
		if (item == null)
		{
			throw new IllegalArgumentException("item must not be null");
		}

		if (indexConfig == null)
		{
			throw new IllegalArgumentException("indexConfig must not be null");
		}

		if (indexedType == null)
		{
			throw new IllegalArgumentException("indexedType must not be null");
		}
	}

	protected DefaultSolrInputDocument createWrappedDocument(final IndexerBatchContext batchContext,
			final SolrInputDocument delegate)
	{
		return new DefaultSolrInputDocument(delegate, batchContext, fieldNameProvider, rangeNameProvider);
	}

	protected DefaultSolrInputDocument createWrappedDocumentForPartialUpdates(final IndexerBatchContext batchContext,
			final SolrInputDocument delegate, final Set<String> indexedPropertiesFields)
	{
		return new DefaultSolrPartialUpdateInputDocument(delegate, batchContext, fieldNameProvider, rangeNameProvider,
				indexedPropertiesFields);
	}

	protected void addCommonFields(final SolrInputDocument document, final IndexerBatchContext batchContext, final ItemModel model)
	{
		final FacetSearchConfig facetSearchConfig = batchContext.getFacetSearchConfig();
		final IndexedType indexedType = batchContext.getIndexedType();

		final IdentityProvider<ItemModel> identityProvider = getIdentityProvider(indexedType);
		final String id = identityProvider.getIdentifier(facetSearchConfig.getIndexConfig(), model);

		if (LOG.isDebugEnabled())
		{
			LOG.debug("Using SolrInputDocument id [" + id + "]");
		}

		document.addField(SolrfacetsearchConstants.ID_FIELD, id);
		document.addField(SolrfacetsearchConstants.PK_FIELD, Long.valueOf(model.getPk().getLongValue()));

		final ComposedTypeModel composedType = typeService.getComposedTypeForClass(model.getClass());
		if (Objects.equals(composedType.getCatalogItemType(), Boolean.TRUE))
		{
			final AttributeDescriptorModel catalogAttDesc = composedType.getCatalogVersionAttribute();
			final CatalogVersionModel catalogVersion = modelService.getAttributeValue(model, catalogAttDesc.getQualifier());
			document.addField(SolrfacetsearchConstants.CATALOG_ID_FIELD, catalogVersion.getCatalog().getId());
			document.addField(SolrfacetsearchConstants.CATALOG_VERSION_FIELD, catalogVersion.getVersion());
		}
	}

	protected void addIndexedPropertyFields(final InputDocument document, final IndexerBatchContext batchContext,
			final ItemModel model) throws FieldValueProviderException
	{
		final Map<String, Collection<IndexedProperty>> valueProviders = resolveValueProviders(batchContext);

		for (final Entry<String, Collection<IndexedProperty>> entry : valueProviders.entrySet())
		{
			final String valueProviderId = entry.getKey();
			final Collection<IndexedProperty> indexedProperties = entry.getValue();

			final Object valueProvider = valueProviderSelectionStrategy.getValueProvider(valueProviderId);

			if (valueProvider instanceof FieldValueProvider)
			{
				addIndexedPropertyFieldsForOldApi(document, batchContext, model, indexedProperties, valueProviderId,
						(FieldValueProvider) valueProvider);
			}
			else if (valueProvider instanceof ValueResolver)
			{
				addIndexedPropertyFieldsForNewApi(document, batchContext, model, indexedProperties, valueProviderId,
						(ValueResolver<ItemModel>) valueProvider);
			}
			else
			{
				throw new FieldValueProviderException("Value provider is not of an expected type: " + valueProviderId);
			}
		}
	}

	protected void addIndexedPropertyFieldsForOldApi(final InputDocument document, final IndexerBatchContext batchContext,
			final ItemModel model, final Collection<IndexedProperty> indexedProperties, final String valueProviderId,
			final FieldValueProvider valueProvider) throws FieldValueProviderException
	{
		final FacetSearchConfig facetSearchConfig = batchContext.getFacetSearchConfig();

		for (final IndexedProperty indexedProperty : indexedProperties)
		{
			try
			{
				final Collection<FieldValue> fieldValues = valueProvider.getFieldValues(facetSearchConfig.getIndexConfig(),
						indexedProperty, model);
				for (final FieldValue fieldValue : fieldValues)
				{
					document.addField(fieldValue.getFieldName(), fieldValue.getValue());
				}
			}
			catch (FieldValueProviderException | RuntimeException e)
			{
				final String message = "Failed to resolve values for item with PK: " + model.getPk() + ", by resolver: "
						+ valueProviderId + ", for property: " + indexedProperty.getName() + ", reason: " + e.getMessage();
				handleError(facetSearchConfig.getIndexConfig(), message, e);
			}
		}
	}

	protected void addIndexedPropertyFieldsForNewApi(final InputDocument document, final IndexerBatchContext batchContext,
			final ItemModel model, final Collection<IndexedProperty> indexedProperties, final String valueProviderId,
			final ValueResolver<ItemModel> valueProvider) throws FieldValueProviderException
	{
		final FacetSearchConfig facetSearchConfig = batchContext.getFacetSearchConfig();

		try
		{
			valueProvider.resolve(document, batchContext, indexedProperties, model);
		}
		catch (FieldValueProviderException | RuntimeException e)
		{
			final ArrayList<String> indexedPropertiesNames = new ArrayList<>();
			for (final IndexedProperty indexedProperty : indexedProperties)
			{
				indexedPropertiesNames.add(indexedProperty.getName());
			}

			final String message = "Failed to resolve values for item with PK: " + model.getPk() + ", by resolver: "
					+ valueProviderId + ", for properties: " + indexedPropertiesNames + ", reason: " + e.getMessage();
			handleError(facetSearchConfig.getIndexConfig(), message, e);
		}
	}

	protected void addIndexedTypeFields(final InputDocument document, final IndexerBatchContext batchContext, final ItemModel model)
			throws FieldValueProviderException
	{
		final IndexedType indexedType = batchContext.getIndexedType();
		final String typeValueProviderBeanId = indexedType.getFieldsValuesProvider();

		if (typeValueProviderBeanId != null)
		{
			final Object typeValueProvider = getTypeValueProvider(typeValueProviderBeanId);

			if (typeValueProvider instanceof IndexedTypeFieldsValuesProvider)
			{
				addIndexedTypeFieldsForOldApi(document, batchContext, model, typeValueProviderBeanId,
						(IndexedTypeFieldsValuesProvider) typeValueProvider);
			}
			else if (typeValueProvider instanceof TypeValueResolver)
			{
				addIndexedTypeFieldsForNewApi(document, batchContext, model, typeValueProviderBeanId,
						(TypeValueResolver<ItemModel>) typeValueProvider);
			}
			else
			{
				throw new FieldValueProviderException("Type value provider is not of an expected type: " + typeValueProviderBeanId);
			}
		}
	}

	protected void addIndexedTypeFieldsForOldApi(final InputDocument document, final IndexerBatchContext batchContext,
			final ItemModel model, final String typeValueProviderBeanId, final IndexedTypeFieldsValuesProvider typeValueProvider)
			throws FieldValueProviderException
	{
		final FacetSearchConfig facetSearchConfig = batchContext.getFacetSearchConfig();

		try
		{
			final Collection<FieldValue> fieldValues = typeValueProvider.getFieldValues(facetSearchConfig.getIndexConfig(), model);

			for (final FieldValue fieldValue : fieldValues)
			{
				document.addField(fieldValue.getFieldName(), fieldValue.getValue());
			}
		}
		catch (FieldValueProviderException | RuntimeException e)
		{
			final String message = "Failed to resolve values for item with PK: " + model.getPk() + ", by resolver: "
					+ typeValueProviderBeanId + ", reason: " + e.getMessage();
			handleError(facetSearchConfig.getIndexConfig(), message, e);
		}
	}

	protected void addIndexedTypeFieldsForNewApi(final InputDocument document, final IndexerBatchContext batchContext,
			final ItemModel model, final String typeValueProviderBeanId, final TypeValueResolver<ItemModel> typeValueProvider)
			throws FieldValueProviderException
	{
		final FacetSearchConfig facetSearchConfig = batchContext.getFacetSearchConfig();

		try
		{
			typeValueProvider.resolve(document, batchContext, model);
		}
		catch (FieldValueProviderException | RuntimeException e)
		{
			final String message = "Failed to resolve values for item with PK: " + model.getPk() + ", by resolver: "
					+ typeValueProviderBeanId + ", reason: " + e.getMessage();
			handleError(facetSearchConfig.getIndexConfig(), message, e);
		}
	}

	protected Map<String, Collection<IndexedProperty>> resolveValueProviders(final IndexerBatchContext batchContext)
	{
		Map<String, Collection<IndexedProperty>> valueProviders = (Map<String, Collection<IndexedProperty>>) batchContext
				.getAttributes().get(VALUE_PROVIDERS_KEY);

		if (valueProviders == null)
		{
			valueProviders = valueProviderSelectionStrategy.resolveValueProviders(batchContext.getIndexedType(),
					batchContext.getIndexedProperties());
			batchContext.getAttributes().put(VALUE_PROVIDERS_KEY, valueProviders);
		}

		return valueProviders;
	}

	protected Set<String> getIndexedFields(final IndexerBatchContext batchContext) throws FieldValueProviderException
	{
		Set<String> indexedPropertiesFields = (Set<String>) batchContext.getAttributes().get(INDEXED_FIELDS_KEY);
		if (CollectionUtils.isNotEmpty(indexedPropertiesFields))
		{
			return indexedPropertiesFields;
		}

		indexedPropertiesFields = new HashSet<>();
		final Set<String> indexedPropertiesNames = new HashSet<>();

		for (final IndexedProperty indexedProperty : batchContext.getIndexedProperties())
		{
			indexedPropertiesNames.add(indexedProperty.getName());
		}

		SolrClient solrClient = null;
		try
		{
			final Index index = batchContext.getIndex();
			final FacetSearchConfig facetSearchConfig = batchContext.getFacetSearchConfig();
			final IndexedType indexedType = batchContext.getIndexedType();

			final SolrSearchProvider solrSearchProvider = solrSearchProviderFactory.getSearchProvider(facetSearchConfig,
					indexedType);
			solrClient = solrSearchProvider.getClientForIndexing(index);

			final Set<String> fields = getIndexedFields(index, solrClient);

			for (final String field : fields)
			{
				final String indexedPropertyName = fieldNameProvider.getPropertyName(field);
				if (indexedPropertiesNames.contains(indexedPropertyName))
				{
					indexedPropertiesFields.add(field);
				}
			}
		}
		catch (final IOException | SolrServerException | SolrServiceException e)
		{
			throw new FieldValueProviderException("Could not fetch fields from solr server", e);
		}
		finally
		{
			IOUtils.closeQuietly(solrClient);
		}

		batchContext.getAttributes().put(INDEXED_FIELDS_KEY, indexedPropertiesFields);

		return indexedPropertiesFields;
	}

	protected Set<String> getIndexedFields(final Index index, final SolrClient solrClient) throws SolrServerException, IOException
	{
		final LukeRequest request = new LukeRequest();
		request.setNumTerms(0);

		final LukeResponse response = request.process(solrClient, index.getName());
		final Map<String, FieldInfo> fields = response.getFieldInfo();

		if (fields != null)
		{
			return fields.keySet();
		}

		return Collections.<String>emptySet();
	}

	protected void handleError(final IndexConfig indexConfig, final String message, final Exception error)
			throws FieldValueProviderException
	{
		if (indexConfig.isIgnoreErrors())
		{
			LOG.warn(message);
		}
		else
		{
			throw new FieldValueProviderException(message, error);
		}
	}

	protected IdentityProvider<ItemModel> getIdentityProvider(final IndexedType indexedType)
	{
		return beanFactory.getBean(indexedType.getIdentityProvider(), IdentityProvider.class);
	}

	protected Object getTypeValueProvider(final String beanName)
	{
		return beanFactory.getBean(beanName);
	}
}
