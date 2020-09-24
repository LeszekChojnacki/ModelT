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
package de.hybris.platform.couponservices.constants;

@SuppressWarnings("PMD")
public class CouponServicesConstants extends GeneratedCouponServicesConstants // NOSONAR
{

	public static final String EXTENSIONNAME = "couponservices";

	/**
	 * The property that stores which algorithm to use for coupon code generation
	 */
	public static final String COUPON_CODE_GENERATION_ALGORITHM_PROPERTY = "couponservices.code.generation.signature.algorithm";

	/**
	 * The default algorithm used for coupon code generation (AES). This is used for the ciphertext generation.
	 */
	public static final String COUPON_CODE_GENERATION_ALGORITHM_DEFAULT_VALUE = "AES";

	/**
	 * The property that stores the key size of the algorithm used for coupon code generation
	 */
	public static final String COUPON_CODE_GENERATION_KEY_SIZE_PROPERTY = "couponservices.code.generation.signature.keysize";

	/**
	 * The default key size (128). Note: If you want to increase the keysize for AES to 192 or 256 you need to install
	 * the Java Cryptography Extension (JCE)
	 */
	public static final int COUPON_CODE_GENERATION_KEY_SIZE_DEFAULT_VALUE = 128;

	/**
	 * The property that stores the global character set containing all characters that can be used for coupon codes.
	 * From this a randomly shuffled coupon alphabet is selected when a new coupon is created.
	 */
	public static final String COUPON_CODE_GENERATION_GLOBAL_CHARACTERSET_PROPERTY = "couponservices.code.generation.global.characterset";

	/**
	 * The default character set containing all characters that can be used for coupon codes.
	 */
	public static final String COUPON_CODE_GENERATION_GLOBAL_CHARACTERSET_DEFAULT_VALUE = "123456789ABCDEFGHKLMNPRSTWXYZ";

	/**
	 * The property that stores the alphabet length for each coupon.
	 */
	public static final String COUPON_CODE_GENERATION_ALPHABET_LENGTH_PROPERTY = "couponservices.code.generation.alphabet.length";

	/**
	 * The property that stores the media folder qualifier for generated coupons.
	 */
	public static final String COUPON_CODE_GENERATION_MEDIA_FOLDER_QUALIFIER = "couponservices.code.generation.media.folder.qualifier";
	/**
	 * The default media folder qualifier used for coupon code generation.
	 */
	public static final String COUPON_CODE_GENERATION_MEDIA_FOLDER_QUALIFIER_DEFAULT_VALUE = "couponcodes";
	/**
	 * The default alphabet length (16). Note that the default implementation for coupon code generation only supports
	 * the default value.
	 */
	public static final int COUPON_CODE_GENERATION_ALPHABET_LENGTH_DEFAULT_VALUE = 16;

	/**
	 * The property that defines whether to use a SecureRandom (vs. a regular java.util.Random) for randomly picking an
	 * alphabet for each coupon. Setting this property to {@code true} results in using SecureRandom. Note that if your
	 * hybris installation runs on a VM, using SecureRandom requires special setup in order to avoid poor performance.
	 */
	public static final String COUPON_CODE_GENERATION_PICK_ALPHABET_USING_SECURERANDOM_PROPERTY = "couponservices.code.generation.pick.alphabet.using.securerandom";
	/**
	 * The property that defines the prefix regex used for coupon code generation.
	 */
	public static final String COUPON_CODE_GENERATION_PREFIX_REGEX_PROPERTY = "couponservices.code.generation.prefix.regexp";
	/**
	 * The default prefix regex used for coupon code generation.
	 */
	public static final String COUPON_CODE_GENERATION_PREFIX_REGEX_DEFAULT_VALUE = "[A-Za-z0-9]+";
	/**
	 * The property that defines global characterset allows multi byte characters used for coupon code generation.
	 */
	public static final String COUPON_CODE_GENERATION_ALLOW_MULTIBYTE_CHARACTERS_PROPERTY = "couponservices.code.generation.global.characterset.allow.multibyte.characters";
	/**
	 * The property that defines signature algorithm allows non aes used for coupon code generation.
	 */
	public static final String COUPON_CODE_GENERATION_SIGNATURE_ALGORITHM_ALLOW_NON_AES_PROPERTY = "couponservices.code.generation.signature.algorithm.allow.non-aes";
	/**
	 * The property that defines alphabet allows variable length used for coupon code generation.
	 */
	public static final String COUPON_CODE_GENERATION_ALPHABET_ALLOW_VARIABLE_LENGTH_PROPERTY = "couponservices.code.generation.alphabet.allow.variable.length";
	/**
	 * The property that defines invalid coupon code error message code used while coupon code redemption.
	 */
	public static final String COUPON_CODE_INVALID_ERROR_CODE = "coupon.invalid.code.provided";
	/**
	 * The property that defines coupon code has already been redeemed error message code used while coupon code
	 * redemption.
	 */
	public static final String COUPON_CODE_ALREADY_REDEEMED_ERROR_CODE = "coupon.already.redeemed";
	/**
	 * The property that defines order recalculation error message code used while coupon code redemption.
	 */
	public static final String COUPON_ORDER_RECALCULATION_ERROR_CODE = "coupon.order.recalculation.error";
	/**
	 * The property that defines coupon code expired or not active error message code used while coupon code redemption.
	 */
	public static final String COUPON_CODE_EXPIRED_ERROR_CODE = "coupon.not.active.expired";
	/**
	 * The property that defines coupon code already applied error message code used while coupon code redemption.
	 */
	public static final String COUPON_CODE_ALREADY_EXISTS_ERROR_CODE = "coupon.already.exists.cart";
	/**
	 * The property that defines coupon general error code used while coupon code redemption.
	 */
	public static final String COUPON_GENERAL_ERROR_CODE = "coupon.general.error";

	/*
	 * Rule metadata with this identifier is generated by rule translator to keep rule coupon Ids.
	 */
	public static final String COUPON_IDS = "couponIds";

	private CouponServicesConstants()
	{
		//empty
	}


}
