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
package de.hybris.platform.ordercancel.jalo;

import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.JaloBusinessException;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.order.Order;
import de.hybris.platform.jalo.type.ComposedType;

import org.apache.log4j.Logger;


public class OrderCancelRecord extends GeneratedOrderCancelRecord
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(OrderCancelRecord.class.getName());

	private static final String SUFFIX = "_CANCEL";

	@Override
	protected Item createItem(final SessionContext ctx, final ComposedType type, final ItemAttributeMap allAttributes)
			throws JaloBusinessException
	{
		final Order owningOrder = (Order) allAttributes.get(ORDER);
		allAttributes.put(IDENTIFIER, owningOrder + SUFFIX);
		allAttributes.setAttributeMode(IDENTIFIER, AttributeMode.INITIAL);
		return super.createItem(ctx, type, allAttributes);
	}

}
