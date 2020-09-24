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
package de.hybris.platform.ordersplitting.strategy.impl;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple grouping mechanism used for order splitting
 */
public class OrderEntryGroup extends ArrayList<AbstractOrderEntryModel>
{
	private final Map<String, Object> parameters = new HashMap<>(); // NOSONAR (false positive for "make transient")

	public Object getParameter(final String paramName)
	{
		return parameters.get(paramName);
	}

	public void setParameter(final String paramName, final Object paramValue)
	{
		parameters.put(paramName, paramValue);
	}

	public OrderEntryGroup getEmpty()
	{
		final OrderEntryGroup result = new OrderEntryGroup();
		result.parameters.putAll(this.parameters);
		return result;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		if (!super.equals(o))
		{
			return false;
		}

		final OrderEntryGroup that = (OrderEntryGroup) o;
		return parameters.equals(that.parameters);

	}

	@Override
	public int hashCode()
	{
		return 31 * super.hashCode() + parameters.hashCode();
	}
}
