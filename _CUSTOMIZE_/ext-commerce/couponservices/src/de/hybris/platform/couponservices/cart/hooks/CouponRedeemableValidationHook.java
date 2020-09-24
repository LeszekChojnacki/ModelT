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
package de.hybris.platform.couponservices.cart.hooks;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import static org.apache.commons.lang3.BooleanUtils.isFalse;

import de.hybris.platform.commerceservices.order.CommerceCartModification;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.commerceservices.strategies.hooks.CartValidationHook;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.couponservices.service.data.CouponResponse;
import de.hybris.platform.couponservices.services.CouponService;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Implementation of coupon specific hook to validate the applied coupon codes {@link CartValidationHook}
 */
public class CouponRedeemableValidationHook implements CartValidationHook
{
	private static final String COUPONNOTVALID = "couponNotValid";
	private CouponService couponService;

	@Override
	public void beforeValidateCart(final CommerceCartParameter parameter, final List<CommerceCartModification> modifications)
	{
		// nothing to do here
	}

	@Override
	public void afterValidateCart(final CommerceCartParameter parameter, final List<CommerceCartModification> modifications)
	{
		validateParameterNotNullStandardMessage("cart", parameter.getCart());

		final CartModel cartModel = parameter.getCart();
		if (CollectionUtils.isNotEmpty(cartModel.getAppliedCouponCodes()))
		{
			for (final String couponCode : cartModel.getAppliedCouponCodes())
			{
				final CouponResponse response = getCouponService().verifyCouponCode(couponCode, cartModel);
				if (isFalse(response.getSuccess()))
				{
					getCouponService().releaseCouponCode(couponCode, cartModel);
					final CommerceCartModification cartModificationData = new CommerceCartModification();
					cartModificationData.setStatusCode(COUPONNOTVALID);
					modifications.add(cartModificationData);
				}
			}
		}
	}

	protected CouponService getCouponService()
	{
		return couponService;
	}

	@Required
	public void setCouponService(final CouponService couponService)
	{
		this.couponService = couponService;
	}

}
