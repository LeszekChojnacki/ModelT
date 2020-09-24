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
package de.hybris.platform.couponservices.strategies;

import de.hybris.platform.couponservices.model.AbstractCouponModel;

import java.util.Optional;


/**
 * The FindCouponStrategy interface provides a simple method to retrieve a coupon based on a given coupon code.
 */
public interface FindCouponStrategy
{
	/**
	 *
	 * Find the coupon for the provided {@code couponCode} without any validaton.
	 *
	 * @param couponCode
	 *           the coupon code (i.e. as entered in a storefront etc)
	 * @return AbstractCouponModel if a coupon model is found for the given coupon code.
	 */
	Optional<AbstractCouponModel> findCouponForCouponCode(final String couponCode);

	/**
	 * Find the coupon for the provided {@code couponCode} with the validation of the coupon
	 * throws CouponServiceException if the coupon Code is not active or not within the date range.
	 *
	 * @param couponCode
	 *           the coupon code (i.e. as entered in a storefront etc)
	 * @return AbstractCouponModel if a coupon model is found for the given coupon code.
	 */
	Optional<AbstractCouponModel> findValidatedCouponForCouponCode(final String couponCode);

}
