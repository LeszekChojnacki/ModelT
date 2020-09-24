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
package de.hybris.platform.couponservices.couponcodegeneration;

import de.hybris.platform.couponservices.model.MultiCodeCouponModel;

import java.util.Map;


/**
 * Interface to provide a unique method for generating a batch of coupon codes
 */
public interface CouponCodesGenerator
{

	/**
	 * generate next coupon code in the batch
	 *
	 * @param coupon
	 *           instance of {@link MultiCodeCouponModel} to generate coupons for
	 *
	 * @return next coupon code, null otherwise
	 */
	String generateNextCouponCode(MultiCodeCouponModel coupon);

	/**
	 * returns the ratio of clear-text to cipher-text based on total length of the coupon code.
	 *
	 * <pre>
	 * key: coupon code length.
	 * value: clear-text length.
	 * implicit: cipher-text length = (key - value)
	 * </pre>
	 */
	Map<Integer, Integer> getCodeLengthMapping();
}
