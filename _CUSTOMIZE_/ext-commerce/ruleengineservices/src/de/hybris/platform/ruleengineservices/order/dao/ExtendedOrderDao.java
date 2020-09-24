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
package de.hybris.platform.ruleengineservices.order.dao;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.order.daos.OrderDao;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;


/**
 * Extends Data Access Object oriented on orders and order entries adding some more functionality.
 */
public interface ExtendedOrderDao extends OrderDao
{
	/**
	 * Finds the original Order by its code (excludes order snapshots). Throws runtime exception
	 * {@link ModelNotFoundException} in case no order is found.
	 *
	 * @param code
	 *           of an Order to return.
	 * @return Order by its code
	 */
	AbstractOrderModel findOrderByCode(String code);
}
