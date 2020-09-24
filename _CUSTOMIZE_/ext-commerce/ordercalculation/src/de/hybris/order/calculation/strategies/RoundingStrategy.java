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
package de.hybris.order.calculation.strategies;

import de.hybris.order.calculation.money.Currency;
import de.hybris.order.calculation.money.Money;
import de.hybris.order.calculation.money.Percentage;

import java.math.BigDecimal;

public interface RoundingStrategy
{
	/**
	 * Divide price by factor
	 *
	 * @param money
	 *           to divide for
	 * @param factor
	 *           to divide by
	 * @return result of dividing
	 */
	Money divide(Money money, BigDecimal factor);

	/**
	 * Multiply the given money by given factor. The returned Money is the nearest result (rounded up/down) to the
	 * calculation.
	 *
	 * @param money
	 *           to multiple for
	 * @param factor
	 *           to multiple by
	 * @return result of multiplying
	 */
	Money multiply(Money money, BigDecimal factor);

	/**
	 * Returns for the given {@link Percentage} amount the calculated amount in {@link Money}. E.g. 25% of 10.01Euro
	 * result in 2.50euro
	 *
	 * @param price
	 *           the price
	 * @param percent
	 *           the percentage of the price to take.
	 * @return A Money object representing a percentage of the price.
	 */
	Money getPercentValue(Money price, Percentage percent);

	/**
	 * Creates a {@link Money} object based on the given BigDecimal amount. Based on the implementation the amount is
	 * round up/down to fit into the money.
	 *
	 * @param amount
	 *           the amount to be converted
	 * @param currency
	 *           based on the currency digits the amount is round up/down
	 * @return the neares Money object for the given amount.
	 */
	Money roundToMoney(BigDecimal amount, Currency currency);
}
