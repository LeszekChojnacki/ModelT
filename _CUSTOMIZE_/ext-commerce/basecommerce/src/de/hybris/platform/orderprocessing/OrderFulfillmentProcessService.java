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
package de.hybris.platform.orderprocessing;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;


/**
 * Convenience service dealing with order in the context of order oriented business process
 */
public interface OrderFulfillmentProcessService
{
	/**
	 * Starts a defined fulfillment process for an order. The process to start is defined as a
	 * 'basecommerce.fulfillmentprocess.name' property in project.properties.
	 * 
	 * @param order
	 *           - the order to be submitted to the process
	 * @return OrderProcessModel - the process model reference
	 */
	OrderProcessModel startFulfillmentProcessForOrder(OrderModel order);
}
