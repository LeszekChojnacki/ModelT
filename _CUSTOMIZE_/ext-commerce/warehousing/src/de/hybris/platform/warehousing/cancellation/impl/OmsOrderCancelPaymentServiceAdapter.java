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
package de.hybris.platform.warehousing.cancellation.impl;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.ordercancel.OrderCancelPaymentServiceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link OrderCancelPaymentServiceAdapter} to recalculate an order and modifying the payments after an order has been cancelled
 */
public class OmsOrderCancelPaymentServiceAdapter implements OrderCancelPaymentServiceAdapter
{
	private static final Logger LOG = LoggerFactory.getLogger(OmsOrderCancelPaymentServiceAdapter.class);

	@Override
	public void recalculateOrderAndModifyPayments(OrderModel order)
	{
		LOG.info("Recalculate and modify payments for order: {}", order.getCode());
	}
}
