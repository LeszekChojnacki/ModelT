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
package de.hybris.platform.warehousing.daos;

import de.hybris.platform.core.model.order.OrderEntryModel;

import java.util.Map;


/**
 * Provides the methods to retrieve the various quantities related to an order entry
 */
public interface WarehousingOrderEntryQuantityDao
{

	/**
	 * Retrieve the cancelled quantity for a specific order entry
	 *
	 * @param orderEntry
	 *           the order entry for which we want to get the cancelled quantity
	 * @return the cancelled quantity
	 */
	Long getCancelledQuantity(OrderEntryModel orderEntry);

	/**
	 * Retrieve the quantity returned for a specific order entry
	 *
	 * @param orderEntry
	 *           the order entry for which we want to get the returned quantity
	 * @return the returned quantity
	 */
	Long getQuantityReturned(OrderEntryModel orderEntry);

	/**
	 * Process the flexible search given in parameter and applies the list of parameters associated
	 *
	 * @param queryString
	 *           the flexible search to process
	 * @param params
	 *           the list of params requested by the associated query
	 * @return the quantity asked
	 */
	Long processRequestWithParams(String queryString, Map<String, Object> params);

}
