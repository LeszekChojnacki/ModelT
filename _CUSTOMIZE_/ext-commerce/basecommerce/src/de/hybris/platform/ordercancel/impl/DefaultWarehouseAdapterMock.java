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
package de.hybris.platform.ordercancel.impl;

import de.hybris.platform.ordercancel.OrderCancelRequest;
import de.hybris.platform.ordercancel.OrderCancelWarehouseAdapter;

import org.apache.log4j.Logger;


/**
 * Mock implementation for {@link OrderCancelWarehouseAdapter}
 */
public class DefaultWarehouseAdapterMock implements OrderCancelWarehouseAdapter
{
	private static final Logger LOG = Logger.getLogger(DefaultWarehouseAdapterMock.class.getName());

	@Override
	public void requestOrderCancel(final OrderCancelRequest orderCancelRequest)
	{
		LOG
				.info("MOCK: Order cancel request is being sent to the Warehouse. Cancel will proceed after a response from the Warehouse is received");
	}
}
