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

import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.jalo.order.price.PriceInformation;
import de.hybris.platform.product.PriceService;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.provider.FieldValue;
import de.hybris.platform.solrfacetsearch.provider.FieldValueProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections4.CollectionUtils;


/**
 * {@link FieldValueProvider} for prices. Supports multi-currencies
 */
public class ProductPriceValueProvider extends AbstractPropertyFieldValueProvider implements FieldValueProvider
{
	private FieldNameProvider fieldNameProvider;
	private PriceService priceService;

	@Override
	public Collection<FieldValue> getFieldValues(final IndexConfig indexConfig, final IndexedProperty indexedProperty,
			final Object model) throws FieldValueProviderException
	{
		if (!(model instanceof ProductModel))
		{
			throw new FieldValueProviderException("Cannot evaluate price of non-product item");
		}

		final Collection<FieldValue> fieldValues = new ArrayList<FieldValue>();
		final ProductModel product = (ProductModel) model;

		if (indexedProperty.isCurrency() && CollectionUtils.isNotEmpty(indexConfig.getCurrencies()))
		{
			final CurrencyModel sessionCurrency = i18nService.getCurrentCurrency();

			try
			{
				for (final CurrencyModel currency : indexConfig.getCurrencies())
				{
					i18nService.setCurrentCurrency(currency);
					addFieldValues(fieldValues, product, indexedProperty, currency.getIsocode());
				}
			}
			finally
			{
				i18nService.setCurrentCurrency(sessionCurrency);
			}
		}
		else
		{
			addFieldValues(fieldValues, product, indexedProperty, null);
		}

		return fieldValues;
	}

	protected void addFieldValues(final Collection<FieldValue> fieldValues, final ProductModel product,
			final IndexedProperty indexedProperty, final String currency) throws FieldValueProviderException
	{
		final List<PriceInformation> prices = priceService.getPriceInformationsForProduct(product);

		if (CollectionUtils.isEmpty(prices))
		{
			return;
		}

		final Double value = Double.valueOf(prices.get(0).getPriceValue().getValue());
		final List<String> rangeNameList = getRangeNameList(indexedProperty, value, currency);
		final Collection<String> fieldNames = fieldNameProvider.getFieldNames(indexedProperty,
				currency == null ? null : currency.toLowerCase(Locale.ROOT));

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

	public void setFieldNameProvider(final FieldNameProvider fieldNameProvider)
	{
		this.fieldNameProvider = fieldNameProvider;
	}

	public void setPriceService(final PriceService priceService)
	{
		this.priceService = priceService;
	}
}
