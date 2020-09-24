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
package de.hybris.platform.couponwebservices.util;

import static java.util.Objects.isNull;

import de.hybris.platform.couponservices.dao.CouponDao;
import de.hybris.platform.couponservices.model.AbstractCouponModel;
import de.hybris.platform.couponservices.model.MultiCodeCouponModel;
import de.hybris.platform.couponservices.model.SingleCodeCouponModel;
import de.hybris.platform.couponwebservices.CouponNotFoundException;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Required;


/**
 * Some common utility methods.
 *
 */
public class CouponWsUtils
{
	private static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

	private CouponDao couponDao;

	/**
	 * Returns mapping function from {@link String} (in ISO date time format) into {@link Date} (at UTC time zone)
	 */
	public Function<String, Date> getStringToDateMapper()
	{
		return s -> Date.from(LocalDateTime.parse(s, DATE_TIME_FORMATTER).atZone(UTC_ZONE_ID).toInstant());
	}

	/**
	 * Returns mapping function from {@link Date} (at UTC time zone) into {@link String} (in ISO date time format)
	 */
	public Function<Date, String> getDateToStringMapper()
	{
		return d -> LocalDateTime.ofInstant(d.toInstant(), UTC_ZONE_ID).format(DATE_TIME_FORMATTER);
	}

	/**
	 * Given the couponModel and couponId string, checks for instance of SingleCode Coupon, otherwise throws exception
	 *
	 * @param couponModel
	 *
	 * @param couponId
	 *
	 */
	public void assertValidSingleCodeCoupon(final AbstractCouponModel couponModel, final String couponId)
	{
		if (isNull(couponModel) || !(couponModel instanceof SingleCodeCouponModel))
		{
			throw new CouponNotFoundException("No single code coupon was found for code [" + couponId + "]");
		}
	}

	/**
	 * Given the couponModel and couponId string, checks for instance of MultiCode Coupon, otherwise throws exception
	 *
	 * @param couponModel
	 *
	 * @param couponId
	 */
	public void assertValidMultiCodeCoupon(final AbstractCouponModel couponModel, final String couponId)
	{
		if (isNull(couponModel) || !(couponModel instanceof MultiCodeCouponModel))
		{
			throw new CouponNotFoundException("No multi-code coupon was found for code [" + couponId + "]");
		}
	}

	/**
	 * Given the couponId string, checks for instance of MultiCode Coupon, otherwise throws exception
	 *
	 * @param couponId
	 *           a string to be used to find MultiCode coupon
	 * @return - an instance of MultiCodeCouponModel
	 */
	public MultiCodeCouponModel getValidMultiCodeCoupon(final String couponId)
	{
		final AbstractCouponModel abstractCouponModel = getCouponById(couponId);
		assertValidMultiCodeCoupon(abstractCouponModel, couponId);
		return (MultiCodeCouponModel) abstractCouponModel;
	}

	/**
	 * Given the couponId string, checks for instance of Coupon, otherwise throws exception
	 *
	 * @param couponId
	 *           a string to be used to find Coupon
	 * @return AbstractCouponModel - an instance of AbstractCouponModel
	 */
	public AbstractCouponModel getCouponById(final String couponId)
	{
		AbstractCouponModel abstractCouponModel = null;
		try
		{
			abstractCouponModel = getCouponDao().findCouponById(couponId);
		}
		catch (final ModelNotFoundException ex)
		{
			throw new CouponNotFoundException("No coupon found for couponId [" + couponId + "]", "invalid", "couponId");
		}
		return abstractCouponModel;
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

}
