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

import de.hybris.platform.payment.commands.request.VoidRequest;
import de.hybris.platform.payment.commands.result.VoidResult;


/**
 * Command for handling voiding capture or credit. Refund means to cancel a capture or credit request. Transaction can
 * be voided only if payment service provider has not already submitted the capture or credit card to processor.
 */
public interface VoidCommand extends Command<VoidRequest, VoidResult>
{
	// empty
}
