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
package de.hybris.platform.warehousing.consignmententry.service;

import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;


/**
 * Provides the methods to retrieve quantities according to an order entry
 */
public interface ConsignmentEntryQuantityService
{
	/**
	 * Gets the quantity that has been shipped for the given {@link ConsignmentEntryModel}
	 *
	 * @param consignmentEntryModel
	 *           the given consignment entry for which we want to get the shipped quantity
	 * @return the quantity shipped for the given consignment entry
	 */
	Long getQuantityShipped(ConsignmentEntryModel consignmentEntryModel);

	/**
	 * Gets the pending quantity for the given {@link ConsignmentEntryModel}
	 *
	 * @param consignmentEntryModel
	 *           the given consignment entry for which we want to get the pending quantity
	 * @return the quantity pending for the given consignment entry
	 */
	Long getQuantityPending(ConsignmentEntryModel consignmentEntryModel);

	/**
	 * Retrieve the quantity declined for a specific consignment entry
	 *
	 * @param consignmentEntry
	 *           the consignment entry for which we want to get the declined quantity
	 * @return the declined quantity
	 */
	Long getQuantityDeclined(ConsignmentEntryModel consignmentEntry);
}
