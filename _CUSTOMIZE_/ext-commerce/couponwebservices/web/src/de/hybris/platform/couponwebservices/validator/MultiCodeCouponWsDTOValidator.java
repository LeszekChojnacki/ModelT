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

import de.hybris.platform.couponwebservices.dto.MultiCodeCouponWsDTO;

import org.springframework.validation.Errors;


/**
 * Multi-code coupon validator for WS
 */
public class MultiCodeCouponWsDTOValidator extends AbstractCouponWsDTOValidator<MultiCodeCouponWsDTO>
{

	@Override
	protected void addValidation(final Object target, final Errors errors)
	{
		rejectIfEmpty(errors, "codeGenerationConfiguration", "field.required");
	}

	@Override
	protected Class<MultiCodeCouponWsDTO> getSupportingClass()
	{
		return MultiCodeCouponWsDTO.class;
	}

}
