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

import de.hybris.platform.payment.commands.UpdateSubscriptionCommand;
import de.hybris.platform.payment.dto.BillingInfo;
import de.hybris.platform.payment.dto.CardInfo;


/**
 * Request for {@link UpdateSubscriptionCommand}
 */
public class UpdateSubscriptionRequest extends AbstractRequest
{
	private final String subscriptionID;
	private final String paymentProvider;
	private final BillingInfo billingInfo;
	private final CardInfo card;


	/**
	 * @param merchantTransactionCode
	 *           Merchants transaction ID
	 * @param subscriptionID
	 *           subscription ID
	 */
	public UpdateSubscriptionRequest(final String merchantTransactionCode, final String subscriptionID,
			final String paymentProvider, final BillingInfo billingInfo, final CardInfo card)
	{
		super(merchantTransactionCode);
		this.subscriptionID = subscriptionID;
		this.paymentProvider = paymentProvider;
		this.billingInfo = billingInfo;
		this.card = card;
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

	/**
	 * @return the card
	 */
	public CardInfo getCard()
	{
		return card;
	}

	/**
	 * @return the billingInfo
	 */
	public BillingInfo getBillingInfo()
	{
		return billingInfo;
	}
}
