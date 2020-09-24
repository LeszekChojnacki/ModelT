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
package de.hybris.platform.adaptivesearch.strategies.impl;

import de.hybris.platform.adaptivesearch.strategies.AsValidationStrategy;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.validation.enums.Severity;
import de.hybris.platform.validation.exceptions.HybrisConstraintViolation;
import de.hybris.platform.validation.services.ValidationService;

import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Required;


/**
 * Default implementation of {@link AsValidationStrategy}.
 */
public class DefaultAsValidationStrategy implements AsValidationStrategy
{
	private ValidationService validationService;

	@Override
	public boolean isValid(final ItemModel model)
	{
		final Set<HybrisConstraintViolation> constraintViolations = validationService.validate(model,
				validationService.getActiveConstraintGroups());

		if (CollectionUtils.isNotEmpty(constraintViolations))
		{
			for (final HybrisConstraintViolation constraintViolation : constraintViolations)
			{
				if (constraintViolation.getViolationSeverity() == Severity.ERROR)
				{
					return false;
				}
			}
		}

		return true;
	}

	public ValidationService getValidationService()
	{
		return validationService;
	}

	@Required
	public void setValidationService(final ValidationService validationService)
	{
		this.validationService = validationService;
	}
}
