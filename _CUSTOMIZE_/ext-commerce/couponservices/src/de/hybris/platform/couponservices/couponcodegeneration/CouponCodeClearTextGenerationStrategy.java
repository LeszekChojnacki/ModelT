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
import de.hybris.platform.servicelayer.exceptions.SystemException;



/**
 * Interface for generating the clear-text part of a coupon code.
 */
public interface CouponCodeClearTextGenerationStrategy
{
	/**
	 * returns the generated clear-text part of the coupon code with the given length
	 *
	 * @throws CouponCodeGenerationException
	 *            if the maximum number of possible codes has already been generated for the given coupon and length
	 */
	String generateClearText(MultiCodeCouponModel coupon, int length);

	/**
	 * returns the original coupon code number the given clearText was based on. (This is the inverse function to the
	 * {@link #generateClearText(MultiCodeCouponModel, int)} method)
	 *
	 * @throws SystemException
	 *            if the coupon code number cannot be computed (e.g. if the clearText contains characters not in the
	 *            coupon's alphabet)
	 */
	long getCouponCodeNumberForClearText(MultiCodeCouponModel coupon, String clearText);
}