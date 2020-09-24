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

import de.hybris.platform.payment.commands.GetSubscriptionDataCommand;


/**
 * Request for {@link GetSubscriptionDataCommand}
 */
public class SubscriptionDataRequest extends AbstractRequest
{
	private final String subscriptionID;
	private final String paymentProvider;


	/**
	 * @param merchantTransactionCode
	 *           Merchants transaction ID
	 * @param subscriptionID
	 *           subscription ID
	 */
	public SubscriptionDataRequest(final String merchantTransactionCode, final String subscriptionID, final String paymentProvider)
	{
		super(merchantTransactionCode);
		this.subscriptionID = subscriptionID;
		this.paymentProvider = paymentProvider;
	}

	/**
	 * @return the subscriptionID
	 */
	public String getSubscriptionID()
	{
		return subscriptionID;
	}

	/**
	 * @return the paymentProvider
	 */
	public String getPaymentProvider()
	{
		return paymentProvider;
	}
}
