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
package de.hybris.platform.couponservices.services;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.couponservices.model.AbstractCouponModel;
import de.hybris.platform.couponservices.service.data.CouponResponse;

import java.util.Optional;


/**
 * Coupon service interface. To be used by facade to query/manage the coupon lifecycle
 */
public interface CouponService
{

	/**
	 * Release the coupon code from given cart.
	 *
	 * @param couponCode
	 *           Coupon code to release
	 * @param order
	 *           {@link AbstractOrderModel} to release the coupon from
	 */
	void releaseCouponCode(final String couponCode, final AbstractOrderModel order);

	/**
	 * Verify if provided {@code couponCode} is valid.
	 *
	 * @param couponCode
	 *           coupon code to validate
	 * @param order
	 *           order for which coupon should be validated
	 * @return CouponResponse containing true if coupon code is valid or false if not.
	 */
	CouponResponse verifyCouponCode(final String couponCode, final AbstractOrderModel order);

	/**
	 * Validate if provided {@code couponCode} is valid.
	 *
	 * @param couponCode
	 *           coupon code to validate
	 * @param user
	 *           user for which coupon should be validated
	 * @return CouponResponse containing true if coupon code is valid or false if not.
	 */
	CouponResponse validateCouponCode(final String couponCode, final UserModel user);

	/**
	 * Returns Coupon Model object for a given coupon code
	 *
	 * @param couponCode
	 *           Coupon code to get coupon details for
	 * @return {@link AbstractCouponModel}
	 */
	Optional<AbstractCouponModel> getCouponForCode(final String couponCode);

	/**
	 * Returns Coupon Model object for a given coupon code with validation
	 *
	 * @param couponCode
	 *           Coupon code to get coupon details for
	 * @return {@link AbstractCouponModel}
	 */
	Optional<AbstractCouponModel> getValidatedCouponForCode(final String couponCode);

	/**
	 * Redeem the coupon code associated with the cart.
	 *
	 * @param couponCode
	 *           Coupon code to redeem
	 * @param cart
	 *           {@link CartModel} to redeem coupon
	 *
	 * @return CouponResponse containing true if coupon code has been redeem at cart or false if not.
	 */
	CouponResponse redeemCoupon(final String couponCode, final CartModel cart);

	/**
	 * Create a coupon redemption instance for the coupon redeemed for the order.
	 *
	 * @param couponCode
	 *           Coupon code to redeem
	 * @param order
	 *           {@link OrderModel} to redeem coupon
	 *
	 * @return CouponResponse containing true when coupon redemption instance has been created if a coupon has been
	 *         redeemed successfully.
	 */
	CouponResponse redeemCoupon(final String couponCode, final OrderModel order);
}
