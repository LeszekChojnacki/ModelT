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

import de.hybris.platform.payment.commands.FollowOnRefundCommand;

import java.math.BigDecimal;
import java.util.Currency;


/**
 * Request for {@link FollowOnRefundCommand}
 */
public class FollowOnRefundRequest extends AbstractRequest
{
	private final String requestId;
	private final String requestToken;
	private final Currency currency;
	private final BigDecimal totalAmount;
	private final String paymentProvider;

	/**
	 * Constructs refund request.
	 * 
	 * @param merchantTransactionCode
	 *           merchantTransactionId
	 * @param requestId
	 *           request id from previous response
	 * @param requestToken
	 *           request token from previous response
	 */
	public FollowOnRefundRequest(final String merchantTransactionCode, final String requestId, final String requestToken,
			final Currency currency, final BigDecimal totalAmount, final String paymentProvider)
	{
		super(merchantTransactionCode);
		this.requestId = requestId;
		this.requestToken = requestToken;
		this.currency = currency;
		this.totalAmount = totalAmount;
		this.paymentProvider = paymentProvider;
	}

	/**
	 * @return the requestId
	 */
	public String getRequestId()
	{
		return requestId;
	}

	/**
	 * @return the requestToken
	 */
	public String getRequestToken()
	{
		return requestToken;
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
	 * @return the paymentProvider
	 */
	public String getPaymentProvider()
	{
		return paymentProvider;
	}
}
