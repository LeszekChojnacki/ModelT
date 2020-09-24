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
package de.hybris.platform.couponwebservices.facades;

import de.hybris.platform.core.model.media.MediaModel;

import java.util.Collection;
import java.util.Optional;


/**
 * Facade for coupon code generation WS
 */
public interface CouponCodeGenerationWsFacade
{

	/**
	 * Generate {@code batchsize} coupon codes
	 *
	 * @param couponId
	 *           Coupon id of the multi-code coupon
	 * @param batchsize
	 *           the size of the generated coupon batch
	 *
	 * @return The (optional) reference to MediaModel instance with generated coupons
	 *
	 */
	Optional<MediaModel> generateCouponCodes(String couponId, int batchsize);


	/**
	 * Get the collection of generated coupon codes for a given multi-code coupon
	 *
	 * @param couponId
	 *           Coupon id of the multi-code coupon
	 *
	 * @return a collection of Media with generated coupons
	 */
	Collection<MediaModel> getCouponCodeBatches(String couponId);

	/**
	 * Get the octet bytes array of generated coupon codes for a given multi-code coupon
	 *
	 * @param couponId
	 *           Coupon id of the multi-code coupon
	 * 
	 * @param batchCode
	 *           The string corresponding to media model code
	 * 
	 * @return a collection of Media with generated coupons
	 */
	byte[] getCouponCodes(String couponId, String batchCode);

}
