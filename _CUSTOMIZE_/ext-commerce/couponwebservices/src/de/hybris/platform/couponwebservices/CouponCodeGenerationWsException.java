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
package de.hybris.platform.couponwebservices;

import de.hybris.platform.webservicescommons.errors.exceptions.WebserviceException;


/**
 * Exception to be thrown in the case of error during coupon generation
 */
public class CouponCodeGenerationWsException extends WebserviceException
{

	private static final String TYPE = "CouponCodeGenerationWsException";
	private static final String SUBJECT_TYPE = "state";

	public CouponCodeGenerationWsException(final String message)
	{
		super(message);
	}

	public CouponCodeGenerationWsException(final String message, final String reason)
	{
		super(message, reason);

	}

	public CouponCodeGenerationWsException(final String message, final String reason, final Throwable cause)
	{
		super(message, reason, cause);

	}

	public CouponCodeGenerationWsException(final String message, final String reason, final String subject)
	{
		super(message, reason, subject);
	}

	public CouponCodeGenerationWsException(final String message, final String reason, final String subject, final Throwable cause)
	{
		super(message, reason, subject, cause);
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	public String getSubjectType()
	{
		return SUBJECT_TYPE;
	}

}