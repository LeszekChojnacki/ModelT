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
package de.hybris.platform.orderhandler;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.model.attribute.DynamicAttributeHandler;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * 
 * This dynamic attribute handler for Order type returns the localized value of the order status.
 * 
 *
 */
public class DynamicAttributesOrderStatusDisplayByEnum implements DynamicAttributeHandler<String, OrderModel>
{
	private EnumerationService enumerationService;
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(DynamicAttributesOrderStatusDisplayByEnum.class);

	@Required
	public void setEnumerationService(final EnumerationService enumerationService)
	{
		this.enumerationService = enumerationService;
	}

	@Override
	public String get(final OrderModel order)
	{
		final String ret = StringUtils.EMPTY;
		if (order == null)
		{
			throw new IllegalArgumentException("Item model is required");
		}
		if (order.getStatus() == null)
		{
			return ret;
		}
		return enumerationService.getEnumerationName(order.getStatus());
	}

	@Override
	public void set(final OrderModel model, final String value)
	{
		throw new UnsupportedOperationException();
	}

}
