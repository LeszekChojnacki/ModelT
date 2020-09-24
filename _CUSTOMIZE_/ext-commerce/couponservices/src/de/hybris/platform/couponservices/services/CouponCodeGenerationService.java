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

import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.couponservices.couponcodegeneration.CouponCodeGenerationException;
import de.hybris.platform.couponservices.model.MultiCodeCouponModel;

import java.util.Optional;


/**
 * The CouponCodeGenerationService provides methods to configure coupon code generation and create coupon codes. Each
 * coupon code has a clear-text part and a cipher-text part. The cipher-text part is calculated by encrypting the
 * clear-text part.
 */
public interface CouponCodeGenerationService
{
	/**
	 * generates a base64 encoded String representing the signature of the multi code coupon. The signature is used to
	 * generate the cipher-text part of the coupon code which incorporates the signed first half of the coupon code.
	 *
	 * @return base64 encoded String representing the signature of the multi code coupon
	 */
	String generateCouponSignature();

	/**
	 * generates the alphabet used for a coupon.
	 *
	 * @return alphabet string used for a coupon
	 */
	String generateCouponAlphabet();

	/**
	 * Generates a code for the given multi code coupon. Note: This method doesn't save the given coupon, but modifies it
	 * (by incrementing the {@code couponCodeNumber} attribute)
	 *
	 * @param coupon
	 *           multi code coupon
	 * @return code for the given multi code coupon
	 * @throws CouponCodeGenerationException
	 *            if the code cannot be generated (e.g. if the maximum number of coupon codes has been exceeded)
	 */
	String generateCouponCode(MultiCodeCouponModel coupon);

	/**
	 * tries to find the prefix substring of the given coupon code.
	 *
	 * @param couponCode
	 *           coupon code to extract prefix substring
	 * @return the (assumed) prefix of the given coupon code or null if none could be determined
	 */
	String extractCouponPrefix(final String couponCode);

	/**
	 * verifies if the given {@code couponCode} is valid for the given {@code coupon}.
	 *
	 * @param coupon
	 *           Coupon against which check will be fired
	 * @param couponCode
	 *           Coupon code to verify
	 * @return true if the couponCode is valid, otherwise false
	 */
	boolean verifyCouponCode(MultiCodeCouponModel coupon, String couponCode);

	/**
	 * verifies if the given string is a valid code separator
	 *
	 * @param codeSeparator
	 *           Code separator
	 * @return true if the codeSeperator is valid, otherwise false
	 */
	boolean isValidCodeSeparator(String codeSeparator);

	/**
	 * Generates codes for the given multi-code coupon. Returns a MediaModel containing a csv file with the generated
	 * codes. Note that the returned file contains {@code quantity} coupon codes or less. It contains less codes if the
	 * requested quantity cannot be generated (e.g. if the limit of codes has been reached). If no coupon codes could be
	 * generated an {@code Optional.empty()} is returned.
	 *
	 * @param multiCodeCoupon
	 *           Multi code coupon instance
	 * @param quantity
	 *           number of coupon codes to be generated
	 * @return MediaModel containing a csv file with the generated codes. If no coupon codes could be generated an
	 *         {@code Optional.empty()} is returned.
	 */
	Optional<MediaModel> generateCouponCodes(MultiCodeCouponModel multiCodeCoupon, int quantity);
}
