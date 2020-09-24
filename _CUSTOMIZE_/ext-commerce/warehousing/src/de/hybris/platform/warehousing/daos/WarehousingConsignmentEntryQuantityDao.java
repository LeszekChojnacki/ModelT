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

import de.hybris.platform.ordersplitting.model.ConsignmentEntryModel;

import java.util.Map;


/**
 * Provides the methods to retrieve the various quantities related to a consignment entry
 */
public interface WarehousingConsignmentEntryQuantityDao
{

	/**
	 * Retrieve the quantity shipped for a specific order entry
	 *
	 * @param consignmentEntry
	 *           the consignment entry for which we want to get the shipped quantity
	 * @return the shipped quantity
	 */
	Long getQuantityShipped(ConsignmentEntryModel consignmentEntry);

	/**
	 * Retrieve the quantity declined for a specific consignment entry
	 *
	 * @param consignmentEntry
	 *           the consignment entry for which we want to get the declined quantity
	 * @return the declined quantity
	 */
	Long getQuantityDeclined(ConsignmentEntryModel consignmentEntry);

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
