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
package de.hybris.platform.warehousing.externalfulfillment.strategy;


import de.hybris.platform.ordersplitting.model.ConsignmentModel;


/**
 * Strategy to send the given {@link ConsignmentModel} to external fulfillment system
 */
public interface SendConsignmentToExternalFulfillmentSystemStrategy
{
	/**
	 * Sends {@link ConsignmentModel} to external fulfillment system
	 *
	 * @param consignment
	 * 		the {@link ConsignmentModel}
	 */
	void sendConsignment(ConsignmentModel consignment);
}
