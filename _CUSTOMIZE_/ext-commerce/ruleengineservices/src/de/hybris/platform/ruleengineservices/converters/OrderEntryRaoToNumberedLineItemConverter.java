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
import de.hybris.order.calculation.money.Money;
import de.hybris.platform.ruleengineservices.calculation.NumberedLineItem;
import de.hybris.platform.ruleengineservices.rao.AbstractOrderRAO;
import de.hybris.platform.ruleengineservices.rao.OrderEntryRAO;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.springframework.beans.factory.annotation.Required;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;


/**
 * Converts {@link OrderEntryRAO} to {@link NumberedLineItem}.
 */
public class OrderEntryRaoToNumberedLineItemConverter implements Converter<OrderEntryRAO, NumberedLineItem>
{
	private Converter<AbstractOrderRAO, Currency> abstractOrderRaoToCurrencyConverter;

	@Override
	public NumberedLineItem convert(final OrderEntryRAO entryRao) 
	{
		validateParameterNotNull(entryRao, "order entry rao must not be null");
		validateParameterNotNull(entryRao.getOrder(), "corresponding entry cart rao must not be null");
		final AbstractOrderRAO rao = entryRao.getOrder();
		final Money money = new Money(entryRao.getPrice(), getAbstractOrderRaoToCurrencyConverter().convert(rao));
		final NumberedLineItem lineItem = new NumberedLineItem(money, entryRao.getQuantity());
		lineItem.setEntryNumber(entryRao.getEntryNumber());
		return lineItem;
	}

	@Override
	public NumberedLineItem convert(final OrderEntryRAO paramSOURCE, final NumberedLineItem paramTARGET)
			
	{
		throw new UnsupportedOperationException();
	}

	protected Converter<AbstractOrderRAO, Currency> getAbstractOrderRaoToCurrencyConverter()
	{
		return abstractOrderRaoToCurrencyConverter;
	}

	@Required
	public void setAbstractOrderRaoToCurrencyConverter(
			final Converter<AbstractOrderRAO, Currency> abstractOrderRaoToCurrencyConverter)
	{
		this.abstractOrderRaoToCurrencyConverter = abstractOrderRaoToCurrencyConverter;
	}
}
