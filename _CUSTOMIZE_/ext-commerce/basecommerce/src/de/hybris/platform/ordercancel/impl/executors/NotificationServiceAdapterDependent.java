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
package de.hybris.platform.ordercancel.impl.executors;

import de.hybris.platform.ordercancel.OrderCancelNotificationServiceAdapter;


/**
 * A marker interface used to make junit tests easier.
 */
public interface NotificationServiceAdapterDependent
{
	void setNotificationServiceAdapter(final OrderCancelNotificationServiceAdapter notificationServiceAdapter);
}
