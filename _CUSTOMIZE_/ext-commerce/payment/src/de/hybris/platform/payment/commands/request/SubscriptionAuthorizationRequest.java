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

import java.math.BigDecimal;
import java.util.Currency;



/**
 * Request for {@link AuthorizationCommand}
 */
public class SubscriptionAuthorizationRequest extends AbstractRequest
{
	private final String subscriptionID;

	private final Currency currency;
	private final BigDecimal totalAmount;
	private final BillingInfo shippingInfo;
	private final String cv2;

	private final String paymentProvider;

	/**
	 * An authorized request, where missing data is filled by the payment provider from a previous txn.
	 *
	 * @param merchantTransactionCode
	 * 		merchant transaction code
	 * @param subscriptionID
	 * 		id of subscription
	 * @param currency
	 * 		instance of {@link Currency}
	 * @param totalAmount
	 * 		{@link BigDecimal} representing the total amount
	 * @param shippingInfo
	 * 		can be null if subscriptionID is not null
	 * @param cv2
	 * 		the card verification value
	 * @param paymentProvider
	 * 		code of the payment provider
	 */
	public SubscriptionAuthorizationRequest(final String merchantTransactionCode, final String subscriptionID,
			final Currency currency, final BigDecimal totalAmount, final BillingInfo shippingInfo, final String cv2,
			final String paymentProvider)
	{
		super(merchantTransactionCode);
		this.subscriptionID = subscriptionID;
		this.currency = currency;
		this.totalAmount = totalAmount;
		this.shippingInfo = shippingInfo;
		this.cv2 = cv2;
		this.paymentProvider = paymentProvider;
	}


	public SubscriptionAuthorizationRequest(final String merchantTransactionCode, final String subscriptionID,
			final Currency currency, final BigDecimal totalAmount, final BillingInfo shippingInfo, final String paymentProvider)
	{
		super(merchantTransactionCode);
		this.subscriptionID = subscriptionID;
		this.currency = currency;
		this.totalAmount = totalAmount;
		this.shippingInfo = shippingInfo;
		this.cv2 = null;
		this.paymentProvider = paymentProvider;
	}

	/**
	 * @deprecated Since 4.4 use
	 * {@link #SubscriptionAuthorizationRequest(String, String, Currency, BigDecimal, BillingInfo, String)}
	 * or
	 * {@link #SubscriptionAuthorizationRequest(String, String, Currency, BigDecimal, BillingInfo, String, String)}
	 */
	@Deprecated
	public SubscriptionAuthorizationRequest(final String merchantTransactionCode, final String subscriptionID,      // NOSONAR
			final Currency currency, final BigDecimal totalAmount, final BillingInfo shippingInfo)
	{
		super(merchantTransactionCode);
		this.subscriptionID = subscriptionID;
		this.currency = currency;
		this.totalAmount = totalAmount;
		this.shippingInfo = shippingInfo;
		this.cv2 = null;
		this.paymentProvider = null;
	}

	/**
	 * @return the subscriptionID
	 */
	public String getSubscriptionID()
	{
		return subscriptionID;
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

	/**
	 * @return the card verification value
	 */
	public String getCv2()
	{
		return cv2;
	}


	/**
	 * @return the paymentProvider
	 */
	public String getPaymentProvider()
	{
		return paymentProvider;
	}

}
