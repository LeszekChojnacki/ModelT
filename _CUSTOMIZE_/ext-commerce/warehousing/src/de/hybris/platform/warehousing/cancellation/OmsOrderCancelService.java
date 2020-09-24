/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 *
 */
package de.hybris.platform.warehousing.cancellation;

import de.hybris.platform.ordercancel.OrderCancelEntry;
import de.hybris.platform.ordercancel.OrderCancelException;
import de.hybris.platform.ordercancel.model.OrderCancelRecordEntryModel;

import java.util.List;


/**
 * OMS implementation of {@link de.hybris.platform.ordercancel.OrderCancelService}
 */
public interface OmsOrderCancelService
{
	/**
	 * process order cancellation
	 *
	 * @param orderCancelRecordEntryModel
	 * 		holds information about the order entry to be cancelled
	 * @return
	 * 		list of {@link OrderCancelEntry}
	 * @throws OrderCancelException
	 * 		{@link OrderCancelException}
	 */
	List<OrderCancelEntry> processOrderCancel(final OrderCancelRecordEntryModel orderCancelRecordEntryModel)
			throws OrderCancelException;
}
