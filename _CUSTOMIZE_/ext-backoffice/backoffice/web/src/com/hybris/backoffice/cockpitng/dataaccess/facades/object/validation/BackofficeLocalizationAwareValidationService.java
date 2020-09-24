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

import java.util.Collection;
import java.util.List;

import com.hybris.cockpitng.validation.LocalizedQualifier;
import com.hybris.cockpitng.validation.ValidationContext;
import com.hybris.cockpitng.validation.model.ValidationInfo;


/**
 * {@inheritDoc} Additionally the service supports validation for properties of object for only selected set of locales.
 */
public interface BackofficeLocalizationAwareValidationService extends BackofficeValidationService
{
	/**
	 * Validate qualifiers for given object, for given locales, with defined validation context {@link ValidationContext}.
	 *
	 * @param objectToValidate
	 *           object to validate.
	 * @param localizedQualifiers
	 *           qualifiers.
	 * @param validationContext
	 *           validation context.
	 * @return list of {@link ValidationInfo} containing validation results
	 */
	List<ValidationInfo> validate(Object objectToValidate, Collection<LocalizedQualifier> localizedQualifiers,
			ValidationContext validationContext);
}
