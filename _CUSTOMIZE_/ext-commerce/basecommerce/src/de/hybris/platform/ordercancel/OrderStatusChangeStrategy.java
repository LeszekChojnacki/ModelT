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
 * This interface is used to change OrderStatus of an Order after cancel operation. It is used by several
 * RequestExecutor/ResponseExecutor classes to decouple concrete OrderStatus changes from the executor logic.
 */
public interface OrderStatusChangeStrategy
{
	void changeOrderStatusAfterCancelOperation(OrderCancelRecordEntryModel orderCancelRecordEntry, boolean saveOrderModel);
}
