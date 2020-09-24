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
package de.hybris.platform.solrfacetsearch.search.impl;

import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider.FieldType;
import de.hybris.platform.solrfacetsearch.provider.IndexedTypeFieldsValuesProvider;
import de.hybris.platform.solrfacetsearch.provider.Qualifier;
import de.hybris.platform.solrfacetsearch.provider.QualifierProvider;
import de.hybris.platform.solrfacetsearch.provider.QualifierProviderAware;
import de.hybris.platform.solrfacetsearch.provider.ValueProviderSelectionStrategy;
import de.hybris.platform.solrfacetsearch.search.FieldNameTranslator;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;
import de.hybris.platform.solrfacetsearch.search.context.FacetSearchContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link FieldNameTranslator}.
 */
public class DefaultFieldNameTranslator implements FieldNameTranslator, BeanFactoryAware
{
	public static final String FIELD_INFOS_MAPPING_KEY = "solrfacetsearch.fieldInfos";

	private FieldNameProvider fieldNameProvider;
	private ValueProviderSelectionStrategy valueProviderSelectionStrategy;
	private BeanFactory beanFactory;

	public FieldNameProvider getFieldNameProvider()
	{
		return fieldNameProvider;
	}

	@Required
	public void setFieldNameProvider(final FieldNameProvider fieldNameProvider)
	{
		this.fieldNameProvider = fieldNameProvider;
	}

	public ValueProviderSelectionStrategy getValueProviderSelectionStrategy()
	{
		return valueProviderSelectionStrategy;
	}

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
	public String translate(final SearchQuery searchQuery, final String field, final FieldType fieldType)
	{
		final IndexedType indexedType = searchQuery.getIndexedType();
		final IndexedProperty indexedProperty = indexedType.getIndexedProperties().get(field);

		String translatedField = null;

		if (indexedProperty != null)
		{
			translatedField = translateFromProperty(searchQuery, indexedProperty, fieldType);
		}
		else
		{
			translatedField = translateFromType(searchQuery, field);
		}

		if (translatedField == null)
		{
			translatedField = field;
		}

		return translatedField;
	}

	@Override
	public String translate(final FacetSearchContext searchContext, final String field, final FieldType fieldType)
	{
		return translate(searchContext.getSearchQuery(), field, fieldType);
	}

	@Override
	public String translate(final FacetSearchContext searchContext, final String field)
	{
		final FieldInfosMapping fieldInfosMapping = getFieldInfos(searchContext);
		final FieldInfo fieldInfo = fieldInfosMapping.getFieldInfos().get(field);

		String translatedField;

		if (fieldInfo != null)
		{
			translatedField = fieldInfo.getTranslatedFieldName();
		}
		else
		{
			translatedField = field;
		}

		return translatedField;
	}

	@Override
	public FieldInfosMapping getFieldInfos(final FacetSearchContext searchContext)
	{
		DefaultFieldInfosMapping fieldInfosMapping = (DefaultFieldInfosMapping) searchContext.getAttributes()
				.get(FIELD_INFOS_MAPPING_KEY);

		if (fieldInfosMapping == null)
		{
			final Map<String, FieldInfo> fieldInfos = new HashMap<>();
			final Map<String, FieldInfo> invertedFieldInfos = new HashMap<>();

			final SearchQuery searchQuery = searchContext.getSearchQuery();
			final IndexedType indexedType = searchContext.getIndexedType();

			// populate from the indexed properties
			for (final IndexedProperty indexedProperty : indexedType.getIndexedProperties().values())
			{
				final String fieldName = indexedProperty.getName();
				final String translatedFieldName = translateFromProperty(searchQuery, indexedProperty, FieldType.INDEX);

				final DefaultFieldInfo fieldInfo = new DefaultFieldInfo();
				fieldInfo.setFieldName(fieldName);
				fieldInfo.setTranslatedFieldName(translatedFieldName);
				fieldInfo.setIndexedProperty(indexedProperty);

				fieldInfos.put(fieldName, fieldInfo);
				invertedFieldInfos.put(translatedFieldName, fieldInfo);
			}

			// populate from the type value provider
			final Object typeValueProvider = getTypeValueProvider(indexedType);
			if (typeValueProvider instanceof IndexedTypeFieldsValuesProvider)
			{
				final Map<String, String> fieldNamesMapping = ((IndexedTypeFieldsValuesProvider) typeValueProvider)
						.getFieldNamesMapping();

				for (final Entry<String, String> entry : fieldNamesMapping.entrySet())
				{
					final String fieldName = entry.getKey();
					final String translatedFieldName = entry.getValue();

					final DefaultFieldInfo fieldInfo = new DefaultFieldInfo();
					fieldInfo.setFieldName(fieldName);
					fieldInfo.setTranslatedFieldName(translatedFieldName);

					fieldInfos.put(fieldName, fieldInfo);
					invertedFieldInfos.put(translatedFieldName, fieldInfo);
				}
			}

			fieldInfosMapping = new DefaultFieldInfosMapping();
			fieldInfosMapping.setFieldInfos(fieldInfos);
			fieldInfosMapping.setInvertedFieldInfos(invertedFieldInfos);

			searchContext.getAttributes().put(FIELD_INFOS_MAPPING_KEY, fieldInfosMapping);
		}

		return fieldInfosMapping;
	}

	protected String translateFromProperty(final SearchQuery searchQuery, final IndexedProperty indexedProperty,
			final FieldType fieldType)
	{
		final IndexedType indexedType = searchQuery.getIndexedType();

		String fieldQualifier = null;

		final String valueProviderId = valueProviderSelectionStrategy.resolveValueProvider(indexedType, indexedProperty);
		final Object valueProvider = valueProviderSelectionStrategy.getValueProvider(valueProviderId);

		final QualifierProvider qualifierProvider = (valueProvider instanceof QualifierProviderAware)
				? ((QualifierProviderAware) valueProvider).getQualifierProvider() : null;
		if ((qualifierProvider != null) && (qualifierProvider.canApply(indexedProperty)))
		{
			final Qualifier qualifier = qualifierProvider.getCurrentQualifier();
			fieldQualifier = (qualifier != null) ? qualifier.toFieldQualifier() : null;
		}
		else if (indexedProperty.isLocalized())
		{
			fieldQualifier = searchQuery.getLanguage();
		}
		else if (indexedProperty.isCurrency())
		{
			fieldQualifier = searchQuery.getCurrency();
		}

		return fieldNameProvider.getFieldName(indexedProperty, fieldQualifier, fieldType);
	}

	protected String translateFromType(final SearchQuery searchQuery, final String field)
	{
		final IndexedType indexedType = searchQuery.getIndexedType();
		final Object typeValueProvider = getTypeValueProvider(indexedType);

		if (typeValueProvider instanceof IndexedTypeFieldsValuesProvider)
		{
			final Map<String, String> fieldNamesMapping = ((IndexedTypeFieldsValuesProvider) typeValueProvider)
					.getFieldNamesMapping();
			return fieldNamesMapping.get(field);
		}

		return null;
	}

	protected Object getTypeValueProvider(final IndexedType indexedType)
	{
		final String fieldsValuesProvider = indexedType.getFieldsValuesProvider();

		if (fieldsValuesProvider != null)
		{
			return beanFactory.getBean(fieldsValuesProvider);
		}

		return null;
	}

	protected static class DefaultFieldInfosMapping implements FieldInfosMapping
	{
		private Map<String, FieldInfo> fieldInfos;
		private Map<String, FieldInfo> invertedFieldInfos;

		@Override
		public Map<String, FieldInfo> getFieldInfos()
		{
			return fieldInfos;
		}

		public void setFieldInfos(final Map<String, FieldInfo> fieldInfos)
		{
			this.fieldInfos = fieldInfos;
		}

		@Override
		public Map<String, FieldInfo> getInvertedFieldInfos()
		{
			return invertedFieldInfos;
		}

		public void setInvertedFieldInfos(final Map<String, FieldInfo> invertedFieldInfos)
		{
			this.invertedFieldInfos = invertedFieldInfos;
		}
	}

	protected static class DefaultFieldInfo implements FieldInfo
	{
		private String fieldName;
		private String translatedFieldName;
		private IndexedProperty indexedProperty;

		@Override
		public String getFieldName()
		{
			return fieldName;
		}

		public void setFieldName(final String fieldName)
		{
			this.fieldName = fieldName;
		}

		@Override
		public String getTranslatedFieldName()
		{
			return translatedFieldName;
		}

		public void setTranslatedFieldName(final String translatedFieldName)
		{
			this.translatedFieldName = translatedFieldName;
		}

		@Override
		public IndexedProperty getIndexedProperty()
		{
			return indexedProperty;
		}

		public void setIndexedProperty(final IndexedProperty indexedProperty)
		{
			this.indexedProperty = indexedProperty;
		}
	}
}
