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
package de.hybris.platform.payment.commands.result;

import de.hybris.platform.payment.commands.GetSubscriptionDataCommand;
import de.hybris.platform.payment.dto.BillingInfo;
import de.hybris.platform.payment.dto.CardInfo;


/**
 * Result for {@link GetSubscriptionDataCommand}
 */
public class SubscriptionDataResult extends SubscriptionResult
{
	private BillingInfo billingInfo;
	private CardInfo card;


	/**
	 * @return the card
	 */
	public CardInfo getCard()
	{
		return card;
	}

	/**
	 * @param card
	 *           the CardInfo to set
	 */
	public void setCard(final CardInfo card)
	{
		this.card = card;
	}

	/**
	 * @return the billingInfo
	 */
	public BillingInfo getBillingInfo()
	{
		return billingInfo;
	}

	/**
	 * @param billingInfo
	 *           the BillingInfo to set
	 */
	public void setBillingInfo(final BillingInfo billingInfo)
	{
		this.billingInfo = billingInfo;
	}

}
