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
package de.hybris.platform.couponservices.dao.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.couponservices.dao.RuleBasedCouponActionDao;
import de.hybris.platform.couponservices.model.RuleBasedAddCouponActionModel;
import de.hybris.platform.promotionengineservices.model.RuleBasedPromotionModel;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.servicelayer.internal.dao.AbstractItemDao;

import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * Default implementation of {@link RuleBasedCouponActionDao}.
 */
public class DefaultRuleBasedCouponActionDao extends AbstractItemDao implements RuleBasedCouponActionDao
{

	private static final String GET_RULE_BASED_COUPON_ACTION_BY_ORDER_QUERY = "SELECT {action.pk} " + "FROM   {"
			+ PromotionResultModel._TYPECODE + " AS pr" + " JOIN " + OrderModel._TYPECODE + " AS order ON {pr.order}={order.pk} " // NOSONAR
			+ " JOIN " + RuleBasedPromotionModel._TYPECODE + " AS promotion ON {promotion.pk}={pr.promotion} " + " JOIN " // NOSONAR
			+ RuleBasedAddCouponActionModel._TYPECODE + " AS action ON {action.promotionResult}={pr.pk}}" + "WHERE  {order"
			+ "} =?order";

	@Override
	public List<RuleBasedAddCouponActionModel> findRuleBasedCouponActionByOrder(final OrderModel order)
	{
		validateParameterNotNullStandardMessage("order model", order);
		final Map<String, Object> params = Collections.singletonMap("order", order);
		return getFlexibleSearchService()
				.<RuleBasedAddCouponActionModel> search(GET_RULE_BASED_COUPON_ACTION_BY_ORDER_QUERY, params).getResult();
	}

}
