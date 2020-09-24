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


import de.hybris.platform.basecommerce.enums.OrderEntryStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;


/**
 * Utilities to manage order
 */
public class OrderUtils
{

	private OrderUtils()
	{
		// prevent initialization
	}

	/**
	 * Check if in given order exist an entry that have OrderEntryStatus flag set to null or
	 * {@link OrderEntryStatus#LIVING}.
	 */
	public static boolean hasLivingEntries(final AbstractOrderModel order)
	{
		for (final AbstractOrderEntryModel entry : order.getEntries())
		{
			if (entry.getQuantityStatus() == null || OrderEntryStatus.LIVING.equals(entry.getQuantityStatus()))
			{
				return true;
			}
		}
		return false;
	}
}
