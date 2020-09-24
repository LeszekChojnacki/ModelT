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

import de.hybris.platform.couponservices.model.MultiCodeCouponModel;
import de.hybris.platform.couponservices.services.CouponCodeGenerationService;
import de.hybris.platform.servicelayer.interceptor.InitDefaultsInterceptor;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;

import org.springframework.beans.factory.annotation.Required;


/**
 * The MultiCodeCouponInitDefaultsInterceptor initializes the internal attributes required for coupon code generation.
 */
public class MultiCodeCouponInitDefaultsInterceptor implements InitDefaultsInterceptor<MultiCodeCouponModel>
{

	private CouponCodeGenerationService couponCodeGenerationService;

	@Override
	public void onInitDefaults(final MultiCodeCouponModel model, final InterceptorContext ctx) throws InterceptorException
	{
		// set the signature and the alphabet used for coupon code generation
		model.setSignature(getCouponCodeGenerationService().generateCouponSignature());
		model.setAlphabet(getCouponCodeGenerationService().generateCouponAlphabet());
	}

	protected CouponCodeGenerationService getCouponCodeGenerationService()
	{
		return couponCodeGenerationService;
	}

	@Required
	public void setCouponCodeGenerationService(final CouponCodeGenerationService couponCodeGenerationService)
	{
		this.couponCodeGenerationService = couponCodeGenerationService;
	}


}
