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

import static org.springframework.validation.ValidationUtils.rejectIfEmpty;

import de.hybris.platform.couponwebservices.dto.CouponStatusWsDTO;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


/**
 * Single-code coupon validator for WS
 */
public class CouponStatusWsDTOValidator implements Validator
{

	@Override
	public boolean supports(final Class<?> clazz)
	{
		return CouponStatusWsDTO.class.equals(clazz);
	}

	@Override
	public void validate(final Object target, final Errors errors)
	{
		rejectIfEmpty(errors, "couponId", "field.required");
		rejectIfEmpty(errors, "active", "field.required");
	}
}
