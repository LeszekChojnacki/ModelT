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
 * 
 */
public interface OrderCancelNotificationServiceAdapter
{
	/**
	 * Method used for sending notifications when order cancel operation has finished.
	 * 
	 * @param cancelRequestRecordEntry
	 */
	void sendCancelFinishedNotifications(OrderCancelRecordEntryModel cancelRequestRecordEntry);

	/**
	 * Method used for sending notifications when order cancel operation has started.
	 */
	void sendCancelPendingNotifications(OrderCancelRecordEntryModel cancelRequestRecordEntry);
}
