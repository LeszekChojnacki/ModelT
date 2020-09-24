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
package de.hybris.platform.couponservices.dao;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.couponservices.model.RuleBasedAddCouponActionModel;

import java.util.List;


/**
 * The DAO class is used to retrieve {@link RuleBasedAddCouponActionModel}
 */
public interface RuleBasedCouponActionDao
{
	/**
	 * Returns the list of RuleBasedAddCouponActionModel for a given Order.
	 *
	 * @param order
	 *           the order
	 *
	 * @return the list of RuleBasedAddCouponActionModel for a given order.
	 */
	List<RuleBasedAddCouponActionModel> findRuleBasedCouponActionByOrder(OrderModel order);
}
