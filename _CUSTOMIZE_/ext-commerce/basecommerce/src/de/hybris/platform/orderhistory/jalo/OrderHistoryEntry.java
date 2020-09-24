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
package de.hybris.platform.orderhistory.jalo;

import de.hybris.platform.basecommerce.jalo.BasecommerceManager;
import de.hybris.platform.jalo.Item;
import de.hybris.platform.jalo.JaloBusinessException;
import de.hybris.platform.jalo.JaloInvalidParameterException;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.jalo.flexiblesearch.FlexibleSearch;
import de.hybris.platform.jalo.order.Order;
import de.hybris.platform.jalo.type.ComposedType;
import de.hybris.platform.orderhistory.model.OrderHistoryEntryModel;

import java.util.Collections;
import java.util.Date;
import java.util.List;


public class OrderHistoryEntry extends GeneratedOrderHistoryEntry
{

	@Override
	protected Item createItem(final SessionContext ctx, final ComposedType type, final ItemAttributeMap allAttributes)
			throws JaloBusinessException
	{
		final Order owningOrder = (Order) allAttributes.get(ORDER);
		// check for owning order
		if (owningOrder == null)
		{
			throw new JaloInvalidParameterException("Missing " + ORDER + " for creating a new " + type.getCode(), 0);
		}
		// check if previous order snapshot is actually a copy
		final Order prev = (Order) allAttributes.get(PREVIOUSORDERVERSION);
		if (prev != null && BasecommerceManager.getInstance().getVersionID(prev) == null)
		{
			throw new JaloInvalidParameterException("Illegal previous order version " + prev + " order is no copy!", 0);
		}
		// provide default timestamp
		if (allAttributes.get(TIMESTAMP) == null)
		{
			allAttributes.put(TIMESTAMP, new Date());
		}
		// if no position has been specified use query to get next one
		if (allAttributes.get(ORDERPOS) == null)
		{
			allAttributes.put(ORDERPOS, Integer.valueOf(queryNewPos(owningOrder)));
		}
		// perf: let all crucial attribute being written in INSERT
		allAttributes.setAttributeMode(ORDER, AttributeMode.INITIAL);
		allAttributes.setAttributeMode(ORDERPOS, AttributeMode.INITIAL);
		allAttributes.setAttributeMode(TIMESTAMP, AttributeMode.INITIAL);
		allAttributes.setAttributeMode(DESCRIPTION, AttributeMode.INITIAL);
		allAttributes.setAttributeMode(PREVIOUSORDERVERSION, AttributeMode.INITIAL);

		return super.createItem(ctx, type, allAttributes);
	}

	protected int queryNewPos(final Order owningOrder)
	{
		final List<Integer> ret = FlexibleSearch.getInstance() // NOSONAR
				.search(//
						"SELECT MAX({" + ORDERPOS + "}) FROM {" + OrderHistoryEntryModel._TYPECODE + "} " + //
								"WHERE {" + ORDER + "}=?o ", //
						Collections.singletonMap("o", owningOrder), //
						Integer.class)
				.getResult();

		return ret.isEmpty() || ret.get(0) == null ? 0 : ret.get(0).intValue() + 1;
	}

	@Override
	public void setPreviousOrderVersion(final SessionContext ctx, final Order prev)
	{
		// check if previous order snapshot is actually marked as copy
		if (prev != null && BasecommerceManager.getInstance().getVersionID(prev) == null)
		{
			throw new JaloInvalidParameterException("Illegal previous order version " + prev + " order is no copy!", 0);
		}
		super.setPreviousOrderVersion(ctx, prev);
	}
}
