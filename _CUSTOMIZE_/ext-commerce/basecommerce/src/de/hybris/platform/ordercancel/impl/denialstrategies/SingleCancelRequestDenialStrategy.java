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
package de.hybris.platform.ordercancel.impl.denialstrategies;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.ordercancel.OrderCancelDenialReason;
import de.hybris.platform.ordercancel.OrderCancelDenialStrategy;
import de.hybris.platform.ordercancel.OrderCancelRecordsHandler;
import de.hybris.platform.ordercancel.model.OrderCancelConfigModel;
import de.hybris.platform.ordercancel.model.OrderCancelRecordModel;


/**
 * Strategy that forbids cancel, where there's already a previous (pending) order cancel requests.
 */
public class SingleCancelRequestDenialStrategy extends AbstractCancelDenialStrategy implements OrderCancelDenialStrategy
{
	private OrderCancelRecordsHandler orderCancelRecordsHandler;

	@Override
	public OrderCancelDenialReason getCancelDenialReason(final OrderCancelConfigModel configuration, final OrderModel order,
			final PrincipalModel requester, final boolean partialCancel, final boolean partialEntryCancel)
	{
		final OrderCancelRecordModel orderCancel = this.orderCancelRecordsHandler.getCancelRecord(order);
		if (orderCancel == null)
		{
			return null;
		}
		else
		{
			if (orderCancel.isInProgress())
			{
				return getReason();
			}
			else
			{
				return null;
			}
		}
	}

	/**
	 * @return the orderCancelRecordsHandler
	 */
	public OrderCancelRecordsHandler getOrderCancelRecordsHandler()
	{
		return orderCancelRecordsHandler;
	}

	/**
	 * @param orderCancelRecordsHandler
	 *           the orderCancelRecordsHandler to set
	 */
	public void setOrderCancelRecordsHandler(final OrderCancelRecordsHandler orderCancelRecordsHandler)
	{
		this.orderCancelRecordsHandler = orderCancelRecordsHandler;
	}
}
