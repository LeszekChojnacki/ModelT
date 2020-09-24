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

import static de.hybris.platform.couponservices.constants.CouponServicesConstants.COUPON_CODE_EXPIRED_ERROR_CODE;

import de.hybris.platform.couponservices.CouponServiceException;
import de.hybris.platform.couponservices.dao.CouponDao;
import de.hybris.platform.couponservices.model.AbstractCouponModel;
import de.hybris.platform.couponservices.strategies.FindCouponStrategy;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;

import java.util.Date;
import java.util.Optional;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;


/**
 * Abstract base class for all out of the box FindCouponStrategy implementations
 */
public abstract class AbstractFindCouponStrategy implements FindCouponStrategy
{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractFindCouponStrategy.class);

	private CouponDao couponDao;

	@Override
	public Optional<AbstractCouponModel> findCouponForCouponCode(final String couponCode)
	{
		return getCouponByCode(couponCode);
	}

	@Override
	public Optional<AbstractCouponModel> findValidatedCouponForCouponCode(final String couponCode)
	{
		final Optional<AbstractCouponModel> couponModel = getCouponByCode(couponCode);

		return couponModel.isPresent() ? couponValidation(couponModel.get()) : Optional.empty();
	}

	protected Optional<AbstractCouponModel> getCouponByCode(final String couponCode)
	{
		final String couponId = getCouponId(couponCode);
		if (StringUtils.isNotEmpty(couponId))
		{
			try
			{
				final AbstractCouponModel coupon = getCouponDao().findCouponById(couponId);

				return Optional.of(coupon);
			}
			catch (ModelNotFoundException | AmbiguousIdentifierException ex)
			{
				LOG.debug(ex.getMessage(), ex);
			}
		}
		return Optional.empty();
	}

	/**
	 * returns the couponId based on the given {@code couponCode}
	 */
	protected abstract String getCouponId(String couponCode);

	protected Optional<AbstractCouponModel> couponValidation(final AbstractCouponModel coupon)
	{
		if (isActive(coupon) && isWithinDateRange(coupon))
		{
			return Optional.of(coupon);
		}
		throw new CouponServiceException(COUPON_CODE_EXPIRED_ERROR_CODE);
	}

	protected boolean isActive(final AbstractCouponModel coupon)
	{
		return BooleanUtils.isTrue(coupon.getActive());
	}

	protected boolean isWithinDateRange(final AbstractCouponModel coupon)
	{
		final Date currentDate = new Date();
		return isStartDateBefore(currentDate, coupon.getStartDate()) && isEndDateAfter(currentDate, coupon.getEndDate());
	}

	protected boolean isStartDateBefore(final Date date, final Date startDate)
	{
		return startDate == null || date.getTime() >= startDate.getTime();
	}

	protected boolean isEndDateAfter(final Date date, final Date endDate)
	{
		return endDate == null || date.getTime() <= endDate.getTime();
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
