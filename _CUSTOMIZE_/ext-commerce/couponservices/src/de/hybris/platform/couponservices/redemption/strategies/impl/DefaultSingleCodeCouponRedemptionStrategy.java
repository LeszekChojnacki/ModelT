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
package de.hybris.platform.couponservices.redemption.strategies.impl;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.couponservices.dao.CouponRedemptionDao;
import de.hybris.platform.couponservices.model.CouponRedemptionModel;
import de.hybris.platform.couponservices.model.SingleCodeCouponModel;
import de.hybris.platform.couponservices.redemption.strategies.CouponRedemptionStrategy;

import java.util.List;


/**
 * The strategy provides methods for checking the single code coupon capability of being redeemed.
 */
public class DefaultSingleCodeCouponRedemptionStrategy implements CouponRedemptionStrategy<SingleCodeCouponModel>
{
	private CouponRedemptionDao couponRedemptionDao;

	@Override
	public boolean isRedeemable(final SingleCodeCouponModel coupon, final AbstractOrderModel abstractOrder,
			final String couponCode)
	{
		return isCouponRedeemable(coupon, abstractOrder.getUser(), couponCode);
	}

	@Override
	public boolean isCouponRedeemable(final SingleCodeCouponModel coupon, final UserModel user, final String couponCode)
	{
		return nonNull(user) ? checkSingleCodeCouponRedeemableForUser(coupon, user) : checkSingleCodeCouponRedeemable(coupon);
	}

	protected boolean checkSingleCodeCouponRedeemableForUser(final SingleCodeCouponModel coupon, final UserModel user)
	{
		final List<CouponRedemptionModel> couponRedemptionsUser = getCouponRedemptionDao()
				.findCouponRedemptionsByCodeAndUser(coupon.getCouponId(), user);

		final int maxRedemptionsPerCustomer = isNull(coupon.getMaxRedemptionsPerCustomer()) ? Integer.MAX_VALUE
				: coupon.getMaxRedemptionsPerCustomer().intValue();

		boolean redeemable = false;

		if (couponRedemptionsUser.size() < maxRedemptionsPerCustomer)
		{
			redeemable = checkSingleCodeCouponRedeemable(coupon);
		}
		return redeemable;
	}

	protected boolean checkSingleCodeCouponRedeemable(final SingleCodeCouponModel coupon)
	{
		final int maxTotalRedemptions = isNull(coupon.getMaxTotalRedemptions()) ? Integer.MAX_VALUE
				: coupon.getMaxTotalRedemptions().intValue();

		final List<CouponRedemptionModel> couponRedemptionTotal = getCouponRedemptionDao()
				.findCouponRedemptionsByCode(coupon.getCouponId());
		return couponRedemptionTotal.size() < maxTotalRedemptions;
	}

	protected CouponRedemptionDao getCouponRedemptionDao()
	{
		return couponRedemptionDao;
	}

	public void setCouponRedemptionDao(final CouponRedemptionDao couponRedemptionDao)
	{
		this.couponRedemptionDao = couponRedemptionDao;
	}

}
