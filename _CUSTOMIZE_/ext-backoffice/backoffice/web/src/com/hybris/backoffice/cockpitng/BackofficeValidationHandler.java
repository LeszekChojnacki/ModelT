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
package com.hybris.backoffice.cockpitng;

import java.util.List;

import com.hybris.backoffice.cockpitng.dataaccess.facades.object.validation.BackofficeValidationService;
import com.hybris.cockpitng.validation.ValidationContext;
import com.hybris.cockpitng.validation.ValidationHandler;
import com.hybris.cockpitng.validation.model.ValidationInfo;


/**
 * Platform specific implementation of {@link ValidationHandler} that delegates to {@link BackofficeValidationService}.
 */
public class BackofficeValidationHandler implements ValidationHandler

{
	private BackofficeValidationService validationService;

	public BackofficeValidationService getValidationService()
	{
		return validationService;
	}

	public void setValidationService(final BackofficeValidationService validationService)
	{
		this.validationService = validationService;
	}

	@Override
	public List<ValidationInfo> validate(final Object o, final ValidationContext validationContext)
	{
		return validationService.validate(o, validationContext);
	}

	@Override
	public List<ValidationInfo> validate(final Object o, final List<String> list, final ValidationContext validationContext)
	{
		return validationService.validate(o, list, validationContext);
	}
}
