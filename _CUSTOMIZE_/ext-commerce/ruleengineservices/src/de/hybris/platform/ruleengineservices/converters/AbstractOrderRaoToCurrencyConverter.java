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
package de.hybris.platform.ruleengineservices.converters;

import de.hybris.order.calculation.money.Currency;
import de.hybris.platform.ruleengineservices.rao.AbstractOrderRAO;
import de.hybris.platform.ruleengineservices.util.CurrencyUtils;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.springframework.beans.factory.annotation.Required;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;


/**
 * Converts {@link AbstractOrderRAO} to {@link Currency}.
 */
public class AbstractOrderRaoToCurrencyConverter implements Converter<AbstractOrderRAO, Currency>
{
	private CurrencyUtils currencyUtils;

	@Override
	public Currency convert(final AbstractOrderRAO source)
	{
		// validation along the way
		final String currencyIso = source.getCurrencyIsoCode();
		validateParameterNotNull(currencyIso, "currencyIso must not be null");
		return new Currency(currencyIso, getCurrencyUtils().getDigitsOfCurrencyOrDefault(currencyIso));
	}

	@Override
	public Currency convert(final AbstractOrderRAO paramSOURCE, final Currency paramTARGET)
	{
		throw new UnsupportedOperationException();
	}

	protected CurrencyUtils getCurrencyUtils()
	{
		return currencyUtils;
	}

	@Required
	public void setCurrencyUtils(final CurrencyUtils currencyUtils)
	{
		this.currencyUtils = currencyUtils;
	}
}
