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
package de.hybris.platform.warehousing.shipping.service;

import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.warehousing.process.BusinessProcessException;


/**
 * Service to confirm ship/pickup of {@link de.hybris.platform.ordersplitting.model.ConsignmentModel}
 */
public interface WarehousingShippingService
{
	/**
	 * Checks if the confirm ship/pickup is possible for the given {@link ConsignmentModel}
	 * @param consignment
	 * 		the {@link ConsignmentModel} to be confirmed
	 * @return boolean to indicate if confirmation of the given consignment is possible
	 */
	boolean isConsignmentConfirmable(ConsignmentModel consignment);

	/**
	 * Confirms the shipping of {@link ConsignmentModel}
	 * @param consignment
	 * 		the {@link ConsignmentModel} to be confirmed
	 * @throws BusinessProcessException
	 * 		when associated process cannot move to the confirmation state
	 */
	void confirmShipConsignment(ConsignmentModel consignment) throws BusinessProcessException;

	/**
	 * Confirms the pickup of {@link ConsignmentModel}
	 * @param consignment
	 * 		the {@link ConsignmentModel} to be confirmed
	 * @throws BusinessProcessException
	 * 		when associated process cannot move to the confirmation state
	 */
	void confirmPickupConsignment(ConsignmentModel consignment) throws BusinessProcessException;

}
