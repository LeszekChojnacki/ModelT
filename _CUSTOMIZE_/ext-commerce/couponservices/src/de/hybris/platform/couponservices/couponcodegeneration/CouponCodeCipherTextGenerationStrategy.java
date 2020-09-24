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


/**
 * Interface for generating the cipher-text part of a multi code coupon code.
 */
public interface CouponCodeCipherTextGenerationStrategy
{
	/**
	 * returns the generated cipher-text part of the coupon code for the given clearText with the given length
	 */
	String generateCipherText(MultiCodeCouponModel coupon, String clearText, int length);

}
