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


import de.hybris.platform.catalog.jalo.classification.ClassificationAttributeValue;
import de.hybris.platform.catalog.jalo.classification.util.Feature;
import de.hybris.platform.catalog.jalo.classification.util.FeatureContainer;
import de.hybris.platform.catalog.jalo.classification.util.FeatureValue;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.jalo.product.Product;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.provider.FieldValue;
import de.hybris.platform.solrfacetsearch.provider.FieldValueProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


/**
 * {@link FieldValueProvider} for classification system attributes
 */
public class ClassificationPropertyValueProvider extends AbstractPropertyFieldValueProvider implements FieldValueProvider
{

	private FieldNameProvider fieldNameProvider;


	@Override
	public Collection<FieldValue> getFieldValues(final IndexConfig indexConfig, final IndexedProperty indexedProperty,
			final Object model) throws FieldValueProviderException
	{
		if (model instanceof ProductModel)
		{
			final FeatureContainer cont = FeatureContainer.load((Product) modelService.getSource((ProductModel) model));
			if (cont.hasFeature(indexedProperty.getName()))
			{
				final Feature feature = cont.getFeature(indexedProperty.getName());
				if (feature == null || feature.isEmpty())
				{
					return Collections.emptyList();
				}
				else
				{
					return getFeaturesValues(indexConfig, feature, indexedProperty);
				}
			}
			else
			{
				return Collections.emptyList();
			}
		}
		else
		{
			throw new FieldValueProviderException("Cannot provide classification property of non-product item");
		}
	}

	protected List<FieldValue> getFeaturesValues(final IndexConfig indexConfig, final Feature feature,
			final IndexedProperty indexedProperty) throws FieldValueProviderException
	{
		final List<FieldValue> result = new ArrayList<FieldValue>();
		List<FeatureValue> featureValues = null;
		if (!feature.isLocalized())
		{
			featureValues = feature.getValues();
		}
		if (indexedProperty.isLocalized())
		{
			for (final LanguageModel language : indexConfig.getLanguages())
			{
				final Locale locale = i18nService.getCurrentLocale();
				try
				{
					i18nService.setCurrentLocale(localeService.getLocaleByString(language.getIsocode()));
					result.addAll(
							extractFieldValues(indexedProperty, language, feature.isLocalized() ? feature.getValues() : featureValues));
				}
				finally
				{
					i18nService.setCurrentLocale(locale);
				}
			}
		}
		else
		{
			result.addAll(extractFieldValues(indexedProperty, null, feature.getValues()));
		}
		return result;
	}

	protected List<FieldValue> extractFieldValues(final IndexedProperty indexedProperty, final LanguageModel language,
			final List<FeatureValue> list) throws FieldValueProviderException
	{
		final List<FieldValue> result = new ArrayList<FieldValue>();

		for (final FeatureValue featureValue : list)
		{
			addFeatureValue(result, indexedProperty, language, featureValue);
		}

		return result;
	}

	protected void addFeatureValue(final List<FieldValue> result, final IndexedProperty indexedProperty,
			final LanguageModel language, final FeatureValue featureValue) throws FieldValueProviderException
	{
		Object value = featureValue.getValue();

		if (value instanceof ClassificationAttributeValue)
		{
			value = ((ClassificationAttributeValue) value).getName();
		}

		final List<String> rangeNameList = getRangeNameList(indexedProperty, value);
		final Collection<String> fieldNames = fieldNameProvider.getFieldNames(indexedProperty,
				language == null ? null : language.getIsocode());
		for (final String fieldName : fieldNames)
		{
			if (rangeNameList.isEmpty())
			{
				result.add(new FieldValue(fieldName, value));
			}
			else
			{
				for (final String rangeName : rangeNameList)
				{
					result.add(new FieldValue(fieldName, rangeName == null ? value : rangeName));
				}
			}
		}
	}

	/**
	 * @param fieldNameProvider
	 *           the fieldNameProvider to set
	 */
	public void setFieldNameProvider(final FieldNameProvider fieldNameProvider)
	{
		this.fieldNameProvider = fieldNameProvider;
	}

	public FieldNameProvider getFieldNameProvider()
	{
		return this.fieldNameProvider;
	}

}
