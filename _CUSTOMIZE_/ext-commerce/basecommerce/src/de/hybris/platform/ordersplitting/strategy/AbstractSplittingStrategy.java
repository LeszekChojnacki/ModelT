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
package de.hybris.platform.ordersplitting.strategy;


import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.strategy.impl.OrderEntryGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;


/**
 *
 */
public abstract class AbstractSplittingStrategy implements SplittingStrategy
{
	private static final Logger LOG = Logger.getLogger(AbstractSplittingStrategy.class);

	/**
	 * Gets the set of object that group consignmentEntry.
	 *
	 * @param orderEntry
	 *           the order entry
	 *
	 * @return the grouping object
	 */
	public abstract Object getGroupingObject(final AbstractOrderEntryModel orderEntry);


	/**
	 * After splitting.
	 *
	 * @param groupingObject
	 *           the grouping object
	 * @param createdOne
	 *           the created one
	 */
	public abstract void afterSplitting(Object groupingObject, final ConsignmentModel createdOne);

	@Override
	public void afterSplitting(final OrderEntryGroup group, final ConsignmentModel createdOne)
	{
		afterSplitting(group.getParameter(this.toString()), createdOne);
	}

	@Override
	public List<OrderEntryGroup> perform(final List<OrderEntryGroup> orderEntryListList)
	{
		final List<OrderEntryGroup> newListOrderEntryGroup = new ArrayList<>();

		for (final OrderEntryGroup orderEntryList : orderEntryListList)
		{
			final Map<Object, OrderEntryGroup> groupingMap = new HashMap<>();

			for (final AbstractOrderEntryModel orderEntry : orderEntryList)
			{
				final Object groupingObject = getGroupingObject(orderEntry);

				OrderEntryGroup tmpList = groupingMap.get(groupingObject);

				if (tmpList == null)
				{
					tmpList = orderEntryList.getEmpty();
					newListOrderEntryGroup.add(tmpList);
					tmpList.setParameter(this.toString(), groupingObject);


					groupingMap.put(groupingObject, tmpList);
				}
				tmpList.add(orderEntry);
			}
			if (LOG.isDebugEnabled())
			{
				logSplittingMap(groupingMap);
			}
		}
		return newListOrderEntryGroup;
	}

	private void logSplittingMap(final Map<Object, OrderEntryGroup> groupingMap)
	{
		if (!groupingMap.isEmpty())
		{
			LOG.debug("Resulting grouping map: ");
		}
		for (final Entry<Object, OrderEntryGroup> entry : groupingMap.entrySet())
		{
			if (entry != null)
			{
				LOG.debug(" > GroupingObject [" + entry.getKey().getClass().getName() + "] : " + entry.getKey());
				final OrderEntryGroup oeGroup = entry.getValue();
				if (oeGroup != null)
				{
					oeGroup.forEach(oe -> LOG.debug(" --- " + oe));
				}
			}
		}
	}
}
