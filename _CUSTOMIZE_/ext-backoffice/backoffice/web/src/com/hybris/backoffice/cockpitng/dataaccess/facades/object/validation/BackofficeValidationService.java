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
package com.hybris.backoffice.cockpitng.dataaccess.facades.object.validation;

import java.util.List;

import com.hybris.cockpitng.validation.ValidationContext;
import com.hybris.cockpitng.validation.model.ValidationInfo;


/**
 * This service interface is responsible for executing validation of objects in the backoffice. It allows to obtain
 * lists of {@link ValidationInfo} objects representing the state of the conducted validation.
 */
public interface BackofficeValidationService
{
	/**
	 * Validate object with defined validation context {@link ValidationContext}.
	 *
	 * @param objectToValidate
	 *           object to validate.
	 * @param validationContext
	 *           validation context.
	 * @return list of {@ValidationInfo}.
	 */
	List<ValidationInfo> validate(final Object objectToValidate, final ValidationContext validationContext);

	/**
	 * Validate qualifiers for given object with defined validation context {@link ValidationContext}.
	 *
	 * @param objectToValidate
	 *           object to validate.
	 * @param qualifiers
	 *           qualifiers.
	 * @param validationContext
	 *           validation context.
	 * @return list of {@ValidationInfo}.
	 */
	List<ValidationInfo> validate(final Object objectToValidate, final List<String> qualifiers,
			final ValidationContext validationContext);
}
