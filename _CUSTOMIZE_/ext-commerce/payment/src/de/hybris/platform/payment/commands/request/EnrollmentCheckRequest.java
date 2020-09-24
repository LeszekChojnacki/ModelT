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
package de.hybris.platform.payment.commands.request;

import de.hybris.platform.payment.commands.EnrollmentCheckCommand;
import de.hybris.platform.payment.dto.BasicCardInfo;

import java.math.BigDecimal;
import java.util.Currency;


/**
 * Request for {@link EnrollmentCheckCommand}
 */
public class EnrollmentCheckRequest extends AbstractRequest
{
	private final BasicCardInfo card;
	private final Currency currency;
	private final BigDecimal totalAmount;
	private String httpAccept;
	private String httpUserAgent;


	/**
	 * @param merchantTransactionCode
	 * @param card
	 * @param currency
	 * @param totalAmount
	 */
	protected EnrollmentCheckRequest(final String merchantTransactionCode, final BasicCardInfo card, final Currency currency,
			final BigDecimal totalAmount)
	{
		super(merchantTransactionCode);
		this.card = card;
		this.currency = currency;
		this.totalAmount = totalAmount;
	}

	/**
	 * @param card
	 * @param currency
	 * @param totalAmount
	 */
	public EnrollmentCheckRequest(final String merchantTransactionCode, final BasicCardInfo card, final Currency currency,
			final BigDecimal totalAmount, final String httpAccept, final String httpUserAgent)
	{
		super(merchantTransactionCode);
		this.card = card;
		this.currency = currency;
		this.totalAmount = totalAmount;
		this.httpAccept = httpAccept;
		this.httpUserAgent = httpUserAgent;
	}

	/**
	 * @return the card
	 */
	public BasicCardInfo getCard()
	{
		return card;
	}


	/**
	 * @return the currency
	 */
	public Currency getCurrency()
	{
		return currency;
	}


	/**
	 * @return the totalAmount
	 */
	public BigDecimal getTotalAmount()
	{
		return totalAmount;
	}

	/**
	 * @return the httpAccept
	 */
	public String getHttpAccept()
	{
		return httpAccept;
	}

	/**
	 * @return the httpUserAgent
	 */
	public String getHttpUserAgent()
	{
		return httpUserAgent;
	}

	/**
	 * @param httpAccept
	 *           the httpAccept to set
	 */
	public void setHttpAccept(final String httpAccept)
	{
		this.httpAccept = httpAccept;
	}

	/**
	 * @param httpUserAgent
	 *           the httpUserAgent to set
	 */
	public void setHttpUserAgent(final String httpUserAgent)
	{
		this.httpUserAgent = httpUserAgent;
	}
}
