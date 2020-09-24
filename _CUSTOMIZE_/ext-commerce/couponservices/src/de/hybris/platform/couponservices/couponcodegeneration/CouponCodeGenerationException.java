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

/**
 * CouponCodeGenerationException indicates an exception related to coupon code generation.
 */
public class CouponCodeGenerationException extends RuntimeException
{

	/**
	 * default error code to be used in deprecated constructors
	 */
	public static final int DEFAULT_ERROR_CODE = 0;

	/**
	 * indicates that a coupon code could not be generated because the maximum number of allowed coupon codes has been
	 * exceeded.
	 */
	public static final int ERROR_MAXIMUM_COUPON_CODES_GENERATED = 500;

	private final int errorCode;

	public CouponCodeGenerationException(final String message, final int errorCode, final Throwable cause)
	{
		super(message, cause);
		this.errorCode = errorCode;
	}

	public CouponCodeGenerationException(final String message, final int errorCode)
	{
		super(message);
		this.errorCode = errorCode;
	}

	public int getErrorCode()
	{
		return errorCode;
	}
}
