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

import de.hybris.platform.payment.commands.request.CaptureRequest;
import de.hybris.platform.payment.commands.result.CaptureResult;


/**
 * Command for handling card authorization captures. Capturing an authorization means the authorized amount of money is
 * actually transferred from the card holder account to the merchant account. Capture operation requires a previous
 * successful authorization that has not yet expired.
 */
public interface CaptureCommand extends Command<CaptureRequest, CaptureResult>
{
	//empty
}
