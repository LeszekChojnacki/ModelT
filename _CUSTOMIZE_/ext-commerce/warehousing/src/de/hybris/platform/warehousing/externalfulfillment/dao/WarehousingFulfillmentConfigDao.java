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
package de.hybris.platform.warehousing.externalfulfillment.dao;


import de.hybris.platform.ordersplitting.model.WarehouseModel;


/**
 * Provides operations on the warehousing fulfillment system configuration for a {@link WarehouseModel}
 */
public interface WarehousingFulfillmentConfigDao
{
	/**
	 * Retrieves the fulfillment system configuration attached to a {@link WarehouseModel}
	 *
	 * @param warehouse
	 * 		the {@link WarehouseModel} for which to retrieve the fulfillment system configuration
	 * @return the configuration object
	 */
	Object getConfiguration(WarehouseModel warehouse);
}
