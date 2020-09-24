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
package de.hybris.platform.ruleengineservices.util;

import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.ruleengineservices.rule.evaluation.RuleEvaluationException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import org.springframework.beans.factory.annotation.Required;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang.StringUtils.isEmpty;

/**
 * The class provides some utility methods related to Currency functionality.
 */
public class CurrencyUtils
{
	private static final int DEFAULT_CURRENCY_DIGITS = 2;

	private CommonI18NService commonI18NService;

	/**
	 * Applies the scaling and rounding to a given price, based on declared currency code
	 *
	 * @param price
	 *           - the value to apply the rounding to
	 * @param currencyIsoCode
	 *           - currency code
	 * @return the rounded price
	 */
	public BigDecimal applyRounding(final BigDecimal price, final String currencyIsoCode)
	{
		BigDecimal roundedPrice = null;

		if (nonNull(price))
		{
			final Optional<Integer> scale = getDigitsOfCurrency(currencyIsoCode);
			roundedPrice = scale.map(d -> price.setScale(d, RoundingMode.HALF_EVEN)).orElse(price);
		}

		return roundedPrice;
	}

	/**
	 * Given the source and target currency codes, converts the value from one currency to another
	 *
	 * @param sourceCurrencyIsoCode
	 *           - source currency code
	 * @param targetCurrencyIsoCode
	 *           - target currency code
	 * @param sourceValue
	 *           - the source value to apply the conversion for
	 * @return the converted value
	 */
	public BigDecimal convertCurrency(final String sourceCurrencyIsoCode, final String targetCurrencyIsoCode,
			final BigDecimal sourceValue)
	{
		final CurrencyModel targetCurrency = getCurrency(targetCurrencyIsoCode);
		final double targetConversionRate = targetCurrency.getConversion().doubleValue();
		final CurrencyModel sourceCurrency = getCurrency(sourceCurrencyIsoCode);
		final double sourceConversionRate = sourceCurrency.getConversion().doubleValue();

		final double convertedCurrencyValue;

		final int targetCurrencyDigits = ofNullable(targetCurrency.getDigits()).orElse(0);
		convertedCurrencyValue = getCommonI18NService().convertAndRoundCurrency(sourceConversionRate, targetConversionRate,
				targetCurrencyDigits, sourceValue.doubleValue());

		return BigDecimal.valueOf(convertedCurrencyValue).setScale(targetCurrencyDigits, RoundingMode.HALF_EVEN);
	}

	/**
	 * Provides number of digits defined for the currency that identifies itself by the provided currency iso code
	 *
	 * @param currencyIsoCode currency ISO code
	 * @return number of digits for the currency identified by the provided currency code
	 */
	public Optional<Integer> getDigitsOfCurrency(final String currencyIsoCode)
	{
		Optional<Integer> digits = Optional.empty();

		if (nonNull(currencyIsoCode))
		{
			final CurrencyModel currency = getCurrency(currencyIsoCode);
			digits = ofNullable(currency.getDigits());
		}

		return digits;
	}

	/**
	 * Provides number of digits defined for the currency that identifies itself by the provided currency iso code
	 *
	 * @param currencyIsoCode currency ISO code
	 * @return number of digits for the currency identified by the provided currency code or default value
	 */
	public Integer getDigitsOfCurrencyOrDefault(final String currencyIsoCode)
	{
		return getDigitsOfCurrency(currencyIsoCode).orElse(DEFAULT_CURRENCY_DIGITS);
	}

	/**
	 * Provides an instance of {@link CurrencyModel} for a given currency code
	 *
	 * @param currencyCode currency code
	 * @return instance of {@link CurrencyModel} that matches provided currency code
	 * @throws {@link RuleEvaluationException} in case provided currency code is empty or there is no currency with provided currency code
	 */
	public CurrencyModel getCurrency(final String currencyCode)
	{
		if (isEmpty(currencyCode))
		{
			throw new RuleEvaluationException("Currency code is empty");
		}

		final CurrencyModel currency;

		try
		{
			currency = getCommonI18NService().getCurrency(currencyCode);
		}
		catch (final UnknownIdentifierException e)
		{
			throw new RuleEvaluationException("No currency found with the code: " + currencyCode, e);
		}

		return currency;
	}

	protected CommonI18NService getCommonI18NService()
	{
		return commonI18NService;
	}

	@Required
	public void setCommonI18NService(final CommonI18NService commonI18NService)
	{
		this.commonI18NService = commonI18NService;
	}
}
