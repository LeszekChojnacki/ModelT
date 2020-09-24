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
package de.hybris.platform.couponservices.services.impl;

import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.voucher.impl.DefaultVoucherService;

import java.util.Collection;
import java.util.Collections;


/**
 * Extends DefaultVoucherService for coupon specific logic. It overrides getAppliedVoucherCodes in order to use codes
 * stored in attribute AbstractOrderModel.couponCodes.
 */
public class CouponVoucherService extends DefaultVoucherService
{
	@Override
	public Collection<String> getAppliedVoucherCodes(final CartModel cart)
	{
		return cart.getAppliedCouponCodes() == null ? Collections.emptyList() : cart.getAppliedCouponCodes();
	}

	@Override
	public Collection<String> getAppliedVoucherCodes(final OrderModel order)
	{
		return order.getAppliedCouponCodes() == null ? Collections.emptyList() : order.getAppliedCouponCodes();
	}
}
