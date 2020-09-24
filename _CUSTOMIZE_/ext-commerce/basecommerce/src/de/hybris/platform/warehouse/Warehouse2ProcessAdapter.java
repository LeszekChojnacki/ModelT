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
package de.hybris.platform.warehouse;

import de.hybris.platform.ordersplitting.model.ConsignmentModel;


/**
 * The Interface WarehouseConnector.
 */
public interface Warehouse2ProcessAdapter
{


	/**
	 * Receive warehouse consignment status.
	 * 
	 * @param consignment
	 *           the consignment
	 * @param status
	 *           the status
	 */
	void receiveConsignmentStatus(ConsignmentModel consignment, WarehouseConsignmentStatus status);


}
