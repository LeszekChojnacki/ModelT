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
package de.hybris.platform.payment.commands.result;

import de.hybris.platform.payment.commands.VoidCommand;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;


/**
 * result of {@link VoidCommand}
 */
public class VoidResult extends AbstractResult
{
	private Currency currency;
	private BigDecimal amount;
	private Date requestTime;

	/**
	 * @return the currency
	 */
	public Currency getCurrency()
	{
		return currency;
	}

	/**
	 * @param currency
	 *           the currency to set
	 */
	public void setCurrency(final Currency currency)
	{
		this.currency = currency;
	}

	/**
	 * @return the amount
	 */
	public BigDecimal getAmount()
	{
		return amount;
	}

	/**
	 * @param amount
	 *           the amount to set
	 */
	public void setAmount(final BigDecimal amount)
	{
		this.amount = amount;
	}

	/**
	 * @return the requestTime
	 */
	public Date getRequestTime()
	{
		return requestTime;
	}

	/**
	 * @param requestTime
	 *           the requestTime to set
	 */
	public void setRequestTime(final Date requestTime)
	{
		this.requestTime = requestTime;
	}
}
