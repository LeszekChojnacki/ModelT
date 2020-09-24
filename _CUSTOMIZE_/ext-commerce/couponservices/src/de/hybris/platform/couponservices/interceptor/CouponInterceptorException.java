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
package de.hybris.platform.couponservices.interceptor;

import de.hybris.platform.servicelayer.interceptor.Interceptor;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;


public class CouponInterceptorException extends InterceptorException
{
	public CouponInterceptorException(final String message)
	{
		super(message, null, null);
	}

	public CouponInterceptorException(final String message, final Throwable cause)
	{
		super(message, cause, null);
	}

	public CouponInterceptorException(final String message, final Interceptor inter)
	{
		super(message, null, inter);
	}

	public CouponInterceptorException(final String message, final Throwable cause, final Interceptor inter)
	{
		super(message, cause, inter);
	}
}