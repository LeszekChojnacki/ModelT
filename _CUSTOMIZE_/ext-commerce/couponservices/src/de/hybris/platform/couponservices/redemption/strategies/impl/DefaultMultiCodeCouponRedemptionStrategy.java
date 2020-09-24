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

import static de.hybris.platform.couponservices.constants.CouponServicesConstants.COUPON_CODE_ALREADY_REDEEMED_ERROR_CODE;
import static de.hybris.platform.couponservices.constants.CouponServicesConstants.COUPON_CODE_INVALID_ERROR_CODE;

import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.couponservices.CouponServiceException;
import de.hybris.platform.couponservices.dao.CouponRedemptionDao;
import de.hybris.platform.couponservices.model.MultiCodeCouponModel;
import de.hybris.platform.couponservices.redemption.strategies.CouponRedemptionStrategy;
import de.hybris.platform.couponservices.services.CouponCodeGenerationService;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * The strategy provides methods for checking the multicode coupon capability of being redeemed.
 */
public class DefaultMultiCodeCouponRedemptionStrategy implements CouponRedemptionStrategy<MultiCodeCouponModel>
{

	private CouponCodeGenerationService couponCodeGenerationService;

	private CouponRedemptionDao couponRedemptionDao;

	private static final Logger LOG = LoggerFactory.getLogger(DefaultMultiCodeCouponRedemptionStrategy.class);

	@Override
	public boolean isRedeemable(final MultiCodeCouponModel coupon, final AbstractOrderModel abstractOrder, final String couponCode)
	{

		return checkMultiCodeCouponRedeemable(coupon, couponCode);
	}

	@Override
	public boolean isCouponRedeemable(final MultiCodeCouponModel coupon, final UserModel user, final String couponCode)
	{
		return checkMultiCodeCouponRedeemable(coupon, couponCode);
	}

	protected boolean checkMultiCodeCouponRedeemable(final MultiCodeCouponModel coupon, final String couponCode)
	{
		if (getCouponCodeGenerationService().verifyCouponCode(coupon, couponCode))
		{
			if (CollectionUtils.isEmpty(getCouponRedemptionDao().findCouponRedemptionsByCode(couponCode)))
			{
				return true;
			}
			throw new CouponServiceException(COUPON_CODE_ALREADY_REDEEMED_ERROR_CODE);
		}
		LOG.error("Cannot verify the coupon {} for entered coupon code", coupon.getName());
		throw new CouponServiceException(COUPON_CODE_INVALID_ERROR_CODE);
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

	protected CouponRedemptionDao getCouponRedemptionDao()
	{
		return couponRedemptionDao;
	}

	@Required
	public void setCouponRedemptionDao(final CouponRedemptionDao couponRedemptionDao)
	{
		this.couponRedemptionDao = couponRedemptionDao;
	}
}
