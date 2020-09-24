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
package de.hybris.platform.promotionengineservices.promotionengine.impl;

import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.promotionengineservices.promotionengine.PromotionMessageParameterResolutionStrategy;
import de.hybris.platform.promotionengineservices.util.PromotionResultUtils;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.ruleengineservices.rule.data.RuleParameterData;
import de.hybris.platform.servicelayer.util.ServicesUtil;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.google.common.base.Preconditions;


/**
 * DefaultCurrencyAmountResolutionStrategy resolves the given {@link RuleParameterData#getValue()} attribute into a
 * formatted currency amount, for example $200.00. The value attribute is cast into a {@code Map<String,BigDecimal>} and
 * the given {@link PromotionResultModel#getOrder()}'s currency isocode is used as key to lookup the amount to be
 * formatted.
 */
public class DefaultCurrencyAmountResolutionStrategy implements PromotionMessageParameterResolutionStrategy
{

	private static final Logger LOG = LoggerFactory.getLogger(DefaultCurrencyAmountResolutionStrategy.class);
	private PromotionResultUtils promotionResultUtils;
	
	/**
	 * @throws IllegalArgumentException
	 *            if any of the given parameters is null
	 * @throws ClassCastException
	 *            if the given data.getValue() doesn't contain a Map<String,BigDecimal>
	 */
	@Override
	public String getValue(final RuleParameterData data, final PromotionResultModel promotionResult, final Locale locale)
	{
		ServicesUtil.validateParameterNotNull(data, "parameter data must not be null");
		ServicesUtil.validateParameterNotNull(promotionResult, "parameter promotionResult must not be null");
		ServicesUtil.validateParameterNotNull(locale, "parameter locale must not be null");

		final Map<String, BigDecimal> values = data.getValue();
		final AbstractOrderModel order = getPromotionResultUtils().getOrder(promotionResult);
		if (order != null && order.getCurrency() != null)
		{
			final String isoCode = order.getCurrency().getIsocode();
			final BigDecimal amount = values.get(isoCode);
			if (amount != null)
			{
				return formatCurrencyAmount(locale, order.getCurrency(), amount);
			}
		}
		return null;
	}

	@Override
	public RuleParameterData getReplacedParameter(final RuleParameterData paramToReplace,
			final PromotionResultModel promotionResult, final Object actualValueAsObject)
	{
		ServicesUtil.validateParameterNotNull(paramToReplace, "parameter paramToReplace must not be null");
		ServicesUtil.validateParameterNotNull(promotionResult, "parameter promotionResult must not be null");
		final AbstractOrderModel order = getPromotionResultUtils().getOrder(promotionResult);
		ServicesUtil.validateParameterNotNull(order, "parameter promotionResult.order must not be null");
		ServicesUtil.validateParameterNotNull(order.getCurrency(),
				"parameter promotionResult.order.currency must not be null");
		ServicesUtil.validateParameterNotNull(order.getCurrency().getIsocode(),
				"parameter promotionResult.order.currency.isocode must not be null");
		Preconditions.checkArgument(actualValueAsObject instanceof BigDecimal, "Actual value must be of BigDecimal type");
		Preconditions.checkArgument(paramToReplace.getValue() instanceof Map,
				"parameter paramToReplace must by of type java.util.Map");

		final Map<String, BigDecimal> values = new HashMap<String, BigDecimal>((Map<String, BigDecimal>) paramToReplace.getValue());
		final String isoCode = order.getCurrency().getIsocode();
		values.put(isoCode, (BigDecimal) actualValueAsObject);
		final RuleParameterData result = new RuleParameterData();
		result.setType(paramToReplace.getType());
		result.setUuid(paramToReplace.getUuid());
		result.setValue(values);
		return result;
	}

	/**
	 * Format an amount in a currency for a locale.
	 *
	 * @param locale
	 *           the java locale that the amount should be rendered in
	 * @param currency
	 *           the hybris currency model for the amount
	 * @param amount
	 *           the value
	 * @return a formatted string
	 */
	protected String formatCurrencyAmount(final Locale locale, final CurrencyModel currency, final BigDecimal amount)
	{
		ServicesUtil.validateParameterNotNull(locale, "locale must not be null");
		ServicesUtil.validateParameterNotNull(currency, "currency must not be null");

		// Lookup the number formatter for the locale
		final NumberFormat localisedNumberFormat = NumberFormat.getCurrencyInstance(locale);

		// Lookup the java currency object for the currency code (must be ISO 4217)
		final String currencyIsoCode = currency.getIsocode();
		final java.util.Currency javaCurrency = java.util.Currency.getInstance(currencyIsoCode);
		if (javaCurrency == null)
		{
			LOG.warn("formatCurrencyAmount failed to lookup java.util.Currency from [{}] ensure this is an ISO 4217 code and is supported by the java runtime.",
					currencyIsoCode);
		}
		else
		{
			localisedNumberFormat.setCurrency(javaCurrency);
		}

		adjustDigits((DecimalFormat) localisedNumberFormat, currency);
		adjustSymbol((DecimalFormat) localisedNumberFormat, currency);

		// Format the amount
		final String result = localisedNumberFormat.format(amount);

		if (LOG.isDebugEnabled())
		{
			LOG.debug("formatCurrencyAmount locale=[" + locale + "] currency=[" + currency + "] amount=[" + amount
					+ "] currencyIsoCode=[" + currencyIsoCode + "] javaCurrency=[" + javaCurrency + "] result=[" + result + "]");
		}

		return result;

	}

	/**
	 * Adjusts {@link java.text.DecimalFormat}'s fraction digits according to given {@link CurrencyModel}.
	 */
	protected DecimalFormat adjustDigits(final DecimalFormat format, final CurrencyModel currency)
	{
		final int tempDigits = currency.getDigits() == null ? 0 : currency.getDigits().intValue();
		final int digits = Math.max(0, tempDigits);

		format.setMaximumFractionDigits(digits);
		format.setMinimumFractionDigits(digits);
		if (digits == 0)
		{
			format.setDecimalSeparatorAlwaysShown(false);
		}

		return format;
	}

	/**
	 * Adjusts {@link DecimalFormat}'s symbol according to given {@link CurrencyModel}.
	 */
	protected static DecimalFormat adjustSymbol(final DecimalFormat format, final CurrencyModel currency)
	{
		final String symbol = currency.getSymbol();
		if (symbol != null)
		{
			final DecimalFormatSymbols symbols = format.getDecimalFormatSymbols(); // does cloning
			final String iso = currency.getIsocode();
			boolean changed = false;
			if (!iso.equalsIgnoreCase(symbols.getInternationalCurrencySymbol()))
			{
				symbols.setInternationalCurrencySymbol(iso);
				changed = true;
			}
			if (!symbol.equals(symbols.getCurrencySymbol()))
			{
				symbols.setCurrencySymbol(symbol);
				changed = true;
			}
			if (changed)
			{
				format.setDecimalFormatSymbols(symbols);
			}
		}
		return format;
	}

	protected PromotionResultUtils getPromotionResultUtils()
	{
		return promotionResultUtils;
	}

	@Required
	public void setPromotionResultUtils(final PromotionResultUtils promotionResultUtils)
	{
		this.promotionResultUtils = promotionResultUtils;
	}
}
