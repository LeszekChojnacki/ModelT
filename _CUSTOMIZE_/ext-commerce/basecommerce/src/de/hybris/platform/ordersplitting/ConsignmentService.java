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
package de.hybris.platform.ordersplitting;

import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;

import java.util.List;


/**
 * The ConsignmentService provides methods for creating consignments for orders.
 */
public interface ConsignmentService
{



	/**
	 * Creates the consignment.
	 *
	 * @param order
	 * 		the order. If Order is instance of OrderModel type then it is taken to relation to created consignment
	 * @param orderEntries
	 * 		the order entries
	 * @param code
	 * 		the code
	 * @return the consignment model
	 * @throws ConsignmentCreationException
	 * 		in the case of any inconsistency (e.g. warehouses not found)
	 */
	ConsignmentModel createConsignment(final AbstractOrderModel order, final String code,
			final List<AbstractOrderEntryModel> orderEntries) throws ConsignmentCreationException;



	/**
	 * Gets the warehouse. Simple random one from possible
	 *
	 * @param orderEntries
	 * 		the order entries
	 * @return the warehouse
	 */
	WarehouseModel getWarehouse(final List<AbstractOrderEntryModel> orderEntries);
}
