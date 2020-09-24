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

import de.hybris.platform.payment.commands.StandaloneRefundCommand;
import de.hybris.platform.payment.dto.BillingInfo;
import de.hybris.platform.payment.dto.CardInfo;

import java.math.BigDecimal;
import java.util.Currency;


/**
 * Request for {@link StandaloneRefundCommand}
 */
public class StandaloneRefundRequest extends AbstractRequest
{
	private final String subscriptionID;

	private final BillingInfo billTo;
	private final CardInfo card;
	private final Currency currency;
	private final BigDecimal totalAmount;
	private final String paymentProvider;

	/**
	 * Constructs a simple request
	 *
	 * @param merchantTransactionCode
	 * @param billTo
	 * 		customer address for which to refund money
	 * @param card
	 * 		card information
	 * @param currency
	 * 		currency
	 * @param totalAmount
	 * 		how much money to refund
	 */
	public StandaloneRefundRequest(final String merchantTransactionCode, final BillingInfo billTo, final CardInfo card,
			final Currency currency, final BigDecimal totalAmount)
	{
		this(merchantTransactionCode, null, billTo, card, currency, totalAmount, null);
	}

	/**
	 * Constructs request, where missing data is filled by the payment provider from a previous txn.
	 *
	 * @param merchantTransactionCode
	 * @param subscriptionID
	 * 		can be null
	 * @param billTo
	 * 		customer address for which to refund money, can be null if subscriptionID is not null
	 * @param card
	 * 		card information, can be null if subscriptionID is not null
	 * @param currency
	 * 		currency
	 * @param totalAmount
	 * 		how much money to refund
	 */
	public StandaloneRefundRequest(final String merchantTransactionCode, final String subscriptionID, final BillingInfo billTo,
			final CardInfo card, final Currency currency, final BigDecimal totalAmount)
	{
		this(merchantTransactionCode, subscriptionID, billTo, card, currency, totalAmount, null);
	}

	/**
	 * Constructs request
	 *
	 * @param merchantTransactionCode
	 * 		merchant transaction code
	 * @param subscriptionID
	 * 		can be null
	 * @param billTo
	 * 		customer address for which to refund money, can be null if subscriptionID is not null
	 * @param card
	 * 		card information, can be null if subscriptionID is not null
	 * @param currency
	 * 		currency
	 * @param totalAmount
	 * 		how much money to refund
	 * @param paymentProvider
	 * 		payment provider's name
	 */
	public StandaloneRefundRequest(final String merchantTransactionCode, final String subscriptionID, final BillingInfo billTo,
			final CardInfo card, final Currency currency, final BigDecimal totalAmount, final String paymentProvider)
	{
		super(merchantTransactionCode);
		this.subscriptionID = subscriptionID;
		this.billTo = billTo;
		this.card = card;
		this.currency = currency;
		this.totalAmount = totalAmount;
		this.paymentProvider = paymentProvider;
	}

	/**
	 * @return the isTokenizedRequest
	 */
	public boolean isTokenizedRequest()
	{
		return subscriptionID != null;
	}


	/**
	 * @return the subscriptionID
	 */
	public String getSubscriptionID()
	{
		return subscriptionID;
	}


	/**
	 * @return the billTo
	 */
	public BillingInfo getBillTo()
	{
		return billTo;
	}

	/**
	 * @return the card
	 */
	public CardInfo getCard()
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
	 * @return the providerName
	 */
	public String getPaymentProvider()
	{
		return paymentProvider;
	}
}
