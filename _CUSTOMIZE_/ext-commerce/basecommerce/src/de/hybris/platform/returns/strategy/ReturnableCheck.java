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
package de.hybris.platform.returns.strategy;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;


/**
 * This interface is used by {@link de.hybris.platform.returns.impl.DefaultReturnService} to provide information, if the
 * specified product is returnable.
 */
public interface ReturnableCheck
{
	/**
	 * Determines if the product is 'returnable'
	 * 
	 * @param order
	 *           the related original order
	 * @param entry
	 *           the ordered product which will be returned
	 * @param returnQuantity
	 *           the quantity of entries to be returned
	 * 
	 * @return true if product is 'returnable'
	 */
	boolean perform(final OrderModel order, final AbstractOrderEntryModel entry, final long returnQuantity);
}
