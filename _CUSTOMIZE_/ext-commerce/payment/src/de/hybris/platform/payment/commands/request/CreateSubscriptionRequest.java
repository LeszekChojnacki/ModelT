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

import de.hybris.platform.payment.commands.CreateSubscriptionCommand;
import de.hybris.platform.payment.dto.BillingInfo;
import de.hybris.platform.payment.dto.CardInfo;

import java.util.Currency;


/**
 * Request for {@link CreateSubscriptionCommand}
 */
public class CreateSubscriptionRequest extends AbstractRequest
{
	private final BillingInfo billingInfo;
	private final Currency currency;
	private final CardInfo card;
	private final String requestId;
	private final String requestToken;
	private final String paymentProvider;


	/**
	 * @param merchantTransactionCode
	 *           Merchants transaction ID
	 * @param billingInfo
	 *           Billing address
	 * @param currency
	 *           Currency of the payments
	 * @param card
	 *           Credit card info
	 * @param requestId
	 *           The requestID from the previous "Authorize" txn
	 * @param requestToken
	 *           the request token from the previous "Authorize" txn
	 */
	public CreateSubscriptionRequest(final String merchantTransactionCode, final BillingInfo billingInfo, final Currency currency,
			final CardInfo card, final String requestId, final String requestToken, final String paymentProvider)
	{
		super(merchantTransactionCode);
		this.billingInfo = billingInfo;
		this.currency = currency;
		this.card = card;
		this.requestId = requestId;
		this.requestToken = requestToken;
		this.paymentProvider = paymentProvider;
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
	 * @return the billingInfo
	 */
	public BillingInfo getBillingInfo()
	{
		return billingInfo;
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
}
