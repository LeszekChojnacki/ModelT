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

import de.hybris.platform.payment.commands.request.IsApplicableCommandReqest;
import de.hybris.platform.payment.commands.result.IsApplicableCommandResult;


/**
 * Command that each payment provider must implement - configuration that check if for specified arguments payment
 * provider is applicable
 */
public interface IsApplicableCommand extends Command<IsApplicableCommandReqest, IsApplicableCommandResult>
{
	//empty
}
