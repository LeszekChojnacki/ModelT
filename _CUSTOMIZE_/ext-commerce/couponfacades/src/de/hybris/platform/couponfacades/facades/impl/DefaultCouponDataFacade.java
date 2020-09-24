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
package de.hybris.platform.couponfacades.facades.impl;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

import de.hybris.platform.commercefacades.coupon.CouponDataFacade;
import de.hybris.platform.commercefacades.coupon.data.CouponData;
import de.hybris.platform.couponservices.model.AbstractCouponModel;
import de.hybris.platform.couponservices.services.CouponService;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Required;


/**
 * Default Implementation of {@link CouponDataFacade}
 */
public class DefaultCouponDataFacade implements CouponDataFacade
{
	private CouponService couponService;
	private Converter<AbstractCouponModel, CouponData> couponConverter;

	@Override
	public Optional<CouponData> getCouponDetails(final String couponCode)
	{
		validateParameterNotNullStandardMessage("coupon code", couponCode);

		return getCouponService().getCouponForCode(couponCode).map(getCouponConverter()::convert).map(couponData ->
		{
			couponData.setCouponCode(couponCode);
			return couponData;
		});
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

	protected Converter<AbstractCouponModel, CouponData> getCouponConverter()
	{
		return couponConverter;
	}

	@Required
	public void setCouponConverter(final Converter<AbstractCouponModel, CouponData> couponConverter)
	{
		this.couponConverter = couponConverter;
	}

}
