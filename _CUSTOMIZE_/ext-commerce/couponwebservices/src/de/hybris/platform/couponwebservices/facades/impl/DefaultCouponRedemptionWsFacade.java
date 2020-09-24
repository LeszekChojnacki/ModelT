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
package de.hybris.platform.couponwebservices.facades.impl;

import static java.util.Objects.isNull;

import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.couponservices.dao.CouponDao;
import de.hybris.platform.couponservices.dao.CouponRedemptionDao;
import de.hybris.platform.couponservices.model.AbstractCouponModel;
import de.hybris.platform.couponservices.model.CouponRedemptionModel;
import de.hybris.platform.couponservices.model.SingleCodeCouponModel;
import de.hybris.platform.couponwebservices.CouponNotFoundException;
import de.hybris.platform.couponwebservices.dto.CouponRedemptionWsDTO;
import de.hybris.platform.couponwebservices.facades.CouponRedemptionWsFacade;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link CouponRedemptionWsFacade} for Coupon Redemption
 *
 */
public class DefaultCouponRedemptionWsFacade implements CouponRedemptionWsFacade
{
	private CouponDao couponDao;
	private CouponRedemptionDao couponRedemptionDao;
	private UserService userService;
	private Converter<SingleCodeCouponModel, CouponRedemptionWsDTO> couponRedemptionWsDTOConverter;

	@Override
	public CouponRedemptionWsDTO getSingleCodeCouponRedemption(final String couponId, final String customerId)
	{
		AbstractCouponModel abstractCouponModel = null;
		try
		{
			abstractCouponModel = getCouponDao().findCouponById(couponId);
		}
		catch (final ModelNotFoundException ex)
		{
			throw new CouponNotFoundException("No single code coupon found for invalid couponId [" + couponId + "]");
		}

		assertSingleCodeCoupon(abstractCouponModel, couponId);

		final CouponRedemptionWsDTO couponRedemptionWsDTO = getCouponRedemptionWsDTOConverter()
				.convert((SingleCodeCouponModel) abstractCouponModel);

		if (StringUtils.isNotEmpty(customerId))
		{
			final UserModel customer = getUserService().getUserForUID(customerId);
			final List<CouponRedemptionModel> couponRedemptionList = getCouponRedemptionDao()
					.findCouponRedemptionsByCodeAndUser(couponId, customer);
			couponRedemptionWsDTO.setCustomerId(customerId);
			couponRedemptionWsDTO.setRedemptionsPerCustomer(couponRedemptionList.size());
		}
		return couponRedemptionWsDTO;
	}

	protected void assertSingleCodeCoupon(final AbstractCouponModel couponModel, final String couponId)
	{
		if (isNull(couponModel) || !(couponModel instanceof SingleCodeCouponModel))
		{
			throw new CouponNotFoundException("No single code coupon was found for code [" + couponId + "]");
		}
	}

	protected CouponDao getCouponDao()
	{
		return couponDao;
	}

	@Required
	public void setCouponDao(final CouponDao couponDao)
	{
		this.couponDao = couponDao;
	}

	protected Converter<SingleCodeCouponModel, CouponRedemptionWsDTO> getCouponRedemptionWsDTOConverter()
	{
		return couponRedemptionWsDTOConverter;
	}

	@Required
	public void setCouponRedemptionWsDTOConverter(
			final Converter<SingleCodeCouponModel, CouponRedemptionWsDTO> couponRedemptionWsDTOConverter)
	{
		this.couponRedemptionWsDTOConverter = couponRedemptionWsDTOConverter;
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

	protected UserService getUserService()
	{
		return userService;
	}

	@Required
	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}
}
