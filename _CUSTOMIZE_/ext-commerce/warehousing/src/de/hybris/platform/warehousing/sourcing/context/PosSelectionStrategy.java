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
package de.hybris.platform.warehousing.sourcing.context;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.storelocator.model.PointOfServiceModel;


/**
 * Strategy to define which point of service to use for a specific warehouse.
 */
public interface PosSelectionStrategy
{
	/**
	 * Retrieve a specific point of service for a specific warehouse and a specific order
	 *
	 * @param orderModel
	 *           the associated order
	 * @param warehouse
	 *           the warehouse from which we want to get a point of service; cannot be <tt>null</tt>
	 */
	PointOfServiceModel getPointOfService(AbstractOrderModel orderModel, WarehouseModel warehouse);

}
