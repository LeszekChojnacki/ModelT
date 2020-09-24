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

import de.hybris.platform.payment.commands.request.AbstractRequest;
import de.hybris.platform.payment.commands.result.RefundResult;


/**
 * Command for handling follow-on refunds. Follow-on refund means to return back money to customer account associated
 * with order or previous transaction. It is contrary to stand-alone refund {@link StandaloneRefundCommand}
 */
public interface FollowOnRefundCommand<T extends AbstractRequest> extends Command<T, RefundResult>
{
	@Override
	RefundResult perform(T request);
}
