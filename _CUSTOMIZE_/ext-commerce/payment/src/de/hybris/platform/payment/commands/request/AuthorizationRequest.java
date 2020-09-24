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

import de.hybris.platform.payment.commands.AuthorizationCommand;
import de.hybris.platform.payment.dto.BillingInfo;
import de.hybris.platform.payment.dto.CardInfo;

import java.math.BigDecimal;
import java.util.Currency;


/**
 * Request for {@link AuthorizationCommand}
 */
public class AuthorizationRequest extends AbstractRequest
{
	private final CardInfo card;
	private final Currency currency;
	private final BigDecimal totalAmount;
	private final BillingInfo shippingInfo;


	/**
	 * An authorized request, where missing data is filled by the payment provider from a previous txn.
	 * 
	 * @param merchantTransactionCode
	 * @param card
	 * @param currency
	 * @param totalAmount
	 * @param shippingInfo
	 *           can be null if subscriptionID is not null
	 */
	public AuthorizationRequest(final String merchantTransactionCode, final CardInfo card, final Currency currency,
			final BigDecimal totalAmount, final BillingInfo shippingInfo)
	{
		super(merchantTransactionCode);
		this.card = card;
		this.currency = currency;
		this.totalAmount = totalAmount;
		this.shippingInfo = shippingInfo;

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
	 * @return the shippingInfo
	 */
	public BillingInfo getShippingInfo()
	{
		return shippingInfo;
	}

}
