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

import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.servicelayer.model.attribute.DynamicAttributeHandler;
import de.hybris.platform.warehousing.orderentry.service.OrderEntryQuantityService;


/**
 * Handler for {@link OrderEntryModel#getQuantityCancelled}
 */
public class OrderEntryCancelledQuantityHandler implements DynamicAttributeHandler<Long, OrderEntryModel>
{
	private OrderEntryQuantityService orderEntryQuantityService;

	@Override
	public Long get(final OrderEntryModel orderEntry)
	{
		return getOrderEntryQuantityService().getQuantityCancelled(orderEntry);
	}

	@Override
	public void set(final OrderEntryModel orderEntry, final Long value)
	{
		throw new UnsupportedOperationException();
	}

	public OrderEntryQuantityService getOrderEntryQuantityService()
	{
		return orderEntryQuantityService;
	}

	@Required
	public void setOrderEntryQuantityService(OrderEntryQuantityService orderEntryQuantityService)
	{
		this.orderEntryQuantityService = orderEntryQuantityService;
	}
}
