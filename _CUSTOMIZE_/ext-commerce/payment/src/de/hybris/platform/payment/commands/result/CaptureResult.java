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

import de.hybris.platform.payment.commands.CaptureCommand;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;


/**
 * Result for {@link CaptureCommand}
 */
public class CaptureResult extends AbstractResult
{
	private Currency currency;
	private BigDecimal totalAmount;
	private Date requestTime;

	/**
	 * @return the totalAmount
	 */
	public BigDecimal getTotalAmount()
	{
		return totalAmount;
	}

	/**
	 * @param totalAmount
	 *           the totalAmount to set
	 */
	public void setTotalAmount(final BigDecimal totalAmount)
	{
		this.totalAmount = totalAmount;
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
}
