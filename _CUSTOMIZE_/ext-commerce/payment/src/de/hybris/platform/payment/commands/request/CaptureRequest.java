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

import de.hybris.platform.payment.commands.CaptureCommand;

import java.math.BigDecimal;
import java.util.Currency;


/**
 * Request for {@link CaptureCommand}
 */
public class CaptureRequest extends AbstractRequest
{
	private final String requestId;
	private final String requestToken;
	private final Currency currency;
	private final BigDecimal totalAmount;
	private final String paymentProvider;
	private final String subscriptionID;

	/**
	 * Constructs {@link CaptureRequest} without subscription identifier
	 *
	 * @param merchantTransactionCode
	 * @param requestId
	 * @param requestToken
	 * @param currency
	 * @param totalAmount
	 * @param paymentProvider
	 */
	public CaptureRequest(final String merchantTransactionCode, final String requestId, final String requestToken,
			final Currency currency, final BigDecimal totalAmount, final String paymentProvider)
	{
		this(merchantTransactionCode, requestId, requestToken, currency, totalAmount, paymentProvider, null);
	}

	/**
	 * Constructs {@link CaptureRequest}
	 *
	 * @param merchantTransactionCode
	 * @param requestId
	 * @param requestToken
	 * @param currency
	 * @param totalAmount
	 * @param paymentProvider
	 * @param subscriptionID
	 */
	public CaptureRequest(final String merchantTransactionCode, final String requestId, final String requestToken,
			final Currency currency, final BigDecimal totalAmount, final String paymentProvider, final String subscriptionID)
	{
		super(merchantTransactionCode);
		this.requestId = requestId;
		this.requestToken = requestToken;
		this.currency = currency;
		this.totalAmount = totalAmount;
		this.paymentProvider = paymentProvider;
		this.subscriptionID = subscriptionID;
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

	/**
	 * @return the subscriptionID
	 */
	public String getSubscriptionID()
	{
		return subscriptionID;
	}
}
