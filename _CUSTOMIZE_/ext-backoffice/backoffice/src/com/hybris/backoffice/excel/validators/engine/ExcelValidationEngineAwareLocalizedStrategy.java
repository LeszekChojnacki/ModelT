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
package com.hybris.backoffice.excel.validators.engine;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.validation.exceptions.HybrisConstraintViolation;
import de.hybris.platform.validation.localized.LocalizedAttributeConstraint;
import de.hybris.platform.validation.localized.LocalizedConstraintsRegistry;
import de.hybris.platform.validation.services.ConstraintViolationFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ClassUtils;
import org.springframework.beans.factory.annotation.Required;

import com.hybris.backoffice.excel.data.ExcelAttribute;
import com.hybris.backoffice.excel.data.ImportParameters;
import com.hybris.backoffice.excel.validators.data.ExcelValidationResult;
import com.hybris.backoffice.excel.validators.data.ValidationMessage;


/**
 * Excel validation strategy for localized fields which uses validation engine.
 */
public class ExcelValidationEngineAwareLocalizedStrategy extends ExcelAbstractValidationEngineAwareStrategy
{

	private ConstraintViolationFactory violationFactory;
	private LocalizedConstraintsRegistry localizedConstraintsRegistry;

	@Override
	public boolean canHandle(final ImportParameters importParameters, final ExcelAttribute excelAttribute)
	{
		return excelAttribute.isLocalized();
	}

	@Override
	public ExcelValidationResult validate(final ImportParameters importParameters, final ExcelAttribute excelAttribute)
	{
		final Class<ItemModel> modelClass = getTypeService().getModelClass(importParameters.getTypeCode());
		final Collection<LocalizedAttributeConstraint> allConstraints = getAllAttributeConstraints(modelClass);
		final Collection<HybrisConstraintViolation> validationErrors = validateValue(importParameters, excelAttribute);
		final List<ValidationMessage> mappedErrors = mapViolationsToValidationResult(importParameters, allConstraints,
				validationErrors);
		return mappedErrors.isEmpty() ? ExcelValidationResult.SUCCESS : new ExcelValidationResult(mappedErrors);
	}

	private List<ValidationMessage> mapViolationsToValidationResult(final ImportParameters importParameters,
			final Collection<LocalizedAttributeConstraint> allConstraints,
			final Collection<HybrisConstraintViolation> validationErrors)
	{
		final List<ValidationMessage> mappedErrors = new ArrayList<>();
		for (final HybrisConstraintViolation validationError : validationErrors)
		{
			mappedErrors.addAll(mapSingleViolation(importParameters, allConstraints, validationError));
		}
		return mappedErrors;
	}

	private List<ValidationMessage> mapSingleViolation(final ImportParameters importParameters,
			final Collection<LocalizedAttributeConstraint> allConstraints, final HybrisConstraintViolation validationError)
	{
		boolean anyMatch = false;
		final List<ValidationMessage> mappedErrors = new ArrayList<>();
		for (final LocalizedAttributeConstraint constraint : allConstraints)
		{
			if (constraint.matchesNonLocalizedViolation(validationError))
			{
				if (checkIfLocalesMatch(importParameters, constraint))
				{
					final HybrisConstraintViolation localizedConstraintViolation = violationFactory.createLocalizedConstraintViolation(
							validationError.getConstraintViolation(), new Locale(importParameters.getIsoCode()));
					mappedErrors.add(new ValidationMessage(localizedConstraintViolation.getLocalizedMessage(),
							localizedConstraintViolation.getViolationSeverity()));
				}
				anyMatch = true;
			}
		}
		if (!anyMatch)
		{
			mappedErrors.add(new ValidationMessage(validationError.getLocalizedMessage(), validationError.getViolationSeverity()));
		}
		return mappedErrors;
	}

	private static boolean checkIfLocalesMatch(final ImportParameters importParameters,
			final LocalizedAttributeConstraint constraint)
	{
		return constraint.getLanguages().stream().map(Locale::toString).collect(Collectors.toSet())
				.contains(importParameters.getIsoCode());
	}

	protected Collection<LocalizedAttributeConstraint> getAllAttributeConstraints(final Class clazz)
	{
		final Collection<LocalizedAttributeConstraint> constraints = ClassUtils.getAllSuperclasses(clazz).stream()
				.flatMap(aClass -> getLocalizedConstraintsRegistry().get(aClass).getConstraints().stream())
				.collect(Collectors.toSet());
		constraints.addAll(getLocalizedConstraintsRegistry().get(clazz).getConstraints());
		return constraints;
	}

	public ConstraintViolationFactory getViolationFactory()
	{
		return violationFactory;
	}

	@Required
	public void setViolationFactory(final ConstraintViolationFactory violationFactory)
	{
		this.violationFactory = violationFactory;
	}

	public LocalizedConstraintsRegistry getLocalizedConstraintsRegistry()
	{
		return localizedConstraintsRegistry;
	}

	@Required
	public void setLocalizedConstraintsRegistry(final LocalizedConstraintsRegistry localizedConstraintsRegistry)
	{
		this.localizedConstraintsRegistry = localizedConstraintsRegistry;
	}
}
