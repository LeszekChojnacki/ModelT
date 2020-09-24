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
package de.hybris.platform.payment.commands;

import de.hybris.platform.payment.commands.request.SubscriptionDataRequest;
import de.hybris.platform.payment.commands.result.SubscriptionDataResult;


/**
 * Command for getting data from a subscription.
 */
public interface GetSubscriptionDataCommand extends Command<SubscriptionDataRequest, SubscriptionDataResult>
{
	//empty
}
