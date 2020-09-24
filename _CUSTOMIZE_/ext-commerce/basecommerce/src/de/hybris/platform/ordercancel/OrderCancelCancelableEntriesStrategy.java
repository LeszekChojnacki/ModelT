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
package de.hybris.platform.ordercancel;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.security.PrincipalModel;

import java.util.Map;


/**
 * This strategy interface provides information about cancelable entries of given order.
 */
public interface OrderCancelCancelableEntriesStrategy
{
	/**
	 * Provides information about cancelable entries of given order.
	 * 
	 * @return a Map containing an order entry as a key and a long value that indicates cancelable quantity of this order
	 *         entry.
	 */
	Map<AbstractOrderEntryModel, Long> getAllCancelableEntries(final OrderModel order, final PrincipalModel requestor);
}
