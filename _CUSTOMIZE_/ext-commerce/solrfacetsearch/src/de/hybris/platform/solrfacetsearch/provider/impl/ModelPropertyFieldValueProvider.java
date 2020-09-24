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
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.product.VariantsService;
import de.hybris.platform.servicelayer.exceptions.AttributeNotSupportedException;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.provider.FieldValue;
import de.hybris.platform.solrfacetsearch.provider.FieldValueProvider;
import de.hybris.platform.variants.model.VariantAttributeDescriptorModel;
import de.hybris.platform.variants.model.VariantProductModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


public class ModelPropertyFieldValueProvider extends AbstractPropertyFieldValueProvider implements FieldValueProvider
{
	private FieldNameProvider fieldNameProvider;
	private VariantsService variantsService;

	@Override
	public Collection<FieldValue> getFieldValues(final IndexConfig indexConfig, final IndexedProperty indexedProperty,
			final Object model) throws FieldValueProviderException
	{
		if (model == null)
		{
			throw new IllegalArgumentException("No model given");
		}

		final Collection<FieldValue> fieldValues = new ArrayList<FieldValue>();

		if (indexedProperty.isLocalized() && CollectionUtils.isNotEmpty(indexConfig.getLanguages()))
		{
			final Locale sessionLocale = i18nService.getCurrentLocale();

			try
			{
				for (final LanguageModel language : indexConfig.getLanguages())
				{
					i18nService.setCurrentLocale(localeService.getLocaleByString(language.getIsocode()));
					addFieldValues(fieldValues, (ItemModel) model, indexedProperty, language.getIsocode());
				}
			}
			finally
			{
				i18nService.setCurrentLocale(sessionLocale);
			}
		}
		else
		{
			addFieldValues(fieldValues, (ItemModel) model, indexedProperty, null);
		}

		return fieldValues;
	}

	protected void addFieldValues(final Collection<FieldValue> fieldValues, final ItemModel model,
			final IndexedProperty indexedProperty, final String language) throws FieldValueProviderException
	{
		final Object value = getPropertyValue(model, indexedProperty);

		final List<String> rangeNameList = getRangeNameList(indexedProperty, value, language);
		final Collection<String> fieldNames = fieldNameProvider.getFieldNames(indexedProperty,
				language == null ? null : language.toLowerCase(Locale.ROOT));

		for (final String fieldName : fieldNames)
		{
			if (rangeNameList.isEmpty())
			{
				fieldValues.add(new FieldValue(fieldName, value));

			}
			else
			{
				for (final String rangeName : rangeNameList)
				{
					fieldValues.add(new FieldValue(fieldName, rangeName == null ? value : rangeName));
				}
			}
		}
	}

	protected Object getPropertyValue(final Object model, final IndexedProperty indexedProperty)
	{
		String qualifier = indexedProperty.getValueProviderParameter();

		if (qualifier == null || qualifier.trim().isEmpty())
		{
			qualifier = indexedProperty.getName();
		}

		Object result = null;
		try
		{
			result = modelService.getAttributeValue(model, qualifier);
			if ((result == null) && (model instanceof VariantProductModel))
			{
				final ProductModel baseProduct = ((VariantProductModel) model).getBaseProduct();
				result = modelService.getAttributeValue(baseProduct, qualifier);
			}
		}
		catch (final AttributeNotSupportedException ae)
		{
			if (model instanceof VariantProductModel)
			{
				final ProductModel baseProduct = ((VariantProductModel) model).getBaseProduct();
				for (final VariantAttributeDescriptorModel att : baseProduct.getVariantType().getVariantAttributes())
				{
					if (qualifier.equals(att.getQualifier()))
					{
						result = this.variantsService.getVariantAttributeValue((VariantProductModel) model, qualifier);
						break;
					}
				}
			}
			else
			{
				LOG.error(ae.getMessage());
			}
		}
		return result;
	}

	@Required
	public void setFieldNameProvider(final FieldNameProvider fieldNameProvider)
	{
		this.fieldNameProvider = fieldNameProvider;
	}

	@Required
	public void setVariantsService(final VariantsService variantsService)
	{
		this.variantsService = variantsService;
	}
}
