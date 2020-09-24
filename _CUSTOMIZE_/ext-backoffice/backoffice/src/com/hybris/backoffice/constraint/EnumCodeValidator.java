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
package com.hybris.backoffice.constraint;

import de.hybris.platform.core.HybrisEnumValue;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


public class EnumCodeValidator implements ConstraintValidator<EnumCode, HybrisEnumValue>
{
	private String code;

	@Override
	public void initialize(final EnumCode constraintAnnotation)
	{
		code = constraintAnnotation.code();
	}

	@Override
	public boolean isValid(final HybrisEnumValue value, final ConstraintValidatorContext context)
	{
		return value != null && value.getCode().equals(code);
	}
}