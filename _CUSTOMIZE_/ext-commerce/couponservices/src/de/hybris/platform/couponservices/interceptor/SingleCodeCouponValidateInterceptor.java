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
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang.BooleanUtils.isTrue;

import de.hybris.platform.couponservices.model.SingleCodeCouponModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.ValidateInterceptor;


/**
 * validation interceptor for Single code coupon
 */
public class SingleCodeCouponValidateInterceptor implements ValidateInterceptor<SingleCodeCouponModel>
{
	@Override
	public void onValidate(final SingleCodeCouponModel coupon, final InterceptorContext ctx) throws InterceptorException
	{
		checkArgument(nonNull(coupon), "Coupon model cannot be NULL here");
		checkArgument(nonNull(coupon.getCouponId()), "CouponId must be specified");

		if (!ctx.isNew(coupon) && isTrue(coupon.getActive()) && ctx.isModified(coupon, SingleCodeCouponModel.COUPONID))
		{
			throw new CouponInterceptorException("CouponId cannot be modified if coupon is active");
		}

		validateMaxRedemptionsPerCustomer(coupon);
	}

	/**
	 * @throws CouponInterceptorException
	 *            throws exception if MaxRedemptionsPerCustomer are less than 0 or greater than max allowed redemption
	 */
	protected void validateMaxRedemptionsPerCustomer(final SingleCodeCouponModel coupon) throws CouponInterceptorException
	{
		final int maxRedemptionsPerCustomer = isNull(coupon.getMaxRedemptionsPerCustomer()) ? Integer.MAX_VALUE
				: coupon.getMaxRedemptionsPerCustomer().intValue();
		final int maxTotalRedemptions = isNull(coupon.getMaxTotalRedemptions()) ? Integer.MAX_VALUE
				: coupon.getMaxTotalRedemptions().intValue();

		if (maxRedemptionsPerCustomer < 1)
		{
			throw new CouponInterceptorException("MaxRedemptionsPerCustomer should be greater than 0");
		}
		if (nonNull(coupon.getMaxRedemptionsPerCustomer()) && nonNull(coupon.getMaxTotalRedemptions())
				&& maxRedemptionsPerCustomer > maxTotalRedemptions)
		{
			throw new CouponInterceptorException("MaxRedemptionsPerCustomer should not be greater than maxTotalRedemptions");
		}
	}
}
