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
package de.hybris.platform.couponwebservices.validator;

import static java.util.Objects.nonNull;

import de.hybris.platform.couponwebservices.dto.AbstractCouponWsDTO;
import de.hybris.platform.couponwebservices.util.CouponWsUtils;

import java.time.format.DateTimeParseException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;


/**
 * abstract implementation of {@link Validator} for coupon Ws validation
 */
public abstract class AbstractCouponWsDTOValidator<T> implements Validator
{

	private CouponWsUtils couponWsUtils;

	private static final Logger LOG = LoggerFactory.getLogger(AbstractCouponWsDTOValidator.class);

	@Override
	public boolean supports(final Class<?> clazz)
	{
		return getSupportingClass().equals(clazz);
	}

	@Override
	public void validate(final Object target, final Errors errors)
	{
		ValidationUtils.rejectIfEmpty(errors, "couponId", "field.required");
		final AbstractCouponWsDTO couponDto = (AbstractCouponWsDTO) target;
		if (nonNull(couponDto.getStartDate()) && nonNull(couponDto.getEndDate()))
		{
			final Date startDate = getDateValue(errors, "startDate", couponDto.getStartDate());
			final Date endDate = getDateValue(errors, "endDate", couponDto.getEndDate());

			if (startDate != null && endDate != null && startDate.after(endDate))
			{
				errors.rejectValue("startDate", "inconsistent", "field.inconsistent");
			}

		}
		addValidation(target, errors);
	}

	protected Date getDateValue(final Errors errors, final String field, final String dateAsString)
	{
		Date date = null;
		try
		{
			date = getCouponWsUtils().getStringToDateMapper().apply(dateAsString);
		}
		catch (final DateTimeParseException e)
		{
			LOG.error(e.getMessage(), e);
			errors.rejectValue(field, "invalid", "field.invalid");
		}
		return date;
	}

	//NOPMD
	protected void addValidation(@SuppressWarnings("unused") final Object target, @SuppressWarnings("unused") final Errors errors)
	{
		// to be extended by sub classes
	}

	protected abstract Class<T> getSupportingClass();

	protected CouponWsUtils getCouponWsUtils()
	{
		return couponWsUtils;
	}

	@Required
	public void setCouponWsUtils(final CouponWsUtils couponWsUtils)
	{
		this.couponWsUtils = couponWsUtils;
	}

}
