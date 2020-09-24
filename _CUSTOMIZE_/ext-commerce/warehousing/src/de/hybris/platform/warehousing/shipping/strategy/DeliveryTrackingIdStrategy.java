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
package de.hybris.platform.warehousing.shipping.strategy;

import de.hybris.platform.ordersplitting.model.ConsignmentModel;


/**
 * Strategy to generate delivery tracking id
 */
public interface DeliveryTrackingIdStrategy
{
	/**
	 * Generates the {@link ConsignmentModel#TRACKINGID} for the given {@link ConsignmentModel}
	 *
	 * @param consignment
	 * 		the {@link ConsignmentModel} for which tracking id needs to be generated
	 * @return the generated tracking Id
	 */
	String generateTrackingId(ConsignmentModel consignment);
}
