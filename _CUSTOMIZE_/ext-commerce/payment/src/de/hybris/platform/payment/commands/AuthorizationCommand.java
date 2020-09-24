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

import de.hybris.platform.payment.commands.request.AuthorizationRequest;
import de.hybris.platform.payment.commands.result.AuthorizationResult;


/**
 * Command for handling card authorizations. Card authorization is the first step in card payment process. Authorized
 * amount of money remains "locked" on card's account until it is captured or authorization is reversed (cancelled) or
 * authorization is expired.
 */
public interface AuthorizationCommand extends Command<AuthorizationRequest, AuthorizationResult>
{
	//empty
}
