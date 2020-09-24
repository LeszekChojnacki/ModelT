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

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.nonNull;

import de.hybris.platform.couponservices.model.AbstractCouponModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;

import java.util.Calendar;


/**
 * The general coupon validation interceptor
 */
public class AbstractCouponValidateInterceptor implements ValidateInterceptor<AbstractCouponModel>
{
	@Override
	public void onValidate(final AbstractCouponModel coupon, final InterceptorContext ctx) throws InterceptorException
	{
		checkArgument(nonNull(coupon), "Coupon model cannot be NULL here");

		if (nonNull(coupon.getEndDate()) && coupon.getEndDate().before(Calendar.getInstance().getTime()))
		{
			throw new CouponInterceptorException("End date cannot be in the past");
		}
		if (nonNull(coupon.getStartDate()) && nonNull(coupon.getEndDate()) && coupon.getStartDate().after(coupon.getEndDate()))
		{
			throw new CouponInterceptorException("Illegal value of startDate or endDate: endDate should be after startDate.");
		}
	}
}