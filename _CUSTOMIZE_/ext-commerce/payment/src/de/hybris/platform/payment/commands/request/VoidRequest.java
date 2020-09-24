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

import de.hybris.platform.payment.commands.VoidCommand;

import java.math.BigDecimal;
import java.util.Currency;


/**
 * Request for {@link VoidCommand}
 */
public class VoidRequest extends AbstractRequest
{
	private final String requestId;
	private final String requestToken;
	private final String paymentProvider;
	private Currency currency;
	private BigDecimal totalAmount;

	/**
	 * Constructs refund request.
	 *
	 * @param merchantTransactionCode
	 * 		merchantTransactionId
	 * @param requestId
	 * 		request id from previous response
	 * @param requestToken
	 * 		request token from previous response
	 * @param paymentProvider
	 * 		name of the payment provider
	 */
	public VoidRequest(final String merchantTransactionCode, final String requestId, final String requestToken,
			final String paymentProvider)
	{
		super(merchantTransactionCode);
		this.requestId = requestId;
		this.requestToken = requestToken;
		this.paymentProvider = paymentProvider;
	}

	/**
	 * Constructs refund request.
	 *
	 * @param merchantTransactionCode
	 * 		merchantTransactionId
	 * @param requestId
	 * 		request id from previous response
	 * @param requestToken
	 * 		request token from previous response
	 * @param paymentProvider
	 * 		name of the payment provider
	 * @param currency
	 * 		currency used for the transaction to be cancelled
	 * @param totalAmount
	 * 		amount that needs to be reverted
	 */
	public VoidRequest(final String merchantTransactionCode, final String requestId, final String requestToken,
			final String paymentProvider, final Currency currency, final BigDecimal totalAmount)
	{
		this(merchantTransactionCode, requestId, requestToken, paymentProvider);
		this.currency = currency;
		this.totalAmount = totalAmount;
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
	 * @return the paymentProvider
	 */
	public String getPaymentProvider()
	{
		return paymentProvider;
	}

	public BigDecimal getTotalAmount()
	{
		return totalAmount;
	}

	public void setTotalAmount(final BigDecimal totalAmount)
	{
		this.totalAmount = totalAmount;
	}

	public Currency getCurrency()
	{
		return currency;
	}

	public void setCurrency(final Currency currency)
	{
		this.currency = currency;
	}
}
