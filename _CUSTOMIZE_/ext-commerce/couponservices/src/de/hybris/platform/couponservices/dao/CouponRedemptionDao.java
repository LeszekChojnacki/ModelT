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

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.couponservices.model.CouponRedemptionModel;

import java.util.List;


/**
 * Data Access Object for CouponRedemptionModel.
 */
public interface CouponRedemptionDao
{
	/**
	 * Finds the List of CouponRedemptionModel by its couponCode.
	 *
	 * @param couponCode
	 *           the couponCode
	 *
	 * @return CouponRedemptionModel List by its couponCode.
	 */
	List<CouponRedemptionModel> findCouponRedemptionsByCode(final String couponCode);

	/**
	 * Finds the List of CouponRedemptionModel by its couponCode and abstractOrder.
	 *
	 * @param couponCode
	 *           the couponCode
	 * @param abstractOrder
	 *
	 * @return CouponRedemptionModel List by its couponCode and abstractOrder..
	 */
	List<CouponRedemptionModel> findCouponRedemptionsByCodeAndOrder(final String couponCode,
			final AbstractOrderModel abstractOrder);

	/**
	 * Finds the List of CouponRedemptionModel by its couponCode and user.
	 *
	 * @param couponCode
	 *           the couponCode
	 * @param user
	 *           the user redeeming the coupon
	 *
	 * @return CouponRedemptionModel List by its couponCode and user.
	 */
	List<CouponRedemptionModel> findCouponRedemptionsByCodeAndUser(final String couponCode, final UserModel user);

	/**
	 * Finds the List of CouponRedemptionModel by its couponCode.
	 *
	 * @param couponCode
	 *           the couponCode
	 *
	 * @param abstractOrder
	 *
	 * @param user
	 *           the user redeeming the coupon
	 *
	 * @return CouponRedemptionModel List by its couponCode and order and user.
	 */
	List<CouponRedemptionModel> findCouponRedemptionsByCodeOrderAndUser(final String couponCode,
			final AbstractOrderModel abstractOrder, final UserModel user);

}
