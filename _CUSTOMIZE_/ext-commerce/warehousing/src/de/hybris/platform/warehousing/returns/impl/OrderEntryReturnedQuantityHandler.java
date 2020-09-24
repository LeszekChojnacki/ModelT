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
package de.hybris.platform.warehousing.returns.impl;

import org.springframework.beans.factory.annotation.Required;

import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.servicelayer.model.attribute.DynamicAttributeHandler;
import de.hybris.platform.warehousing.orderentry.service.OrderEntryQuantityService;


/**
 * Handler for {@link OrderEntryModel#getQuantityReturned}
 */
public class OrderEntryReturnedQuantityHandler implements DynamicAttributeHandler<Long, OrderEntryModel>
{
	private OrderEntryQuantityService orderEntryQuantityService;

	/**
	 * Calculates the number of items that were returned for a refund. This means that these items cannot be returned a
	 * second time. Replacements are not part of this calculation since it is possible to replace the same item multiple
	 * times, while this is not the case for refunds.
	 */
	@Override
	public Long get(final OrderEntryModel orderEntry)
	{
		return getOrderEntryQuantityService().getQuantityReturned(orderEntry);
	}

	@Override
	public void set(final OrderEntryModel orderEntry, final Long quantity)
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
