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

import de.hybris.platform.payment.commands.CreateSubscriptionCommand;
import de.hybris.platform.payment.commands.DeleteSubscriptionCommand;


/**
 * Result for {@link CreateSubscriptionCommand} and {@link DeleteSubscriptionCommand}
 */
public class SubscriptionResult extends AbstractResult
{
	private String subscriptionID;


	/**
	 * @return the subscriptionID
	 */
	public String getSubscriptionID()
	{
		return subscriptionID;
	}

	/**
	 * @param subscriptionID
	 *           the subscriptionID to set
	 */
	public void setSubscriptionID(final String subscriptionID)
	{
		this.subscriptionID = subscriptionID;
	}

}
