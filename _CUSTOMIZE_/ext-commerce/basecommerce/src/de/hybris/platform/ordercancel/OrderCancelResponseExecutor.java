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
package de.hybris.platform.ordercancel;

import de.hybris.platform.ordercancel.model.OrderCancelRecordEntryModel;


/**
 * This is the interface for executors of cancel responses - messages received from a Warehouse in response to cancel
 * request. An executor performs all actions necessary to finish pending cancel operation. Response executors do not
 * deal with initial cancel requests to Order Cancel Service; This is handled by {@link OrderCancelRequestExecutor}
 */
public interface OrderCancelResponseExecutor
{
	void processCancelResponse(final OrderCancelResponse orderCancelResponse, OrderCancelRecordEntryModel cancelRequestRecordEntry)
			throws OrderCancelException;
}
