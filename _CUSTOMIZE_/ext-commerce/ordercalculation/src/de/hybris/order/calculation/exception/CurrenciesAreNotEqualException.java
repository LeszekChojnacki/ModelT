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
package de.hybris.order.calculation.exception;

import de.hybris.order.calculation.money.Money;
import de.hybris.platform.jalo.c2l.Currency;


/**
 * Thrown when operating on two {@link Money}s with different {@link Currency}.
 *
 */
public class CurrenciesAreNotEqualException extends RuntimeException
{
	public CurrenciesAreNotEqualException(final String message)
	{
		super(message);
	}

}
