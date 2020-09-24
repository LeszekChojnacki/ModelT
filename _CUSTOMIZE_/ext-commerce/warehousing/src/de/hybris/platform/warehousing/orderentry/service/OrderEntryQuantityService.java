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
package de.hybris.platform.warehousing.orderentry.service;

import de.hybris.platform.core.model.order.OrderEntryModel;


/**
 * Provides the methods to retrieve quantities according to an order entry
 */
public interface OrderEntryQuantityService
{

	/**
	 * Gets the quantity that has been shipped for the given {@link OrderEntryModel}
	 * 
	 * @param orderEntryModel
	 *           the given order entry for which we want to get the shipped quantity
	 * @return the quantity shipped for the given order entry
	 */
	Long getQuantityShipped(OrderEntryModel orderEntryModel);

	/**
	 * Gets the quantity that has been cancelled for the given {@link OrderEntryModel}
	 *
	 * @param orderEntryModel
	 *           the given order entry for which we want to get the cancelled quantity
	 * @return the quantity cancelled for the given order entry
	 */
	Long getQuantityCancelled(OrderEntryModel orderEntryModel);

	/**
	 * Gets the quantity that has been allocated for the given {@link OrderEntryModel}
	 *
	 * @param orderEntryModel
	 *           the given order entry for which we want to get the allocated quantity
	 * @return the quantity allocated for the given order entry
	 */
	Long getQuantityAllocated(OrderEntryModel orderEntryModel);

	/**
	 * Gets the quantity that has not been allocated for the given {@link OrderEntryModel}
	 *
	 * @param orderEntryModel
	 *           the given order entry for which we want to get the unallocated quantity
	 * @return the quantity unallocated for the given order entry
	 */
	Long getQuantityUnallocated(OrderEntryModel orderEntryModel);


	/**
	 * Gets the pending quantity for the given {@link OrderEntryModel}
	 *
	 * @param orderEntryModel
	 *           the given order entry for which we want to get the pending quantity
	 * @return the quantity pending for the given order entry
	 */
	Long getQuantityPending(OrderEntryModel orderEntryModel);

	/**
	 * Gets the quantity that has been returned for the given {@link OrderEntryModel}
	 *
	 * @param orderEntryModel
	 *           the given order entry for which we want to get the returned quantity
	 * @return the quantity returned for the given order entry
	 */
	Long getQuantityReturned(OrderEntryModel orderEntryModel);

	/**
	 * Gets the quantity that was declined for the given {@link OrderEntryModel}
	 *
	 * @param orderEntryModel
	 *           the given order entry for which we want to get the declined quantity
	 * @return the quantity declined for the given order entry
	 */
	Long getQuantityDeclined(OrderEntryModel orderEntryModel);
}
