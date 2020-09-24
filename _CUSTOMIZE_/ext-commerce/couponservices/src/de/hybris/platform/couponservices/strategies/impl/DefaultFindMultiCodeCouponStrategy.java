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
import de.hybris.platform.couponservices.model.MultiCodeCouponModel;
import de.hybris.platform.couponservices.services.CouponCodeGenerationService;
import de.hybris.platform.couponservices.strategies.FindCouponStrategy;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of the {@link FindCouponStrategy} interface for {@link MultiCodeCouponModel}
 */
public class DefaultFindMultiCodeCouponStrategy extends AbstractFindCouponStrategy
{

	private CouponCodeGenerationService couponCodeGenerationService;

	@Override
	protected String getCouponId(final String couponCode)
	{
		// for multi code coupons the coupon id is the prefix of the coupon code
		return getCouponCodeGenerationService().extractCouponPrefix(couponCode);
	}

	protected CouponCodeGenerationService getCouponCodeGenerationService()
	{
		return couponCodeGenerationService;
	}

	@Override
	protected Optional<AbstractCouponModel> couponValidation(final AbstractCouponModel coupon)
	{
		return (coupon instanceof MultiCodeCouponModel) ? super.couponValidation(coupon) : empty();
	}

	@Required
	public void setCouponCodeGenerationService(final CouponCodeGenerationService couponCodeGenerationService)
	{
		this.couponCodeGenerationService = couponCodeGenerationService;
	}
}
