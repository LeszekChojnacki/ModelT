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
package de.hybris.platform.warehousing.sourcing;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.warehousing.data.sourcing.SourcingResults;


/**
 * Service layer to source an order using sourcing strategies.
 */
public interface SourcingService
{
	/**
	 * Evaluate the best way to source an order using some defined sourcing strategies.
	 *
	 * @param order
	 *           - the order to be sourced; never <tt>null</tt>
	 * @return the sourcing results indicating if the order can be completely sourced or not and from which locations.
	 */
	SourcingResults sourceOrder(AbstractOrderModel order);

}
