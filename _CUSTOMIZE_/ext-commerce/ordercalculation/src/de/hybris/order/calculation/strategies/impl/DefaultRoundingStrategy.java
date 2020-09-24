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
package de.hybris.order.calculation.strategies.impl;

import de.hybris.order.calculation.money.Currency;
import de.hybris.order.calculation.money.Money;
import de.hybris.order.calculation.money.Percentage;
import de.hybris.order.calculation.strategies.RoundingStrategy;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class DefaultRoundingStrategy implements RoundingStrategy
{
	@Override
	public Money divide(final Money price, final BigDecimal divisor)
	{
		final Currency curr = price.getCurrency();
		final BigDecimal divide = price.getAmount().divide(divisor, RoundingMode.HALF_UP);
		return createMoney(divide, curr);
	}


	@Override
	public Money multiply(final Money price, final BigDecimal multiplicant)
	{
		final Currency curr = price.getCurrency();
		final BigDecimal multiply = price.getAmount().multiply(multiplicant);
		return createMoney(multiply, curr);
	}

	@Override
	public Money getPercentValue(final Money price, final Percentage percent)
	{
		final Currency curr = price.getCurrency();
		final BigDecimal amount = price.getAmount().multiply(percent.getRate()).divide(BigDecimal.valueOf(100));
		return createMoney(amount, curr);
	}

	@Override
	public Money roundToMoney(final BigDecimal amount, final Currency currency)
	{
		return createMoney(amount, currency);
	}

	protected Money createMoney(final BigDecimal amount, final Currency curr)
	{
		return new Money(amount.setScale(curr.getDigits(), RoundingMode.HALF_UP), curr);
	}
}
