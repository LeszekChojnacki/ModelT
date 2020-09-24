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

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DefaultTaxRoundingStrategy extends DefaultRoundingStrategy
{

	@Override
	protected Money createMoney(final BigDecimal amount, final Currency curr)
	{
		return new Money(amount.setScale(curr.getDigits(), RoundingMode.DOWN), curr);
	}
}
