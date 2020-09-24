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
package com.hybris.backoffice.labels.impl;

import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.servicelayer.i18n.I18NService;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.labels.LabelHandler;
import com.hybris.cockpitng.core.util.Validate;


public class PriceLabelHandler implements LabelHandler<Double, CurrencyModel>
{

	private static final Logger LOG = LoggerFactory.getLogger(PriceLabelHandler.class);

	private I18NService i18NService;

	@Override
	public String getLabel(final Double price, final CurrencyModel currencyModel)
	{
		Validate.notNull("Price may not be null", price);
		Validate.notNull("Currency may not be null", currencyModel);
		final String currencyIsoCode = currencyModel.getIsocode();
		final Currency javaCurrency = getI18NService().getBestMatchingJavaCurrency(currencyIsoCode);
		if (javaCurrency == null)
		{
			LOG.warn("Could not find Java Currency for given iso code: {}", currencyIsoCode);
			return StringUtils.EMPTY;
		}
		else
		{
			return getNumberFormatter(javaCurrency, currencyModel).format(price.doubleValue());
		}
	}

	protected NumberFormat getNumberFormatter(final Currency javaCurrency, final CurrencyModel currencyModel)
	{
		final Locale currentLocale = getI18NService().getCurrentLocale();
		final NumberFormat formatter = NumberFormat.getCurrencyInstance(currentLocale);
		if (formatter instanceof DecimalFormat)
		{
			adjustDecimalFormatter(javaCurrency, currencyModel, currentLocale, (DecimalFormat) formatter);
		}
		return formatter;
	}

	protected void adjustDecimalFormatter(final Currency javaCurrency, final CurrencyModel currencyModel, final Locale currentLocale,
										  final DecimalFormat decimalFormatter)
	{
		final DecimalFormatSymbols decimalSymbols = getDecimalFormatSymbols(javaCurrency, currencyModel.getSymbol(), currentLocale);

		decimalFormatter.setDecimalFormatSymbols(decimalSymbols);

		adjustFractionPart(currencyModel.getDigits(), decimalFormatter);
	}

	protected DecimalFormatSymbols getDecimalFormatSymbols(final Currency currency, final String currencySymbol, final Locale currentLocale)
	{
		final DecimalFormatSymbols decimalSymbols = DecimalFormatSymbols.getInstance(currentLocale);
		decimalSymbols.setCurrency(currency);
		if (StringUtils.isNotBlank(currencySymbol))
		{
			decimalSymbols.setCurrencySymbol(currencySymbol);
		}
		return decimalSymbols;
	}

	protected void adjustFractionPart(final Integer digits, final DecimalFormat decimalFormat)
	{
		decimalFormat.setMaximumFractionDigits(digits);
		decimalFormat.setMinimumFractionDigits(digits);
		decimalFormat.setDecimalSeparatorAlwaysShown(digits > 0);
	}

	public I18NService getI18NService()
	{
		return i18NService;
	}

	@Required
	public void setI18NService(final I18NService i18NService)
	{
		this.i18NService = i18NService;
	}
}
