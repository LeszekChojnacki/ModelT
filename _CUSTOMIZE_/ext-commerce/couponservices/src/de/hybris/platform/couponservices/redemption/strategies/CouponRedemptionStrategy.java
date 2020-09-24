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
package de.hybris.platform.couponservices.redemption.strategies;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.couponservices.model.AbstractCouponModel;


/**
 * The strategy interface provides methods for checking the coupon capability of being redeemed.
 */
public interface CouponRedemptionStrategy<T extends AbstractCouponModel>
{
	/**
	 * Check the whether coupon meets the conditions required in order to be redeemed.
	 *
	 * @param coupon
	 *           {@link AbstractCouponModel} coupon to redeem
	 * @param abstractOrder
	 *           {@link AbstractOrderModel} to redeem coupon
	 * @param code
	 *           the coupon code to check
	 *
	 * @return boolean true if coupon could be redeem at cart/order or false if it cannot be redeemed.
	 */
	boolean isRedeemable(final T coupon, final AbstractOrderModel abstractOrder, final String code);

	/**
	 * Check the whether coupon meets the conditions required in order to be redeemed.
	 *
	 * @param coupon
	 *           {@link AbstractCouponModel} coupon to redeem
	 * @param user
	 *           {@link UserModel} to redeem coupon
	 * @param code
	 *           the coupon code to check
	 *
	 * @return boolean true if coupon could be redeem at cart/order or false if it cannot be redeemed.
	 */
	boolean isCouponRedeemable(final T coupon, final UserModel user, final String code);
}
