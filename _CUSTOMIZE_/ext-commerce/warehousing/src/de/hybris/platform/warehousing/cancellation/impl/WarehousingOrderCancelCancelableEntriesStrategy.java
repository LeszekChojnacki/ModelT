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

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.ordercancel.OrderCancelCancelableEntriesStrategy;

import java.util.Map;
import java.util.HashMap;


/**
 * Warehousing implementation for {@link OrderCancelCancelableEntriesStrategy}. Not cancellable quantities of Order entries
 * (i.e. single items that cannot be cancelled from order entry) are evaluated by dynamic attribute {@link OrderEntryModel#QUANTITYPENDING}
 */
public class WarehousingOrderCancelCancelableEntriesStrategy implements OrderCancelCancelableEntriesStrategy
{

	@Override
	public Map<AbstractOrderEntryModel, Long> getAllCancelableEntries(final OrderModel order, final PrincipalModel requestor)
	{
		final Map<AbstractOrderEntryModel, Long> cancellableEntries = new HashMap<>();
		order.getEntries().stream().filter(entry -> ((OrderEntryModel)entry).getQuantityPending().longValue() > 0)
				.forEach(entry -> cancellableEntries.put(entry, ((OrderEntryModel)entry).getQuantityPending()));

		return cancellableEntries;
	}
}
