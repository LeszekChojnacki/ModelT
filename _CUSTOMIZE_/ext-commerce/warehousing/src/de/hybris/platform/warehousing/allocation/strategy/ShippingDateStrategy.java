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
package de.hybris.platform.warehousing.allocation.strategy;


import de.hybris.platform.ordersplitting.model.ConsignmentModel;

import java.util.Date;


/**
 * Strategy to evaluate {@link ConsignmentModel#SHIPPINGDATE}.
 */
public interface ShippingDateStrategy
{
	/**
	 * Determine the expected shipping date of the consignment
	 *
	 * @param consignment
	 * 		the {@link ConsignmentModel} for which expected shipping date is being calculated
	 * @return the expected {@link ConsignmentModel#SHIPPINGDATE}
	 */
	Date getExpectedShippingDate(ConsignmentModel consignment);
}
