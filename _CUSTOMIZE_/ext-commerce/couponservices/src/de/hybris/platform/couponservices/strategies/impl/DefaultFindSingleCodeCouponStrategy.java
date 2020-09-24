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
package de.hybris.platform.couponservices.strategies.impl;

import static java.util.Optional.empty;

import de.hybris.platform.couponservices.model.AbstractCouponModel;
import de.hybris.platform.couponservices.model.SingleCodeCouponModel;
import de.hybris.platform.couponservices.strategies.FindCouponStrategy;

import java.util.Optional;


/**
 * Default implementation of the {@link FindCouponStrategy} interface for {@link SingleCodeCouponModel}
 */
public class DefaultFindSingleCodeCouponStrategy extends AbstractFindCouponStrategy
{

	@Override
	protected String getCouponId(final String couponCode)
	{
		// for SingleCodeCoupons the coupon code is the coupon id
		return couponCode;
	}

	@Override
	protected Optional<AbstractCouponModel> couponValidation(final AbstractCouponModel coupon)
	{
		return (coupon instanceof SingleCodeCouponModel) ? super.couponValidation(coupon) : empty();
	}

}
