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
 * Provides methods for managing coupons, like verifying if coupon code is valid or finding all coupons.
 */
public interface CouponManagementService
{
	/**
	 * Returns Coupon Model object for a given coupon code without validation
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
	 * Verify if given coupon code is valid.
	 *
	 * @param couponCode
	 *           Coupon code to check
	 * @param abstractOrder
	 *           {@link AbstractOrderModel} for which the coupon will be redeemed
	 * @return {@link CouponResponse} containing true if coupon code is valid or false if not, including the message why
	 *         it is not valid.
	 */
	CouponResponse verifyCouponCode(String couponCode, AbstractOrderModel abstractOrder);

	/**
	 * Verify if given coupon code is valid.
	 *
	 * @param couponCode
	 *           Coupon code to check
	 * @param user
	 *           {@link UserModel} for which the coupon will be redeemed
	 * @return {@link CouponResponse} containing true if coupon code is valid or false if not, including the message why
	 *         it is not valid.
	 */
	CouponResponse validateCouponCode(String couponCode, UserModel user);

	/**
	 * Releases coupon provided in {@code couponCode}.
	 *
	 * @param couponCode
	 *           Coupon code to release
	 */
	void releaseCouponCode(String couponCode);

	/**
	 * Redeems coupon (called when coupon code is applied in cart). throws CouponServiceException when verify coupon code
	 * fails
	 *
	 * @param couponCode
	 *           Coupon code to redeem
	 * @param cart
	 *           {@link CartModel} to redeem coupon
	 * @return boolean true if coupon code could be redeemed at cart or false if not.
	 *
	 */
	boolean redeem(String couponCode, CartModel cart);

	/**
	 * Redeems coupon (called when order is placed).
	 *
	 * @param couponCode
	 *           Coupon code to redeem
	 * @param order
	 *           {@link OrderModel} to redeem coupon
	 * @return CouponResponse
	 *
	 */
	CouponResponse redeem(String couponCode, OrderModel order);
}
