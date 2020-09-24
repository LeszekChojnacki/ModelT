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
package de.hybris.platform.promotionengineservices.order.dao;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.order.daos.OrderDao;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;


/**
 * Extends Data Access Object oriented on orders and order entries adding some more functionality.
 * 
 * @deprecated Since 6.7. Use {@link de.hybris.platform.ruleengineservices.order.dao.ExtendedOrderDao} instead.
 */
@Deprecated
public interface ExtendedOrderDao extends OrderDao
{
	/**
	 * Find Order by it code. Throws runtime exception {@link ModelNotFoundException} in case of nothing found.
	 *
	 * @param code
	 *           of an Order to return.
	 * @return Order by it code
	 */
	AbstractOrderModel findOrderByCode(String code);
}
