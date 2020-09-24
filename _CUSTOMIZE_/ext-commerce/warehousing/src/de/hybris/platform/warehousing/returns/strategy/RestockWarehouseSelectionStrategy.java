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
package de.hybris.platform.warehousing.returns.strategy;

import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.returns.model.ReturnRequestModel;


/**
 * Strategy to apply for selecting the warehouse for restock.
 */
public interface RestockWarehouseSelectionStrategy
{
	/**
	 * Determine which warehouse the restock should return to.
	 *
	 * @param returnRequestModel
	 *           - the return request; cannot be <tt>null</tt>
	 */
	WarehouseModel performStrategy(final ReturnRequestModel returnRequestModel);
}
